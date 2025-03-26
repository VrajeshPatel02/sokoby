package com.sokoby.mapper;

import com.sokoby.entity.InventoryLevel;
import com.sokoby.payload.InventoryLevelDto;

public class InventoryLevelMapper {
    private InventoryLevelMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static InventoryLevelDto toDto(InventoryLevel inventoryLevel) {
        if (inventoryLevel == null) {
            throw new IllegalArgumentException("InventoryLevel entity cannot be null for mapping to DTO");
        }

        InventoryLevelDto dto = new InventoryLevelDto();
        dto.setId(inventoryLevel.getId());
        dto.setInventoryItemId(inventoryLevel.getInventoryItem() != null ? inventoryLevel.getInventoryItem().getId() : null);
        dto.setLocationId(inventoryLevel.getLocation() != null ? inventoryLevel.getLocation().getId() : null);
        dto.setAvailableQuantity(inventoryLevel.getAvailableQuantity());
        dto.setReservedQuantity(inventoryLevel.getReservedQuantity());
        dto.setUpdatedAt(inventoryLevel.getUpdatedAt());
        return dto;
    }

    public static InventoryLevel toEntity(InventoryLevelDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("InventoryLevelDto cannot be null for mapping to entity");
        }

        InventoryLevel inventoryLevel = new InventoryLevel();
        if (dto.getId() != null) {
            inventoryLevel.setId(dto.getId());
        }
        inventoryLevel.setAvailableQuantity(dto.getAvailableQuantity() != null ? dto.getAvailableQuantity() : 0);
        inventoryLevel.setReservedQuantity(dto.getReservedQuantity() != null ? dto.getReservedQuantity() : 0);
        // Relationships (inventoryItem, location) set in service
        return inventoryLevel;
    }
}