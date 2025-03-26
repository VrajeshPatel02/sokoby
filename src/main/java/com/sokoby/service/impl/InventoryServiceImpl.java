package com.sokoby.service.impl;

import com.sokoby.entity.InventoryItem;
import com.sokoby.entity.InventoryLevel;
import com.sokoby.entity.Location;
import com.sokoby.entity.Variant;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.InventoryItemRepository;
import com.sokoby.repository.InventoryLevelRepository;
import com.sokoby.repository.LocationRepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final VariantRepository variantRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public InventoryServiceImpl(VariantRepository variantRepository,
                                InventoryItemRepository inventoryItemRepository,
                                InventoryLevelRepository inventoryLevelRepository,
                                LocationRepository locationRepository) {
        this.variantRepository = variantRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryItemRepository.findAll();
    }

    @Override
    @Transactional
    public InventoryItem createInventoryItem(UUID variantId, Integer initialStock) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));

        InventoryItem item = new InventoryItem();
        item.setSku(variant.getSku()); // Use variant's SKU for consistency
        item.setVariant(variant);
        InventoryItem savedItem = inventoryItemRepository.save(item);

        // Set initial stock at a default location
        var defaultLocation = locationRepository.findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new MerchantException("No locations found", "NO_LOCATIONS"));
        InventoryLevel level = new InventoryLevel();
        level.setInventoryItem(savedItem);
        level.setLocation(defaultLocation);
        level.setAvailableQuantity(initialStock != null ? initialStock : 0);
        inventoryLevelRepository.save(level);

        return savedItem;
    }

    @Override
    @Transactional
    public void updateStock(UUID variantId, Integer newStock) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        InventoryItem item = variant.getInventoryItem();
        if (item == null) {
            throw new MerchantException("No inventory item linked to variant", "NO_INVENTORY_ITEM");
        }
        List<InventoryLevel> levels = item.getInventoryLevels();
        if (levels.isEmpty()) {
            throw new MerchantException("No inventory levels found for item", "NO_STOCK_LEVELS");
        }
        // Update stock at the first location for simplicity
        InventoryLevel level = levels.get(0);
        level.setAvailableQuantity(newStock);
        inventoryLevelRepository.save(level);
    }

    @Override
    public InventoryItem getInventoryItemById(UUID id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Inventory item not found", "ITEM_NOT_FOUND"));
    }

    @Override
    @Transactional
    public InventoryItem updateInventoryItem(InventoryItem item) {
        InventoryItem existingItem = getInventoryItemById(item.getId());
        existingItem.setSku(item.getSku());
        return inventoryItemRepository.save(existingItem);
    }

    @Override
    @Transactional
    public void deleteInventoryItem(UUID id) {
        InventoryItem item = getInventoryItemById(id);
        inventoryItemRepository.delete(item);
    }

    @Override
    public boolean isAvailable(UUID variantId, int quantity) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        InventoryItem item = variant.getInventoryItem();
        if (item == null) return false;
        int totalAvailable = item.getInventoryLevels().stream()
                .mapToInt(InventoryLevel::getAvailableQuantity)
                .sum();
        return totalAvailable >= quantity;
    }

    @Override
    @Transactional
    public void reserveStock(UUID variantId, int quantity) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        InventoryItem item = variant.getInventoryItem();
        if (item == null) throw new MerchantException("No inventory item linked", "NO_INVENTORY_ITEM");

        int remaining = quantity;
        for (InventoryLevel level : item.getInventoryLevels()) {
            int available = level.getAvailableQuantity();
            if (available > 0 && remaining > 0) {
                int toReserve = Math.min(available, remaining);
                level.setAvailableQuantity(available - toReserve);
                level.setReservedQuantity(level.getReservedQuantity() + toReserve);
                inventoryLevelRepository.save(level);
                remaining -= toReserve;
            }
        }
        if (remaining > 0) throw new MerchantException("Insufficient stock", "INSUFFICIENT_STOCK");
    }

    @Override
    @Transactional
    public void releaseStock(UUID variantId, int quantity) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        InventoryItem item = variant.getInventoryItem();
        if (item == null) throw new MerchantException("No inventory item linked", "NO_INVENTORY_ITEM");

        int remaining = quantity;
        for (InventoryLevel level : item.getInventoryLevels()) {
            int reserved = level.getReservedQuantity();
            if (reserved > 0 && remaining > 0) {
                int toRelease = Math.min(reserved, remaining);
                level.setAvailableQuantity(level.getAvailableQuantity() + toRelease);
                level.setReservedQuantity(reserved - toRelease);
                inventoryLevelRepository.save(level);
                remaining -= toRelease;
            }
        }
        if (remaining > 0) throw new MerchantException("Insufficient reserved stock", "INSUFFICIENT_RESERVED_STOCK");
    }
}