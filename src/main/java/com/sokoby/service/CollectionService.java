package com.sokoby.service;

import com.sokoby.payload.CollectionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CollectionService {
    CollectionDto createCategory(CollectionDto dto);

    CollectionDto getCategoryById(UUID id);

    List<CollectionDto> getCategoriesByStoreId(UUID storeId);

    Page<CollectionDto> getCategoriesByStoreId(UUID storeId, Pageable pageable);

    CollectionDto updateCategory(UUID id, CollectionDto dto);

    void deleteCategory(UUID id);
}