package com.sokoby.mapper;

import com.sokoby.entity.Discount;
import com.sokoby.payload.DiscountDto;

public class DiscountMapper {
    private DiscountMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static DiscountDto toDto(Discount discount) {
        if (discount == null) {
            throw new IllegalArgumentException("Discount entity cannot be null for mapping to DTO");
        }

        DiscountDto dto = new DiscountDto();
        dto.setId(discount.getId());
        dto.setCode(discount.getCode());
        dto.setDiscountType(discount.getDiscountType());
        dto.setValue(discount.getValue());
        dto.setMinimumOrderAmount(discount.getMinimumOrderAmount());
        dto.setValidFrom(discount.getValidFrom());
        dto.setValidUntil(discount.getValidUntil());
        dto.setIsActive(discount.getIsActive());
        dto.setCreatedAt(discount.getCreatedAt());
        return dto;
    }

    public static Discount toEntity(DiscountDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DiscountDto cannot be null for mapping to entity");
        }

        Discount discount = new Discount();
        if (dto.getId() != null) {
            discount.setId(dto.getId());
        }
        discount.setCode(dto.getCode());
        discount.setDiscountType(dto.getDiscountType());
        discount.setValue(dto.getValue());
        discount.setMinimumOrderAmount(dto.getMinimumOrderAmount());
        discount.setValidFrom(dto.getValidFrom());
        discount.setValidUntil(dto.getValidUntil());
        discount.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return discount;
    }
}