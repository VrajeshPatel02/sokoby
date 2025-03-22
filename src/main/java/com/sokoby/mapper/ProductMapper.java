package com.sokoby.mapper;

import com.sokoby.entity.Product;
import com.sokoby.payload.ProductDto;

public class ProductMapper {
    private ProductMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static ProductDto toDto(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product entity cannot be null");
        }

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        if (product.getStore() != null) {
            dto.setStoreId(product.getStore().getId());
        }
        return dto;
    }

    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ProductDto cannot be null");
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        // Store association handled in service layer
        return product;
    }
}