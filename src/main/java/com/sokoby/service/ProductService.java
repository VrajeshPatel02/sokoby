package com.sokoby.service;

import com.sokoby.payload.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(UUID storeId, ProductDto dto);

    ProductDto getProductById(UUID id);

    List<ProductDto> getProductsByStoreId(UUID storeId);

    Page<ProductDto> searchProducts(String query, Pageable pageable);

    Page<ProductDto> searchProductsByStore(UUID storeId, String query, Pageable pageable);

    ProductDto updateProduct(UUID id, ProductDto dto);

    void deleteProduct(UUID id);
}