package com.sokoby.controller;

import com.sokoby.payload.CollectionDto;
import com.sokoby.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionService collectionService;

    @Autowired
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<CollectionDto> createCollection(
            @PathVariable UUID productId,
            @RequestBody CollectionDto dto) {
        CollectionDto createdCollection = collectionService.createCategory(productId, dto);
        return new ResponseEntity<>(createdCollection, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollectionById(@PathVariable UUID id) {
        CollectionDto collection = collectionService.getCategoryById(id);
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CollectionDto>> getCollectionsByStoreId(@PathVariable UUID storeId) {
        List<CollectionDto> collections = collectionService.getCategoriesByStoreId(storeId);
        return ResponseEntity.ok(collections);
    }

    @GetMapping("/store/{storeId}/paged")
    public ResponseEntity<Page<CollectionDto>> getCollectionsByStoreIdPaged(
            @PathVariable UUID storeId,
            Pageable pageable) {
        Page<CollectionDto> collections = collectionService.getCategoriesByStoreId(storeId, pageable);
        return ResponseEntity.ok(collections);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable UUID id,
            @RequestBody CollectionDto dto) {
        CollectionDto updatedCollection = collectionService.updateCategory(id, dto);
        return ResponseEntity.ok(updatedCollection);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable UUID id) {
        collectionService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}