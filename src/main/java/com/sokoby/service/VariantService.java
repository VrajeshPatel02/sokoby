package com.sokoby.service;

import com.sokoby.payload.VariantDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface VariantService {
    VariantDto createVariant(UUID productId, VariantDto dto);

    VariantDto getVariantById(UUID id);

    List<VariantDto> getVariantsByProductId(UUID productId);

    Page<VariantDto> searchVariants(String query, Pageable pageable);

    Page<VariantDto> searchVariantsByProduct(UUID productId, String query, Pageable pageable);

    VariantDto updateVariant(UUID id, VariantDto dto);

    void deleteVariant(UUID id);

    @Transactional
    void reduceStock(UUID id, int quantity);
}