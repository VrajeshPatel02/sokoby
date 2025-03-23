package com.sokoby.controller;

import com.sokoby.payload.VariantDto;
import com.sokoby.service.VariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/variant")
public class VariantController {
    private final VariantService variantService;

    @Autowired
    public VariantController(VariantService variantService) {
        this.variantService = variantService;
    }

    @PostMapping("/create/{productId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<VariantDto> createVariant(
            @PathVariable UUID productId,
            @RequestBody VariantDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(variantService.createVariant(productId, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantDto> getVariantById(@PathVariable UUID id) {
        return ResponseEntity.ok(variantService.getVariantById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<VariantDto>> getVariantsByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(variantService.getVariantsByProductId(productId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<VariantDto>> searchVariants(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(variantService.searchVariants(query, pageable));
    }

    @GetMapping("/search/product/{productId}")
    public ResponseEntity<Page<VariantDto>> searchVariantsByProduct(
            @PathVariable UUID productId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(variantService.searchVariantsByProduct(productId, query, pageable));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<VariantDto> updateVariant(
            @PathVariable UUID id,
            @RequestBody VariantDto dto) {
        return ResponseEntity.ok(variantService.updateVariant(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deleteVariant(@PathVariable UUID id) {
        variantService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reduce-stock/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> reduceStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        variantService.reduceStock(id, quantity);
        return ResponseEntity.ok().build();
    }
}