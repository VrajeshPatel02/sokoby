package com.sokoby.service.impl;

import com.sokoby.entity.Inventory;
import com.sokoby.entity.Product;
import com.sokoby.entity.SKU;
import com.sokoby.entity.Variant;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.InventoryMapper;
import com.sokoby.repository.InventoryRepository;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.SKURepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final SKURepository skuRepository;

    @Autowired
    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            VariantRepository variantRepository,
            ProductRepository productRepository,
            SKURepository skuRepository) {
        this.inventoryRepository = inventoryRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
    }

    @Override
    public List<Inventory> getAllInventorys() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public Inventory createInventoryForVariant(UUID variantId, Integer initialStock) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        SKU sku = variant.getSku();
        if (inventoryRepository.findBySku(sku).isPresent()) {
            throw new MerchantException("Inventory already exists for this SKU", "DUPLICATE_INVENTORY");
        }
        Inventory inventory = InventoryMapper.toEntity(sku, initialStock);
        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory createInventoryForProduct(UUID productId, Integer initialStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
        if (product.getSku() == null) {
            throw new MerchantException("Product has no SKU assigned", "NO_SKU_ASSIGNED");
        }
        SKU sku = product.getSku();
        if (inventoryRepository.findBySku(sku).isPresent()) {
            throw new MerchantException("Inventory already exists for this SKU", "DUPLICATE_INVENTORY");
        }
        Inventory inventory = InventoryMapper.toEntity(sku, initialStock);
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory getInventoryById(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
    }

    @Override
    @Transactional
    public Inventory updateInventory(Inventory item) {
        Inventory existing = inventoryRepository.findById(item.getId())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        existing.setStockQuantity(item.getStockQuantity());
        return inventoryRepository.save(existing);
    }

    @Override
    @Transactional
    public void updateStockForVariant(UUID variantId, Integer newStock) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        Inventory inventory = inventoryRepository.findBySku(variant.getSku())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        inventory.setStockQuantity(newStock);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void updateStockForProduct(UUID productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
        if (product.getSku() == null) {
            throw new MerchantException("Product has no SKU assigned", "NO_SKU_ASSIGNED");
        }
        Inventory inventory = inventoryRepository.findBySku(product.getSku())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        inventory.setStockQuantity(newStock);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void deleteInventory(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        inventoryRepository.delete(inventory);
    }

    @Override
    public boolean isAvailable(UUID variantId, int quantity) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        return inventoryRepository.findBySku(variant.getSku())
                .map(inventory -> inventory.getStockQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    public boolean isAvailableForProduct(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
        if (product.getSku() == null) return false;
        return inventoryRepository.findBySku(product.getSku())
                .map(inventory -> inventory.getStockQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public void reserveStock(UUID variantId, int quantity) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        Inventory inventory = inventoryRepository.findBySku(variant.getSku())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        if (inventory.getStockQuantity() < quantity) {
            throw new MerchantException("Insufficient stock to reserve", "INSUFFICIENT_STOCK");
        }
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void reserveStockForProduct(UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
        if (product.getSku() == null) {
            throw new MerchantException("Product has no SKU assigned", "NO_SKU_ASSIGNED");
        }
        Inventory inventory = inventoryRepository.findBySku(product.getSku())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        if (inventory.getStockQuantity() < quantity) {
            throw new MerchantException("Insufficient stock to reserve", "INSUFFICIENT_STOCK");
        }
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void releaseStock(UUID variantId, int quantity) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        Inventory inventory = inventoryRepository.findBySku(variant.getSku())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void releaseStockForProduct(UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
        if (product.getSku() == null) {
            throw new MerchantException("Product has no SKU assigned", "NO_SKU_ASSIGNED");
        }
        Inventory inventory = inventoryRepository.findBySku(product.getSku())
                .orElseThrow(() -> new MerchantException("Inventory not found", "INVENTORY_NOT_FOUND"));
        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);
        inventoryRepository.save(inventory);
    }
}