package com.beanandbrew.dto;

import java.math.BigDecimal;

public class TopProduct {

    private String productName;
    private String variantName;
    private long unitsSold;
    private BigDecimal revenue = BigDecimal.ZERO;

    public TopProduct(String productName, String variantName) {
        this.productName = productName;
        this.variantName = variantName;
    }

    public void add(long units, BigDecimal amount) {
        this.unitsSold += units;
        this.revenue = this.revenue.add(amount);
    }

    public String getProductName() { return productName; }
    public String getVariantName() { return variantName; }
    public long getUnitsSold() { return unitsSold; }
    public BigDecimal getRevenue() { return revenue; }
}