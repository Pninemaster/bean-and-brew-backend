package com.beanandbrew.dto;

import com.beanandbrew.entity.Order;

import java.math.BigDecimal;
import java.util.List;

public class AdminOverviewResponse {

    private BigDecimal totalSales;
    private long totalOrders;
    private long pendingOrders;
    private List<LowStockItem> lowStockItems;
    private List<Order> recentOrders;

    public AdminOverviewResponse(BigDecimal totalSales, long totalOrders, long pendingOrders,
                                  List<LowStockItem> lowStockItems, List<Order> recentOrders) {
        this.totalSales = totalSales;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.lowStockItems = lowStockItems;
        this.recentOrders = recentOrders;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public List<LowStockItem> getLowStockItems() {
        return lowStockItems;
    }

    public List<Order> getRecentOrders() {
        return recentOrders;
    }
}