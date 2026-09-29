package com.beanandbrew.service;

import com.beanandbrew.dto.CheckoutRequest;
import com.beanandbrew.dto.OrderItemRequest;
import com.beanandbrew.entity.*;
import com.beanandbrew.repository.OrderRepository;
import com.beanandbrew.repository.ProductRepository;
import com.beanandbrew.repository.ProductVariantRepository;
import com.beanandbrew.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                         ProductRepository productRepository,
                         ProductVariantRepository variantRepository,
                         UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order checkout(String userEmail, CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with no items");
        }
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        if (!"Cash on Delivery".equals(request.getPaymentMethod())) {
            throw new IllegalArgumentException("Only Cash on Delivery is supported");
        }
        for (OrderItemRequest check : request.getItems()) {
            if (check.getProductId() == null) {
                throw new IllegalArgumentException("Each item needs a product");
            }
            if (check.getQuantity() == null || check.getQuantity() < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemRequest.getProductId()));

            if (!product.isActive()) {
                throw new IllegalArgumentException(product.getName() + " is no longer available");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());

            if (itemRequest.getVariantId() != null) {
                ProductVariant variant = variantRepository.findById(itemRequest.getVariantId())
                        .orElseThrow(() -> new IllegalArgumentException("Variant not found with id: " + itemRequest.getVariantId()));

                if (variant.getStock() < itemRequest.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Only " + variant.getStock() + " left in stock for " + product.getName() + " " + variant.getName());
                }

                variant.setStock(variant.getStock() - itemRequest.getQuantity());

                orderItem.setVariantId(variant.getId());
                orderItem.setVariantName(variant.getName());
                orderItem.setUnitPrice(variant.getPrice());
            } else {
                if (product.getStock() < itemRequest.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Only " + product.getStock() + " left in stock for " + product.getName());
                }

                product.setStock(product.getStock() - itemRequest.getQuantity());

                orderItem.setUnitPrice(product.getPrice());
            }

            total = total.add(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setAddress(request.getAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);
        order.setCreatedAt(LocalDateTime.now());

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        return orderRepository.save(order);
    }
 
    @Transactional(readOnly = true)
    public List<Order> getOrdersForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }
}