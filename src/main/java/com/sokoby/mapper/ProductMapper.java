package com.sokoby.mapper;

import com.sokoby.entity.Product;
import com.sokoby.enums.ProductStatus;
import com.sokoby.payload.ImageDto;
import com.sokoby.payload.ProductDto;

import java.util.List;

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
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        if (product.getStore() != null) {
            dto.setStoreId(product.getStore().getId());
        }
        dto.setStatus(product.getStatus().toString());
        dto.setComparedPrice(product.getComparedPrice());
        return dto;
    }

    public static ProductDto toDtoWithImageDto(Product product, List<ImageDto> imageDto) {
        if (product == null) {
            throw new IllegalArgumentException("Product entity cannot be null");
        }

        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        if (product.getStore() != null) {
            dto.setStoreId(product.getStore().getId());
        }
        dto.setImageUrls(imageDto);
        return dto;
    }

    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ProductDto cannot be null");
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStatus(ProductStatus.valueOf(dto.getStatus().toUpperCase()));
        product.setComparedPrice(dto.getComparedPrice());
        // Store association handled in service layer
        return product;
    }
}