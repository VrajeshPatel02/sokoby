package com.sokoby.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class InventoryLevelDto {
    private UUID id;
    private UUID inventoryItemId;
    private UUID locationId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Date updatedAt;
}