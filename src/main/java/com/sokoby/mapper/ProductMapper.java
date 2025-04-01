package com.sokoby.mapper;

import com.sokoby.entity.Product;
import com.sokoby.entity.Variant;
import com.sokoby.enums.ProductStatus;
import com.sokoby.payload.CollectionDto;
import com.sokoby.payload.ImageDto;
import com.sokoby.payload.ProductDto;
import com.sokoby.payload.VariantDto;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        if(product.getVariants()!= null){
            List<VariantDto> variantDtoList = product.getVariants().stream().map(variant -> VariantMapper.toDto(variant, variant.getInventoryItem().getStockQuantity())).toList();
            dto.setVariant(variantDtoList);
        }else {
            dto.setVariant(Collections.emptyList());
        }

        dto.setStatus(product.getStatus().toString());
        dto.setComparedPrice(product.getComparedPrice());

        // Map images without recursion
        if (product.getProductImages() != null) {
            List<ImageDto> imageDtos = product.getProductImages().stream()
                    .map(ProductImageMapper::toDto) // Assumes no back-reference to Product
                    .collect(Collectors.toList());
            dto.setImageUrls(imageDtos);
        } else {
            dto.setImageUrls(Collections.emptyList());
        }

        // Map collections without recursion
        if (product.getCollections() != null) {
            List<CollectionDto> collectionDtos = product.getCollections().stream()
                    .map(CollectionMapper::toDto) // Pass flag to avoid product mapping
                    .collect(Collectors.toList());
            dto.setCollections(collectionDtos);
        } else {
            dto.setCollections(Collections.emptyList());
        }

        dto.setStock(product.getInventory() != null ? product.getInventory().getStockQuantity() : 0);
        dto.setSku(product.getSku() != null ? product.getSku().getSkuCode() : null);

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


    public static ProductDto toDtoWithImageCollection(Product product, List<ImageDto> imageDto, List<CollectionDto> collectionDto) {
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
}