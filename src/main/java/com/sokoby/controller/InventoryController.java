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

    @GetMapping
    public ResponseEntity<List<InventoryItemDto>> getAllInventoryItems() {
        List<InventoryItemDto> dtos = inventoryService.getAllInventoryItems().stream()
                .map(InventoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/variant")
    public ResponseEntity<InventoryItemDto> createInventoryItemForVariant(@RequestBody InventoryItemDto dto) {
        if (dto.getVariantId() == null) {
            throw new MerchantException("Variant ID cannot be null", "INVALID_VARIANT_ID");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new MerchantException("Stock must be non-negative", "INVALID_STOCK");
        }

        InventoryItem savedItem = inventoryService.createInventoryItemForVariant(dto.getVariantId(), dto.getStock());
        return new ResponseEntity<>(InventoryMapper.toDto(savedItem), HttpStatus.CREATED);
    }

    @PostMapping("/product")
    public ResponseEntity<InventoryItemDto> createInventoryItemForProduct(@RequestBody InventoryItemDto dto) {
        if (dto.getProductId() == null) {
            throw new MerchantException("Product ID cannot be null", "INVALID_PRODUCT_ID");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            throw new MerchantException("Stock must be non-negative", "INVALID_STOCK");
        }

        InventoryItem savedItem = inventoryService.createInventoryItemForProduct(dto.getProductId(), dto.getStock());
        return new ResponseEntity<>(InventoryMapper.toDto(savedItem), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDto> getInventoryItemById(@PathVariable UUID id) {
        InventoryItem item = inventoryService.getInventoryItemById(id);
        return ResponseEntity.ok(InventoryMapper.toDto(item));
    }

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
            if (item.getVariant() != null) {
                inventoryService.updateStockForVariant(item.getVariant().getId(), dto.getStock());
            } else if (item.getProduct() != null) {
                inventoryService.updateStockForProduct(item.getProduct().getId(), dto.getStock());
            }
        }
        InventoryItem updatedItem = inventoryService.updateInventoryItem(item);
        return ResponseEntity.ok(InventoryMapper.toDto(updatedItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable UUID id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }
}