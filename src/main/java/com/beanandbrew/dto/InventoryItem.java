package com.beanandbrew.dto;

public class InventoryItem {

    private Long productId;
    private String productName;
    private Long variantId;
    private String variantName;
    private Integer stock;
    private boolean active;

    public InventoryItem(Long productId, String productName, Long variantId,
                          String variantName, Integer stock, boolean active) {
        this.productId = productId;
        this.productName = productName;
        this.variantId = variantId;
        this.variantName = variantName;
        this.stock = stock;
        this.active = active;
    }

    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Long getVariantId() { return variantId; }
    public String getVariantName() { return variantName; }
    public Integer getStock() { return stock; }
    public boolean isActive() { return active; }
}