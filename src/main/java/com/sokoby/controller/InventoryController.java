package com.sokoby.controller;

import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.InventoryMapper;
import com.sokoby.entity.InventoryItem;
import com.sokoby.payload.InventoryItemDto;
import com.sokoby.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory/items")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // GET: List all inventory items
    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getAllInventoryItems() {
        List<InventoryItemDto> dtos = inventoryService.getAllInventoryItems().stream()
                .map(InventoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // POST: Create a new inventory item
    @PostMapping
    public ResponseEntity<InventoryItemDto> createInventoryItem(@RequestBody InventoryItemDto dto) {
        if (dto.getVariantId() == null) {
            throw new MerchantException("Variant ID cannot be null", "INVALID_VARIANT_ID");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new MerchantException("Stock must be non-negative", "INVALID_STOCK");
        }

        InventoryItem item = InventoryMapper.toEntity(dto);
        InventoryItem savedItem = inventoryService.createInventoryItem(item.getVariant().getId(), dto.getStock());
        return new ResponseEntity<>(InventoryMapper.toDto(savedItem), HttpStatus.CREATED);
    }

    // GET: Retrieve a specific inventory item by ID
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDto> getInventoryItemById(@PathVariable UUID id) {
        InventoryItem item = inventoryService.getInventoryItemById(id);
        return ResponseEntity.ok(InventoryMapper.toDto(item));
    }

    // PUT: Update an inventory item (e.g., SKU or stock levels)
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateInventoryItem(
            @PathVariable UUID id, @RequestBody InventoryItemDto dto) {
        InventoryItem item = inventoryService.getInventoryItemById(id);
        if (dto.getSku() != null && !dto.getSku().trim().isEmpty()) {
            item.setSku(dto.getSku());
        }
        if (dto.getStock() != null) {
            if (dto.getStock() < 0) {
                throw new MerchantException("Stock cannot be negative", "INVALID_STOCK");
            }
            inventoryService.updateStock(id, dto.getStock());
        }
        InventoryItem updatedItem = inventoryService.updateInventoryItem(item);
        return ResponseEntity.ok(InventoryMapper.toDto(updatedItem));
    }

    // DELETE: Remove an inventory item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable UUID id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }
}