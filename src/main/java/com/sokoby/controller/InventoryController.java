package com.sokoby.controller;

import com.sokoby.entity.Inventory;
import com.sokoby.mapper.InventoryMapper;
import com.sokoby.payload.InventoryDto;
import com.sokoby.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryDto>> getAllInventorys() {
        List<InventoryDto> inventories = inventoryService.getAllInventorys()
                .stream()
                .map(InventoryMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(inventories);
    }

    @PostMapping("/variant/{variantId}")
    public ResponseEntity<InventoryDto> createInventoryForVariant(
            @PathVariable UUID variantId,
            @RequestParam Integer initialStock) {
        Inventory inventory = inventoryService.createInventoryForVariant(variantId, initialStock);
        return new ResponseEntity<>(InventoryMapper.toDto(inventory), HttpStatus.CREATED);
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<InventoryDto> createInventoryForProduct(
            @PathVariable UUID productId,
            @RequestParam Integer initialStock) {
        Inventory inventory = inventoryService.createInventoryForProduct(productId, initialStock);
        return new ResponseEntity<>(InventoryMapper.toDto(inventory), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto> getInventoryById(@PathVariable UUID id) {
        Inventory inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(InventoryMapper.toDto(inventory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable UUID id,
            @RequestBody InventoryDto dto) {
        Inventory inventory = inventoryService.getInventoryById(id);
        inventory.setStockQuantity(dto.getStockQuantity());
        Inventory updatedInventory = inventoryService.updateInventory(inventory);
        return ResponseEntity.ok(InventoryMapper.toDto(updatedInventory));
    }

    @PutMapping("/variant/{variantId}/stock")
    public ResponseEntity<Void> updateStockForVariant(
            @PathVariable UUID variantId,
            @RequestParam Integer newStock) {
        inventoryService.updateStockForVariant(variantId, newStock);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/product/{productId}/stock")
    public ResponseEntity<Void> updateStockForProduct(
            @PathVariable UUID productId,
            @RequestParam Integer newStock) {
        inventoryService.updateStockForProduct(productId, newStock);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable UUID id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/variant/{variantId}/availability")
    public ResponseEntity<Boolean> isAvailable(
            @PathVariable UUID variantId,
            @RequestParam int quantity) {
        boolean available = inventoryService.isAvailable(variantId, quantity);
        return ResponseEntity.ok(available);
    }

    @GetMapping("/product/{productId}/availability")
    public ResponseEntity<Boolean> isAvailableForProduct(
            @PathVariable UUID productId,
            @RequestParam int quantity) {
        boolean available = inventoryService.isAvailableForProduct(productId, quantity);
        return ResponseEntity.ok(available);
    }

    @PostMapping("/variant/{variantId}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable UUID variantId,
            @RequestParam int quantity) {
        inventoryService.reserveStock(variantId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/product/{productId}/reserve")
    public ResponseEntity<Void> reserveStockForProduct(
            @PathVariable UUID productId,
            @RequestParam int quantity) {
        inventoryService.reserveStockForProduct(productId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/variant/{variantId}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable UUID variantId,
            @RequestParam int quantity) {
        inventoryService.releaseStock(variantId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/product/{productId}/release")
    public ResponseEntity<Void> releaseStockForProduct(
            @PathVariable UUID productId,
            @RequestParam int quantity) {
        inventoryService.releaseStockForProduct(productId, quantity);
        return ResponseEntity.ok().build();
    }
}