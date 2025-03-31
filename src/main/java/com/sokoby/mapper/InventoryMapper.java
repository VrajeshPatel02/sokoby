package com.sokoby.mapper;

import com.sokoby.entity.Inventory;
import com.sokoby.entity.SKU;
import com.sokoby.payload.InventoryDto;

public class InventoryMapper {

    private InventoryMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Inventory toEntity(SKU sku, Integer stockQuantity) {
        Inventory inventory = new Inventory();
        inventory.setSku(sku);
        inventory.setStockQuantity(stockQuantity != null ? stockQuantity : 0);
        return inventory;
    }

    public static InventoryDto toDto(Inventory inventory) {
        InventoryDto dto = new InventoryDto();
        dto.setInventoryId(inventory.getId());
        dto.setSkuId(inventory.getSku().getId());
        dto.setSkuCode(inventory.getSku().getSkuCode());
        dto.setStockQuantity(inventory.getStockQuantity());
        return dto;
    }
}