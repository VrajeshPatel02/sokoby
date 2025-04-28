package com.sokoby.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sokoby.payload.ProductCreationDto;
import com.sokoby.payload.ProductDto;
import com.sokoby.service.ProductService;

@RestController
@RequestMapping(value ="/api/product" ,consumes = {"multipart/form-data", "application/octet-stream"})
public class ProductImageController {
    @Autowired
    private ProductService productService;

    @PostMapping("/create/{storeId}/images")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ProductDto> createProduct(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable UUID storeId,
            @ModelAttribute ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProductWithImages(storeId, dto, files));
    }

    @PostMapping("/create/form-data")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ProductCreationDto> createProductWithDetails(
            @ModelAttribute ProductCreationDto dto,
            @RequestParam(value = "files") MultipartFile[] files) {
        ProductCreationDto createdProduct = productService.createProductWithDetails(dto, files);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/update/{productId}/form-data")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ProductCreationDto>  updateProductWithDetails(
            @ModelAttribute ProductCreationDto dto,
            @RequestParam(value = "files") MultipartFile[] files,
            @PathVariable(value = "productId") UUID productId){
        ProductCreationDto createdProduct = productService.updateProductWithDetails(productId,dto, files);
        return new ResponseEntity<>(createdProduct, HttpStatus.ACCEPTED);
    }
}
