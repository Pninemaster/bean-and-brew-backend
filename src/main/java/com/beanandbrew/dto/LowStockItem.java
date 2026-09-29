package com.beanandbrew.dto;

public class LowStockItem {

    private Long productId;
    private String productName;
    private String variantName;
    private Integer stock;

    public LowStockItem(Long productId, String productName, String variantName, Integer stock) {
        this.productId = productId;
        this.productName = productName;
        this.variantName = variantName;
        this.stock = stock;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public Integer getStock() {
        return stock;
    }
}