package com.sokoby.mapper;

import com.sokoby.entity.InventoryItem;
import com.sokoby.payload.InventoryItemDto;

public class InventoryMapper {
    private InventoryMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static InventoryItemDto toDto(InventoryItem inventoryItem) {
        if (inventoryItem == null) {
            throw new IllegalArgumentException("InventoryItem entity cannot be null for mapping to DTO");
        }

        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(inventoryItem.getId());
        dto.setSku(inventoryItem.getSku());
        dto.setVariantId(inventoryItem.getVariant() != null ? inventoryItem.getVariant().getId() : null);
        int totalStock = inventoryItem.getInventoryLevels().stream()
                .mapToInt(il -> il.getAvailableQuantity())
                .sum();
        dto.setStock(totalStock);
        dto.setCreatedAt(inventoryItem.getCreatedAt());
        return dto;
    }

    public static InventoryItem toEntity(InventoryItemDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("InventoryItemDto cannot be null for mapping to entity");
        }

        InventoryItem inventoryItem = new InventoryItem();
        if (dto.getId() != null) {
            inventoryItem.setId(dto.getId());
        }
        inventoryItem.setSku(dto.getSku());
        // Variant relationship and stock levels are set in the service layer
        return inventoryItem;
    }
}