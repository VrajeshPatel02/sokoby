package com.sokoby.service;

import com.sokoby.entity.Inventory;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
    List<Inventory> getAllInventorys();

    Inventory createInventoryForVariant(UUID variantId, Integer initialStock);
    Inventory createInventoryForProduct(UUID productId, Integer initialStock);

    Inventory getInventoryById(UUID id);

    Inventory updateInventory(Inventory item);

    void updateStockForVariant(UUID variantId, Integer newStock);
    void updateStockForProduct(UUID productId, Integer newStock);

    void deleteInventory(UUID id);

    boolean isAvailable(UUID variantId, int quantity);
    boolean isAvailableForProduct(UUID productId, Integer quantity);

    void reserveStock(UUID variantId, int quantity);
    void reserveStockForProduct(UUID productId, int quantity);

    void releaseStock(UUID variantId, int quantity);
    void releaseStockForProduct(UUID productId, int quantity);
}