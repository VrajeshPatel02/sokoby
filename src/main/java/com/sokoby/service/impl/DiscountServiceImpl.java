package com.sokoby.service.impl;

import com.sokoby.entity.Discount;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.DiscountMapper;
import com.sokoby.payload.DiscountDto;
import com.sokoby.repository.DiscountRepository;
import com.sokoby.service.DiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DiscountServiceImpl implements DiscountService {

    private static final Logger logger = LoggerFactory.getLogger(DiscountServiceImpl.class);

    private final DiscountRepository discountRepository;

    @Autowired
    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    @Transactional
    public DiscountDto createDiscount(DiscountDto dto) {
        validateDiscountInput(dto);

        if (discountRepository.existsByCode(dto.getCode())) {
            throw new MerchantException("Discount code already exists", "DUPLICATE_DISCOUNT_CODE");
        }

        Discount discount = DiscountMapper.toEntity(dto);
        try {
            Discount savedDiscount = discountRepository.save(discount);
            logger.info("Created discount with code: {}", dto.getCode());
            return DiscountMapper.toDto(savedDiscount);
        } catch (Exception e) {
            logger.error("Failed to create discount with code: {} - {}", dto.getCode(), e.getMessage());
            throw new MerchantException("Failed to create discount", "DISCOUNT_CREATION_ERROR");
        }
    }

    @Override
    public DiscountDto getDiscountById(UUID id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Discount not found", "DISCOUNT_NOT_FOUND"));
        logger.info("Retrieved discount with ID: {}", id);
        return DiscountMapper.toDto(discount);
    }

    @Override
    public DiscountDto getDiscountByCode(String code) {
        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new MerchantException("Discount not found for code: " + code, "DISCOUNT_NOT_FOUND"));
        logger.info("Retrieved discount with code: {}", code);
        return DiscountMapper.toDto(discount);
    }

    @Override
    public List<DiscountDto> getAllDiscounts() {
        List<Discount> discounts = discountRepository.findAll();
        return discounts.stream()
                .map(DiscountMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiscountDto updateDiscount(UUID id, DiscountDto dto) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Discount not found", "DISCOUNT_NOT_FOUND"));

        validateDiscountInput(dto);

        // Ensure code uniqueness if changed
        if (!dto.getCode().equals(discount.getCode()) && discountRepository.existsByCode(dto.getCode())) {
            throw new MerchantException("Discount code already exists", "DUPLICATE_DISCOUNT_CODE");
        }

        if (dto.getCode() != null) discount.setCode(dto.getCode());
        if (dto.getDiscountType() != null) discount.setDiscountType(dto.getDiscountType());
        if (dto.getValue() != null) discount.setValue(dto.getValue());
        if (dto.getMinimumOrderAmount() != null) discount.setMinimumOrderAmount(dto.getMinimumOrderAmount());
        if (dto.getValidFrom() != null) discount.setValidFrom(dto.getValidFrom());
        if (dto.getValidUntil() != null) discount.setValidUntil(dto.getValidUntil());
        if (dto.getIsActive() != null) discount.setIsActive(dto.getIsActive());

        try {
            Discount updatedDiscount = discountRepository.save(discount);
            logger.info("Updated discount with ID: {}", id);
            return DiscountMapper.toDto(updatedDiscount);
        } catch (Exception e) {
            logger.error("Failed to update discount with ID: {} - {}", id, e.getMessage());
            throw new MerchantException("Failed to update discount", "DISCOUNT_UPDATE_ERROR");
        }
    }

    @Override
    @Transactional
    public void deleteDiscount(UUID id) {
        if (!discountRepository.existsById(id)) {
            throw new MerchantException("Discount not found", "DISCOUNT_NOT_FOUND");
        }
        try {
            discountRepository.deleteById(id);
            logger.info("Deleted discount with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete discount with ID: {} - {}", id, e.getMessage());
            throw new MerchantException("Failed to delete discount", "DISCOUNT_DELETION_ERROR");
        }
    }

    private void validateDiscountInput(DiscountDto dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new MerchantException("Discount code cannot be null or empty", "INVALID_DISCOUNT_CODE");
        }
        if (dto.getDiscountType() == null || (!dto.getDiscountType().equals("PERCENTAGE") && !dto.getDiscountType().equals("FIXED"))) {
            throw new MerchantException("Discount type must be PERCENTAGE or FIXED", "INVALID_DISCOUNT_TYPE");
        }
        if (dto.getValue() == null || dto.getValue() < 0) {
            throw new MerchantException("Discount value must be non-negative", "INVALID_DISCOUNT_VALUE");
        }
        if (dto.getMinimumOrderAmount() != null && dto.getMinimumOrderAmount() < 0) {
            throw new MerchantException("Minimum order amount cannot be negative", "INVALID_MINIMUM_AMOUNT");
        }
    }
}