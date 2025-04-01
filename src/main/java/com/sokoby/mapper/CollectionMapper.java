package com.sokoby.mapper;

import com.sokoby.entity.Collection;
import com.sokoby.payload.CollectionDto;

import java.util.stream.Collectors;

public class CollectionMapper {
    private CollectionMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static CollectionDto toDto(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection entity cannot be null");
        }

        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setStoreId(collection.getStore().getId());
        dto.setType(collection.getType());
        dto.setVendor(collection.getVendor());
        dto.setCreatedAt(collection.getCreatedAt());
        dto.setUpdatedAt(collection.getUpdatedAt());
//        dto.setProducts(collection.getProducts().stream().map(ProductMapper::toDto).collect(Collectors.toList()));
        return dto;
    }

    public static Collection toEntity(CollectionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CollectionDto cannot be null");
        }

        Collection collection = new Collection();
        collection.setId(dto.getId());
        collection.setType(dto.getType());
        collection.setVendor(dto.getVendor());
        // Store relationship set in service
        return collection;
    }
}