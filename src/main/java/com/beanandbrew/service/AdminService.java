package com.beanandbrew.service;

import com.beanandbrew.dto.AdminOverviewResponse;
import com.beanandbrew.dto.InventoryItem;
import com.beanandbrew.dto.LowStockItem;
import com.beanandbrew.dto.StockUpdateRequest;
import com.beanandbrew.entity.Order;
import com.beanandbrew.entity.OrderStatus;
import com.beanandbrew.entity.Product;
import com.beanandbrew.entity.ProductVariant;
import com.beanandbrew.repository.OrderRepository;
import com.beanandbrew.repository.ProductRepository;
import com.beanandbrew.repository.ProductVariantRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.beanandbrew.dto.ReportResponse;
import com.beanandbrew.dto.TopProduct;
import com.beanandbrew.entity.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public AdminService(OrderRepository orderRepository,
                         ProductRepository productRepository,
                         ProductVariantRepository variantRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalSales = BigDecimal.ZERO;
        long pending = 0;
        for (Order order : allOrders) {
            totalSales = totalSales.add(order.getTotalAmount());
            if (order.getStatus() == OrderStatus.PENDING) {
                pending++;
            }
        }

        List<LowStockItem> lowStock = new ArrayList<>();
        for (Product product : productRepository.findAll()) {
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                for (ProductVariant variant : product.getVariants()) {
                    int stock = variant.getStock() == null ? 0 : variant.getStock();
                    if (stock <= LOW_STOCK_THRESHOLD) {
                        lowStock.add(new LowStockItem(product.getId(), product.getName(), variant.getName(), stock));
                    }
                }
            } else {
                int stock = product.getStock() == null ? 0 : product.getStock();
                if (stock <= LOW_STOCK_THRESHOLD) {
                    lowStock.add(new LowStockItem(product.getId(), product.getName(), null, stock));
                }
            }
        }

        List<Order> recent = orderRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();

        return new AdminOverviewResponse(totalSales, allOrders.size(), pending, lowStock, recent);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional
    public Order updateOrderStatus(Long id, String statusText) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

        OrderStatus requested;
        try {
            requested = OrderStatus.valueOf(statusText == null ? "" : statusText.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status: " + statusText);
        }

        OrderStatus[] flow = OrderStatus.values();
        int current = order.getStatus().ordinal();

        if (current == flow.length - 1) {
            throw new IllegalArgumentException("This order is already completed");
        }
        if (requested.ordinal() != current + 1) {
            throw new IllegalArgumentException(
                    "An order in " + order.getStatus() + " can only move to " + flow[current + 1]);
        }

        order.setStatus(requested);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getInventory() {
        List<InventoryItem> items = new ArrayList<>();
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        for (Product product : products) {
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                for (ProductVariant variant : product.getVariants()) {
                    items.add(new InventoryItem(product.getId(), product.getName(), variant.getId(),
                            variant.getName(), variant.getStock() == null ? 0 : variant.getStock(),
                            product.isActive()));
                }
            } else {
                items.add(new InventoryItem(product.getId(), product.getName(), null, null,
                        product.getStock() == null ? 0 : product.getStock(), product.isActive()));
            }
        }
        return items;
    }

    @Transactional
    public InventoryItem updateStock(StockUpdateRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product is required");
        }
        if (request.getQuantity() == null) {
            throw new IllegalArgumentException("Quantity is required");
        }
        String mode = request.getMode() == null ? "" : request.getMode().trim().toUpperCase();
        if (!mode.equals("ADD") && !mode.equals("SET")) {
            throw new IllegalArgumentException("Mode must be ADD or SET");
        }
        int qty = request.getQuantity();
        if (mode.equals("ADD") && qty < 1) {
            throw new IllegalArgumentException("Quantity to add must be at least 1");
        }
        if (mode.equals("SET") && qty < 0) {
            throw new IllegalArgumentException("Stock can't be negative");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.getProductId()));

        boolean hasSizes = product.getVariants() != null && !product.getVariants().isEmpty();

        if (request.getVariantId() != null) {
            ProductVariant variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Size not found with id: " + request.getVariantId()));
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("That size doesn't belong to this product");
            }
            if (mode.equals("ADD")) {
                variantRepository.addStock(variant.getId(), qty);
            } else {
                variantRepository.setStock(variant.getId(), qty);
            }
            ProductVariant updated = variantRepository.findById(variant.getId()).orElseThrow();
            return new InventoryItem(product.getId(), product.getName(), updated.getId(),
                    updated.getName(), updated.getStock(), product.isActive());
        }

        if (hasSizes) {
            throw new IllegalArgumentException("This product has sizes. Restock a specific size.");
        }

        if (mode.equals("ADD")) {
            productRepository.addStock(product.getId(), qty);
        } else {
            productRepository.setStock(product.getId(), qty);
        }
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        return new InventoryItem(updated.getId(), updated.getName(), null, null,
                updated.getStock() == null ? 0 : updated.getStock(), updated.isActive());
    }
    
    private List<Order> ordersInRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new IllegalArgumentException("The 'from' date can't be after the 'to' date");
        }
        LocalDateTime start = from == null ? null : from.atStartOfDay();
        LocalDateTime end = to == null ? null : to.plusDays(1).atStartOfDay();

        List<Order> result = new ArrayList<>();
        for (Order order : orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))) {
            LocalDateTime created = order.getCreatedAt();
            if (start != null && created.isBefore(start)) {
                continue;
            }
            if (end != null && !created.isBefore(end)) {
                continue;
            }
            result.add(order);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ReportResponse getReport(LocalDate from, LocalDate to) {
        List<Order> orders = ordersInRange(from, to);

        BigDecimal total = BigDecimal.ZERO;
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status.name(), 0L);
        }
        Map<String, TopProduct> products = new LinkedHashMap<>();

        for (Order order : orders) {
            total = total.add(order.getTotalAmount());
            byStatus.merge(order.getStatus().name(), 1L, Long::sum);

            for (OrderItem item : order.getItems()) {
                String key = item.getProductId() + "-" + item.getVariantId();
                TopProduct entry = products.computeIfAbsent(key,
                        k -> new TopProduct(item.getProductName(), item.getVariantName()));
                entry.add(item.getQuantity(), item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        BigDecimal average = orders.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);

        List<TopProduct> top = new ArrayList<>(products.values());
        top.sort(Comparator.comparingLong(TopProduct::getUnitsSold).reversed()
                .thenComparing(TopProduct::getRevenue, Comparator.reverseOrder()));
        if (top.size() > 10) {
            top = new ArrayList<>(top.subList(0, 10));
        }

        return new ReportResponse(total, orders.size(), average, byStatus, top);
    }

    @Transactional(readOnly = true)
    public String buildOrdersCsv(LocalDate from, LocalDate to) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("Order ID,Date,Status,Customer,Email,Phone,Address,Product,Size,Quantity,Unit price,Line total,Order total\r\n");

        for (Order order : ordersInRange(from, to)) {
            for (OrderItem item : order.getItems()) {
                BigDecimal line = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                sb.append(csv(String.valueOf(order.getId()))).append(',')
                  .append(csv(order.getCreatedAt().format(fmt))).append(',')
                  .append(csv(order.getStatus().name())).append(',')
                  .append(csv(order.getCustomerName())).append(',')
                  .append(csv(order.getCustomerEmail())).append(',')
                  .append(csv(order.getCustomerPhone())).append(',')
                  .append(csv(order.getAddress())).append(',')
                  .append(csv(item.getProductName())).append(',')
                  .append(csv(item.getVariantName())).append(',')
                  .append(item.getQuantity()).append(',')
                  .append(item.getUnitPrice().toPlainString()).append(',')
                  .append(line.toPlainString()).append(',')
                  .append(order.getTotalAmount().toPlainString()).append("\r\n");
            }
        }
        return sb.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}