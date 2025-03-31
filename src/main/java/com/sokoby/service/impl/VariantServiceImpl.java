package com.sokoby.service.impl;

import com.sokoby.entity.Inventory;
import com.sokoby.entity.Product;
import com.sokoby.entity.SKU;
import com.sokoby.entity.Variant;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.InventoryMapper;
import com.sokoby.mapper.VariantMapper;
import com.sokoby.payload.VariantDto;
import com.sokoby.repository.InventoryRepository;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.SKURepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.VariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final SKURepository skuRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public VariantServiceImpl(
            VariantRepository variantRepository,
            ProductRepository productRepository,
            SKURepository skuRepository,
            InventoryRepository inventoryRepository) {
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public VariantDto createVariant(UUID productId, VariantDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));

        SKU sku = VariantMapper.toSkuEntity(dto);
        sku = skuRepository.save(sku);

        Variant variant = VariantMapper.toEntity(dto, product, sku);
        variant = variantRepository.save(variant);

        if (dto.getStockQuantity() != null) {
            Inventory inventory = InventoryMapper.toEntity(sku, dto.getStockQuantity());
            inventoryRepository.save(inventory);
        }

        Integer stockQuantity = inventoryRepository.findBySku(sku)
                .map(Inventory::getStockQuantity)
                .orElse(null);
        return VariantMapper.toDto(variant, stockQuantity);
    }

    @Override
    public VariantDto getVariantById(UUID id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        Integer stockQuantity = inventoryRepository.findBySku(variant.getSku())
                .map(Inventory::getStockQuantity)
                .orElse(null);
        return VariantMapper.toDto(variant, stockQuantity);
    }

    @Override
    public List<VariantDto> getVariantsByProductId(UUID productId) {
        List<Variant> variants = variantRepository.findByProductId(productId);
        return variants.stream()
                .map(variant -> {
                    Integer stockQuantity = inventoryRepository.findBySku(variant.getSku())
                            .map(Inventory::getStockQuantity)
                            .orElse(null);
                    return VariantMapper.toDto(variant, stockQuantity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<VariantDto> searchVariants(String query, Pageable pageable) {
        // Simplified search (add proper JPA query if needed)
        Page<Variant> variants = variantRepository.searchVariants(query, pageable);
        Page<VariantDto>  variantDto = variants.map(v -> VariantMapper.toDto(v, v.getInventoryItem().getStockQuantity()));
        return variantDto;
    }

    @Override
    public Page<VariantDto> searchVariantsByProduct(UUID productId, String query, Pageable pageable) {
        List<Variant> variants = variantRepository.findByProductId(productId);
        List<VariantDto> dtos = variants.stream()
                .filter(v -> v.getSku().getSkuCode().contains(query))
                .map(v -> {
                    Integer stockQuantity = inventoryRepository.findBySku(v.getSku())
                            .map(Inventory::getStockQuantity)
                            .orElse(null);
                    return VariantMapper.toDto(v, stockQuantity);
                })
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, dtos.size());
    }

    @Override
    @Transactional
    public VariantDto updateVariant(UUID id, VariantDto dto) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        SKU sku = variant.getSku();

        if (dto.getSkuCode() != null && !dto.getSkuCode().equals(sku.getSkuCode())) {
            sku.setSkuCode(dto.getSkuCode());
            skuRepository.save(sku);
        }
        if (dto.getPrice() != null) {
            variant.setPrice(dto.getPrice());
        }
        variantRepository.save(variant);

        if (dto.getStockQuantity() != null) {
            Inventory inventory = inventoryRepository.findBySku(sku)
                    .orElseGet(() -> InventoryMapper.toEntity(sku, 0));
            inventory.setStockQuantity(dto.getStockQuantity());
            inventoryRepository.save(inventory);
        }

        Integer stockQuantity = inventoryRepository.findBySku(sku)
                .map(Inventory::getStockQuantity)
                .orElse(null);
        return VariantMapper.toDto(variant, stockQuantity);
    }

    @Override
    @Transactional
    public void deleteVariant(UUID id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        inventoryRepository.findBySku(variant.getSku()).ifPresent(inventoryRepository::delete);
        skuRepository.delete(variant.getSku());
        variantRepository.delete(variant);
    }

    @Override
    @Transactional
    public void reduceStock(UUID id, int quantity) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        Inventory inventory = inventoryRepository.findBySku(variant.getSku())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        if (inventory.getStockQuantity() < quantity) {
            throw new MerchantException("Insufficient stock", "INSUFFICIENT_STOCK");
        }
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        inventoryRepository.save(inventory);
    }
}