package com.sokoby.service.impl;

import com.sokoby.entity.InventoryItem;
import com.sokoby.entity.InventoryLevel;
import com.sokoby.entity.Location;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.InventoryItemRepository;
import com.sokoby.repository.InventoryLevelRepository;
import com.sokoby.repository.LocationRepository;
import com.sokoby.service.InventoryLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryLevelServiceImpl implements InventoryLevelService {

    private final InventoryLevelRepository inventoryLevelRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public InventoryLevelServiceImpl(InventoryLevelRepository inventoryLevelRepository,
                                     InventoryItemRepository inventoryItemRepository,
                                     LocationRepository locationRepository) {
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public List<InventoryLevel> getAllInventoryLevels() {
        return inventoryLevelRepository.findAll();
    }

    @Override
    public InventoryLevel getInventoryLevelById(UUID id) {
        return inventoryLevelRepository.findById(id)
                .orElseThrow(() -> new MerchantException("InventoryLevel not found", "LEVEL_NOT_FOUND"));
    }

    @Override
    @Transactional
    public InventoryLevel createInventoryLevel(UUID inventoryItemId, UUID locationId, Integer initialStock) {
        InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new MerchantException("InventoryItem not found", "ITEM_NOT_FOUND"));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new MerchantException("Location not found", "LOCATION_NOT_FOUND"));

        InventoryLevel level = new InventoryLevel();
        level.setInventoryItem(item);
        level.setLocationId(location.getId());
        level.setAvailableQuantity(initialStock != null ? initialStock : 0);
        return inventoryLevelRepository.save(level);
    }

    @Override
    @Transactional
    public InventoryLevel updateInventoryLevel(UUID id, Integer availableQuantity, Integer reservedQuantity) {
        InventoryLevel level = getInventoryLevelById(id);
        if (availableQuantity != null) {
            level.setAvailableQuantity(availableQuantity);
        }
        if (reservedQuantity != null) {
            level.setReservedQuantity(reservedQuantity);
        }
        return inventoryLevelRepository.save(level);
    }

    @Override
    @Transactional
    public void deleteInventoryLevel(UUID id) {
        InventoryLevel level = getInventoryLevelById(id);
        inventoryLevelRepository.delete(level);
    }
}