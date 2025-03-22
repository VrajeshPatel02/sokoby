package com.sokoby.mapper;


import com.sokoby.entity.Variant;
import com.sokoby.payload.VariantDto;

public class VariantMapper {
    private VariantMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static VariantDto toDto(Variant variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Variant entity cannot be null");
        }

        VariantDto dto = new VariantDto();
        dto.setId(variant.getId());
        dto.setName(variant.getName());
        dto.setPrice(variant.getPrice());
        dto.setSku(variant.getSku());
        dto.setCreatedAt(variant.getCreatedAt());
        dto.setUpdatedAt(variant.getUpdatedAt());
        if (variant.getProduct() != null) {
            dto.setProductId(variant.getProduct().getId());
        }
        return dto;
    }

    public static Variant toEntity(VariantDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("VariantDto cannot be null");
        }

        Variant variant = new Variant();
        variant.setId(dto.getId());
        variant.setName(dto.getName());
        variant.setPrice(dto.getPrice());
        variant.setSku(dto.getSku());
        // Product association handled in service
        return variant;
    }
}