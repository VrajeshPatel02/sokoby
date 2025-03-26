package com.sokoby.service;

import com.sokoby.payload.DiscountDto;

import java.util.List;
import java.util.UUID;

public interface DiscountService {
    DiscountDto createDiscount(DiscountDto dto);
    DiscountDto getDiscountById(UUID id);
    DiscountDto getDiscountByCode(String code);
    List<DiscountDto> getAllDiscounts();
    DiscountDto updateDiscount(UUID id, DiscountDto dto);
    void deleteDiscount(UUID id);
}