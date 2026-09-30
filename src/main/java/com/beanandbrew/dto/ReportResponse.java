package com.beanandbrew.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ReportResponse {

    private BigDecimal totalSales;
    private long orderCount;
    private BigDecimal averageOrderValue;
    private Map<String, Long> ordersByStatus;
    private List<TopProduct> topProducts;

    public ReportResponse(BigDecimal totalSales, long orderCount, BigDecimal averageOrderValue,
                           Map<String, Long> ordersByStatus, List<TopProduct> topProducts) {
        this.totalSales = totalSales;
        this.orderCount = orderCount;
        this.averageOrderValue = averageOrderValue;
        this.ordersByStatus = ordersByStatus;
        this.topProducts = topProducts;
    }

    public BigDecimal getTotalSales() { return totalSales; }
    public long getOrderCount() { return orderCount; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public Map<String, Long> getOrdersByStatus() { return ordersByStatus; }
    public List<TopProduct> getTopProducts() { return topProducts; }
}