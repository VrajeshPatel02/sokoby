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
        dto.setLocationId(inventoryLevel.getLocationId()); // Updated to match entity field
        dto.setAvailableQuantity(inventoryLevel.getQuantity()); // Assuming quantity is available stock
        dto.setReservedQuantity(0); // Placeholder; update if reserved quantity is tracked separately
        dto.setUpdatedAt(null); // Add updatedAt to InventoryLevel entity if needed
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
        inventoryLevel.setQuantity(dto.getAvailableQuantity() != null ? dto.getAvailableQuantity() : 0);
        inventoryLevel.setLocationId(dto.getLocationId());
        // Relationships (inventoryItem) set in service
        return inventoryLevel;
    }
}