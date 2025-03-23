package com.sokoby.service;

import com.sokoby.payload.DiscountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DiscountService {
    DiscountDto createDiscount(UUID storeId, DiscountDto discountDto) throws Exception;
    
    DiscountDto getDiscountById(UUID id);
    
    DiscountDto getDiscountByCode(String code);
    
    List<DiscountDto> getDiscountsByStoreId(UUID storeId);
    
    DiscountDto updateDiscount(UUID id, DiscountDto discountDto) throws Exception;
    
    void deleteDiscount(UUID id);
    
    Page<DiscountDto> searchDiscounts(String query, Pageable pageable);
} 