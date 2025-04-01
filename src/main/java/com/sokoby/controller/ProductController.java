package com.sokoby.controller;

import com.sokoby.payload.ProductCreationDto;
import com.sokoby.payload.ProductDto;
import com.sokoby.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create/{storeId}")
    public ResponseEntity<ProductDto> createProduct(
            @PathVariable UUID storeId,
            @RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(storeId, dto));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductCreationDto> getProductById(@PathVariable UUID productId) {
        ProductCreationDto product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductDto>> getProductsByStoreId(@PathVariable UUID storeId) {
        return ResponseEntity.ok(productService.getProductsByStoreId(storeId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.searchProducts(query, pageable));
    }

    @GetMapping("/search/store/{storeId}")
    public ResponseEntity<Page<ProductDto>> searchProductsByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.searchProductsByStore(storeId, query, pageable));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID id,
            @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}