package com.sokoby.mapper;

import com.sokoby.entity.Category;
import com.sokoby.payload.CategoryDto;

import java.util.stream.Collectors;

public class CategoryMapper {
    private CategoryMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category entity cannot be null");
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setStoreId(category.getStore().getId());
        dto.setName(category.getName());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setProducts(category.getProducts().stream().map(ProductMapper::toDto).collect(Collectors.toList()));
        return dto;
    }

    public static Category toEntity(CategoryDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CategoryDto cannot be null");
        }

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        // Store relationship set in service
        return category;
    }
}