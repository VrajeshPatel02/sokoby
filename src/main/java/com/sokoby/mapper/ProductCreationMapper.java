package com.sokoby.mapper;

import com.sokoby.entity.*;
import com.sokoby.enums.ProductStatus;
import com.sokoby.payload.ImageDto;
import com.sokoby.payload.ProductCreationDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProductCreationMapper {

    private ProductCreationMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Product toEntity(ProductCreationDto dto, Store store) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setStore(store);
        product.setPrice(dto.getPrice());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : ProductStatus.ACTIVE);
        product.setComparedPrice(dto.getComparedPrice());
        return product;
    }

    public static SKU toSkuEntity(String skuCode) {
        if (skuCode == null || skuCode.trim().isEmpty()) {
            skuCode = "SKU-" + UUID.randomUUID().toString().substring(0, 8);
        }
        SKU sku = new SKU();
        sku.setSkuCode(skuCode);
        return sku;
    }

    public static Variant toVariantEntity(ProductCreationDto.VariantDto variantDto, Product product) {
        Variant variant = new Variant();
        variant.setName(variantDto.getName());
        variant.setProduct(product);
        variant.setPrice(variantDto.getPrice() != null ? variantDto.getPrice() : product.getPrice());
        return variant;
    }

    public static Inventory toInventoryEntity(SKU sku, Integer stockQuantity) {
        Inventory inventory = new Inventory();
        inventory.setSku(sku);
        inventory.setStockQuantity(stockQuantity != null ? stockQuantity : 0);
        return inventory;
    }

    public static Collection toCollectionEntity(ProductCreationDto.CollectionDto collectionDto, Store store, Product product) {
        Collection collection = new Collection();
        collection.setType(collectionDto.getType());
        collection.setVendor(collectionDto.getVendor());
        collection.setStore(store);
        if (collection.getProducts() == null) {
            collection.setProducts(new ArrayList<>());
        }
        collection.getProducts().add(product);
        return collection;
    }

    public static ProductImage toProductImageEntity(Product product, ImageDto imageDto) {
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setImageUrl(imageDto.getImageUrl());
        return productImage;
    }

    public static ProductCreationDto toDto(Product product, List<ProductCreationDto.VariantDto> variantDtos,
                                           ProductCreationDto.CollectionDto collectionDto, List<ImageDto> imageDtos) {
        ProductCreationDto dto = new ProductCreationDto();
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setStoreId(product.getStore().getId());
        dto.setPrice(product.getPrice());
        dto.setStatus(product.getStatus());
        if(product.getSku().getSkuCode() != null){
            dto.setSkuCode(product.getSku().getSkuCode());
        }
        if(product.getInventory().getStockQuantity() !=null ){
            dto.setStockQuantity(product.getInventory().getStockQuantity());
        }

        dto.setComparedPrice(product.getComparedPrice());

        // Product SKU and Stock
        if (product.getSku() != null) {
            dto.setSkuCode(product.getSku().getSkuCode());
        }

        // Variants
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            List<ProductCreationDto.VariantDto> responseVariantDtos = product.getVariants().stream()
                    .map(variant -> {
                        ProductCreationDto.VariantDto variantDto = new ProductCreationDto.VariantDto();
                        variantDto.setId(variant.getId());
                        variantDto.setName(variant.getName());
                        variantDto.setStockQuantity(variant.getInventoryItem().getStockQuantity());
                        variantDto.setSkuCode(variant.getSku().getSkuCode());
                        variantDto.setPrice(variant.getPrice());
                        // Assuming one Inventory per SKU for simplicity
                        return variantDto;
                    })
                    .collect(Collectors.toList());
            dto.setVariants(responseVariantDtos);
        } else {
            dto.setVariants(variantDtos != null && !variantDtos.isEmpty() ? variantDtos : null);
        }

        // Collection
        dto.setCollection(collectionDto);

        // Images
        if (imageDtos != null && !imageDtos.isEmpty()) {
            dto.setImages(imageDtos);
        } else if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            dto.setImages(product.getProductImages().stream()
                    .map(image -> {
                        ImageDto imageDto = new ImageDto();
                        imageDto.setImageUrl(image.getImageUrl());
                        return imageDto;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}