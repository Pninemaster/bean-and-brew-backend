package com.beanandbrew.service;

import com.beanandbrew.dto.AdminOverviewResponse;
import com.beanandbrew.dto.LowStockItem;
import com.beanandbrew.entity.Order;
import com.beanandbrew.entity.OrderStatus;
import com.beanandbrew.entity.Product;
import com.beanandbrew.entity.ProductVariant;
import com.beanandbrew.repository.OrderRepository;
import com.beanandbrew.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
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
}