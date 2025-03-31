package com.sokoby.payload;

import lombok.Data;

import java.util.UUID;

@Data
public class InventoryDto {
    private UUID inventoryId; // Returned in response
    private UUID skuId;       // Returned in response
    private String skuCode;   // Returned in response
    private Integer stockQuantity; // Stock level
}