package com.sokoby.service.impl;

import com.sokoby.entity.InventoryItem;
import com.sokoby.entity.Product;
import com.sokoby.entity.Variant;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.VariantMapper;
import com.sokoby.payload.VariantDto;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.InventoryService;
import com.sokoby.service.VariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VariantServiceImpl implements VariantService {
    private static final Logger logger = LoggerFactory.getLogger(VariantServiceImpl.class);
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9-]+$");

    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Autowired
    public VariantServiceImpl(VariantRepository variantRepository,
                              ProductRepository productRepository,
                              InventoryService inventoryService) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "variants", key = "#productId")
    public VariantDto createVariant(UUID productId, VariantDto dto) {
        validateVariantInput(dto);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));

        Variant variant = VariantMapper.toEntity(dto);
        variant.setProduct(product);

        try {
            Variant savedVariant = variantRepository.save(variant);

            // Create an InventoryItem for the variant
            InventoryItem inventoryItem = inventoryService.createInventoryItemForVariant(savedVariant.getId(), dto.getStockQuantity());

            savedVariant.setInventoryItem(inventoryItem);
            Variant saved = variantRepository.save(savedVariant);
            logger.info("Created variant {} for product {}", dto.getName(), productId);
            return VariantMapper.toDto(saved);
        } catch (Exception e) {
            logger.error("Failed to create variant for product {}: {}", productId, e.getMessage());
            throw new MerchantException("Failed to create variant", "VARIANT_CREATION_ERROR");
        }
    }

    @Override
    @Cacheable(value = "variants", key = "#id")
    public VariantDto getVariantById(UUID id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        logger.info("Retrieved variant with ID: {}", id);
        return VariantMapper.toDto(variant);
    }

    @Override
    @Cacheable(value = "variants", key = "'product_' + #productId")
    public List<VariantDto> getVariantsByProductId(UUID productId) {
        List<Variant> variants = variantRepository.findByProductId(productId);
        if (variants.isEmpty()) {
            logger.warn("No variants found for product ID: {}", productId);
        }
        return variants.stream().map(VariantMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<VariantDto> searchVariants(String query, Pageable pageable) {
        logger.info("Searching variants with query: {}, page: {}, size: {}, sort: {}",
                query, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Variant> variantPage = (query == null || query.trim().isEmpty())
                ? variantRepository.findAll(pageable)
                : variantRepository.searchVariants(query, pageable);

        if (variantPage.isEmpty()) {
            logger.warn("No variants found for query: {}", query);
        }
        return mapToDtoPage(variantPage);
    }

    @Override
    public Page<VariantDto> searchVariantsByProduct(UUID productId, String query, Pageable pageable) {
        logger.info("Searching variants in product {} with query: {}, page: {}, size: {}, sort: {}",
                productId, query, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        if (!productRepository.existsById(productId)) {
            throw new MerchantException("Product not found", "PRODUCT_NOT_FOUND");
        }

        Page<Variant> variantPage = (query == null || query.trim().isEmpty())
                ? variantRepository.findByProductId(productId, pageable)
                : variantRepository.searchVariantsByProduct(productId, query, pageable);

        if (variantPage.isEmpty()) {
            logger.warn("No variants found for product {} with query: {}", productId, query);
        }
        return mapToDtoPage(variantPage);
    }

    private Page<VariantDto> mapToDtoPage(Page<Variant> variantPage) {
        List<VariantDto> dtos = variantPage.getContent()
                .stream()
                .map(VariantMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, variantPage.getPageable(), variantPage.getTotalElements());
    }

    @Override
    @Transactional
    @CacheEvict(value = "variants", key = "#id")
    public VariantDto updateVariant(UUID id, VariantDto dto) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            variant.setName(dto.getName());
        }
        if (dto.getPrice() != null && dto.getPrice() >= 0) {
            variant.setPrice(dto.getPrice());
        }
        if (dto.getStockQuantity() != null && dto.getStockQuantity() >= 0) {
            inventoryService.updateStockForVariant(id, dto.getStockQuantity());
        }
        if (dto.getSku() != null && !dto.getSku().equals(variant.getSku())) {
            validateSku(dto.getSku());
            variant.setSku(dto.getSku());
        }

        try {
            Variant updatedVariant = variantRepository.save(variant);
            logger.info("Updated variant with ID: {}", id);
            return VariantMapper.toDto(updatedVariant);
        } catch (Exception e) {
            logger.error("Failed to update variant with ID: {}", id, e);
            throw new MerchantException("Failed to update variant", "VARIANT_UPDATE_ERROR");
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "variants", key = "#id")
    public void deleteVariant(UUID id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        try {
            variantRepository.deleteById(id);
            // Optionally delete InventoryItem if required
            logger.info("Deleted variant with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete variant with ID: {}", id, e);
            throw new MerchantException("Failed to delete variant", "VARIANT_DELETION_ERROR");
        }
    }

    @Override
    @Transactional
    public void reduceStock(UUID id, int quantity) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        if (!inventoryService.isAvailable(id, quantity)) {
            throw new MerchantException("Insufficient stock", "INSUFFICIENT_STOCK");
        }
        inventoryService.reserveStock(id, quantity);
        logger.info("Reduced stock for variant {} by {}", id, quantity);
    }

    private void validateVariantInput(VariantDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new MerchantException("Variant name cannot be null or empty", "INVALID_VARIANT_NAME");
        }
        if (dto.getPrice() == null || dto.getPrice() < 0) {
            throw new MerchantException("Price must be provided and non-negative", "INVALID_PRICE");
        }
        if (dto.getStockQuantity() == null || dto.getStockQuantity() < 0) {
            throw new MerchantException("Stock quantity must be non-negative", "INVALID_STOCK");
        }
        if (dto.getSku() != null) {
            validateSku(dto.getSku());
        }
    }

    private void validateSku(String sku) {
        if (!SKU_PATTERN.matcher(sku).matches()) {
            throw new MerchantException("Invalid SKU format (alphanumeric and hyphens only)", "INVALID_SKU");
        }
        if (variantRepository.existsBySku(sku)) {
            throw new MerchantException("SKU already exists", "DUPLICATE_SKU");
        }
    }
}