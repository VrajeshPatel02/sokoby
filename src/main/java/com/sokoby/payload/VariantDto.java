package com.sokoby.payload;

import lombok.Data;

import java.util.UUID;

@Data
public class VariantDto {
    private UUID variantId; // Returned in response
    private UUID productId; // Required for creation
    private String skuCode; // SKU identifier
    private Double price;   // Optional override of product price
    private Integer stockQuantity; // Stock tied to SKU
}