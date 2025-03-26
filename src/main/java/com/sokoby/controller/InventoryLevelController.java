package com.sokoby.controller;

import com.sokoby.entity.InventoryLevel;
import com.sokoby.mapper.InventoryLevelMapper;
import com.sokoby.payload.InventoryLevelDto;
import com.sokoby.service.InventoryLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory/levels")
public class InventoryLevelController {

    private final InventoryLevelService inventoryLevelService;

    @Autowired
    public InventoryLevelController(InventoryLevelService inventoryLevelService) {
        this.inventoryLevelService = inventoryLevelService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryLevelDto>> getAllInventoryLevels() {
        List<InventoryLevelDto> dtos = inventoryLevelService.getAllInventoryLevels().stream()
                .map(InventoryLevelMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryLevelDto> getInventoryLevelById(@PathVariable UUID id) {
        InventoryLevel level = inventoryLevelService.getInventoryLevelById(id);
        return ResponseEntity.ok(InventoryLevelMapper.toDto(level));
    }

    @PostMapping
    public ResponseEntity<InventoryLevelDto> createInventoryLevel(@RequestBody InventoryLevelDto dto) {
        InventoryLevel level = inventoryLevelService.createInventoryLevel(
                dto.getInventoryItemId(), dto.getLocationId(), dto.getAvailableQuantity());
        return new ResponseEntity<>(InventoryLevelMapper.toDto(level), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryLevelDto> updateInventoryLevel(
            @PathVariable UUID id, @RequestBody InventoryLevelDto dto) {
        InventoryLevel updated = inventoryLevelService.updateInventoryLevel(
                id, dto.getAvailableQuantity(), dto.getReservedQuantity());
        return ResponseEntity.ok(InventoryLevelMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryLevel(@PathVariable UUID id) {
        inventoryLevelService.deleteInventoryLevel(id);
        return ResponseEntity.noContent().build();
    }
}