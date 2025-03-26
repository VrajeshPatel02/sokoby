package com.sokoby.service;

import com.sokoby.entity.InventoryLevel;

import java.util.List;
import java.util.UUID;

public interface InventoryLevelService {
    List<InventoryLevel> getAllInventoryLevels();
    InventoryLevel getInventoryLevelById(UUID id);
    InventoryLevel createInventoryLevel(UUID inventoryItemId, UUID locationId, Integer initialStock);
    InventoryLevel updateInventoryLevel(UUID id, Integer availableQuantity, Integer reservedQuantity);
    void deleteInventoryLevel(UUID id);
}