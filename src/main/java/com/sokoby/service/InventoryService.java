package com.sokoby.service;

import com.sokoby.entity.InventoryItem;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    List<InventoryItem> getAllInventoryItems();
    InventoryItem createInventoryItem(UUID variantId, Integer initialStock); // Updated signature
    InventoryItem getInventoryItemById(UUID id);
    InventoryItem updateInventoryItem(InventoryItem item);
    void updateStock(UUID variantId, Integer newStock); // Updated to use variantId
    void deleteInventoryItem(UUID id);
    boolean isAvailable(UUID variantId, int quantity);
    void reserveStock(UUID variantId, int quantity);
    void releaseStock(UUID variantId, int quantity);
}