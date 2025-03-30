package com.sokoby.mapper;

import com.sokoby.entity.Collection;
import com.sokoby.payload.CollectionDto;

import java.util.stream.Collectors;

public class CollectionMapper {
    private CollectionMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static CollectionDto toDto(Collection category) {
        if (category == null) {
            throw new IllegalArgumentException("Category entity cannot be null");
        }

        CollectionDto dto = new CollectionDto();
        dto.setId(category.getId());
        dto.setStoreId(category.getStore().getId());
        dto.setName(category.getName());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setProducts(category.getProducts().stream().map(ProductMapper::toDto).collect(Collectors.toList()));
        return dto;
    }

    public static Collection toEntity(CollectionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CollectionDto cannot be null");
        }

        Collection category = new Collection();
        category.setId(dto.getId());
        category.setName(dto.getName());
        // Store relationship set in service
        return category;
    }
}