package com.sokoby.controller;

import com.sokoby.payload.StoreDto;
import com.sokoby.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/store")
public class StoreController {
    private final StoreService storeService;

    @Autowired
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable UUID id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<StoreDto> getStoreByMerchantId(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(storeService.getStoreByMerchantId(merchantId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StoreDto>> searchStores(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StoreDto> storePage = storeService.searchStores(query, pageable);
        return ResponseEntity.ok(storePage);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<StoreDto> updateStore(
            @PathVariable UUID id,
            @RequestBody StoreDto dto) {
        return ResponseEntity.ok(storeService.updateStore(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}