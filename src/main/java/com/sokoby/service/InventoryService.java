package com.sokoby.service;

import com.sokoby.entity.InventoryItem;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    List<InventoryItem> getAllInventoryItems();

    InventoryItem createInventoryItemForVariant(UUID variantId, Integer initialStock);
    InventoryItem createInventoryItemForProduct(UUID productId, Integer initialStock);

    InventoryItem getInventoryItemById(UUID id);

    InventoryItem updateInventoryItem(InventoryItem item);

    void updateStockForVariant(UUID variantId, Integer newStock);
    void updateStockForProduct(UUID productId, Integer newStock);

    void deleteInventoryItem(UUID id);

    boolean isAvailable(UUID variantId, int quantity);
    boolean isAvailableForProduct(UUID productId, Integer quantity);

    void reserveStock(UUID variantId, int quantity);
    void reserveStockForProduct(UUID productId, int quantity);

    void releaseStock(UUID variantId, int quantity);
    void releaseStockForProduct(UUID productId, int quantity);
}