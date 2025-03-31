package com.sokoby.mapper;

import com.sokoby.entity.Product;
import com.sokoby.entity.SKU;
import com.sokoby.entity.Variant;
import com.sokoby.payload.VariantDto;

import java.util.UUID;

public class VariantMapper {

    private VariantMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Variant toEntity(VariantDto dto, Product product, SKU sku) {
        Variant variant = new Variant();
        variant.setProduct(product);
        variant.setSku(sku);
        variant.setPrice(dto.getPrice() != null ? dto.getPrice() : product.getPrice());
        return variant;
    }

    public static VariantDto toDto(Variant variant, Integer stockQuantity) {
        VariantDto dto = new VariantDto();
        dto.setVariantId(variant.getId());
        dto.setProductId(variant.getProduct().getId());
        dto.setSkuCode(variant.getSku().getSkuCode());
        dto.setPrice(variant.getPrice());
        dto.setStockQuantity(stockQuantity); // Fetched from Inventory
        return dto;
    }

    public static SKU toSkuEntity(VariantDto dto) {
        SKU sku = new SKU();
        sku.setSkuCode(dto.getSkuCode() != null ? dto.getSkuCode() : generateSkuCode("VARIANT"));
        return sku;
    }

    private static String generateSkuCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}