package com.sokoby.service;

import com.sokoby.payload.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto dto);

    CategoryDto getCategoryById(UUID id);

    List<CategoryDto> getCategoriesByStoreId(UUID storeId);

    Page<CategoryDto> getCategoriesByStoreId(UUID storeId, Pageable pageable);

    CategoryDto updateCategory(UUID id, CategoryDto dto);

    void deleteCategory(UUID id);
}