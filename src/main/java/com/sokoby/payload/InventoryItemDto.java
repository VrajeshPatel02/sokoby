package com.sokoby.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class InventoryItemDto {
    private UUID id;
    private String sku;
    private UUID variantId;
    private Integer stock; // Total available stock across all locations
    private Date createdAt;
    private UUID productId;
}