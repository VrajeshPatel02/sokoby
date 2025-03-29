package com.sokoby.service.impl;

import com.sokoby.entity.InventoryItem;
import com.sokoby.entity.InventoryLevel;
import com.sokoby.entity.Product;
import com.sokoby.entity.Variant;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.InventoryItemRepository;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    @Autowired
    public InventoryServiceImpl(InventoryItemRepository inventoryItemRepository,
                                ProductRepository productRepository,
                                VariantRepository variantRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryItemRepository.findAll();
    }

    @Override
    @Transactional
    public InventoryItem createInventoryItemForVariant(UUID variantId, Integer initialStock) {
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
        if (inventoryItemRepository.findByVariantId(variantId).isPresent()) {
            throw new MerchantException("Inventory item already exists for variant: " + variantId, "DUPLICATE_INVENTORY_ITEM");
        }

        InventoryItem item = new InventoryItem();
        item.setSku(generateSkuForVariant(variant));
        item.setVariant(variant);

        InventoryLevel level = new InventoryLevel();
        level.setInventoryItem(item);
        level.setLocationId(UUID.randomUUID()); // Placeholder; adjust for real location logic
        level.setQuantity(initialStock != null ? initialStock : 0);
        level.setAvailableQuantity(variant.getStockQuantity());
        item.getInventoryLevels().add(level);

        return inventoryItemRepository.save(item);
    }

    @Override
    @Transactional
    public InventoryItem createInventoryItemForProduct(UUID productId, Integer initialStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
        if (inventoryItemRepository.findByProductId(productId).isPresent()) {
            throw new MerchantException("Inventory item already exists for product: " + productId, "DUPLICATE_INVENTORY_ITEM");
        }

        InventoryItem item = new InventoryItem();
        item.setSku(generateSkuForProduct(product));
        item.setProduct(product);

        InventoryLevel level = new InventoryLevel();
        level.setInventoryItem(item);
        level.setLocationId(UUID.randomUUID()); // Placeholder; adjust for real location logic
        level.setQuantity(initialStock != null ? initialStock : 0);
        level.setAvailableQuantity(product.getStock());
        item.getInventoryLevels().add(level);

        return inventoryItemRepository.save(item);
    }

    @Override
    public InventoryItem getInventoryItemById(UUID id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Inventory item not found", "INVENTORY_NOT_FOUND"));
    }

    @Override
    @Transactional
    public InventoryItem updateInventoryItem(InventoryItem item) {
        InventoryItem existing = inventoryItemRepository.findById(item.getId())
                .orElseThrow(() -> new MerchantException("Inventory item not found", "INVENTORY_NOT_FOUND"));
        existing.setSku(item.getSku());
        // Note: product/variant relationships shouldn’t change; update levels separately if needed
        return inventoryItemRepository.save(existing);
    }

    @Override
    @Transactional
    public void updateStockForVariant(UUID variantId, Integer newStock) {
        InventoryItem item = inventoryItemRepository.findByVariantId(variantId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for variant: " + variantId, "INVENTORY_NOT_FOUND"));
        item.getInventoryLevels().stream()
                .findFirst()
                .ifPresent(level -> level.setQuantity(newStock));
        inventoryItemRepository.save(item);
    }

    @Override
    @Transactional
    public void updateStockForProduct(UUID productId, Integer newStock) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for product: " + productId, "INVENTORY_NOT_FOUND"));
        item.getInventoryLevels().stream()
                .findFirst()
                .ifPresent(level -> level.setQuantity(newStock));
        inventoryItemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteInventoryItem(UUID id) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Inventory item not found", "INVENTORY_NOT_FOUND"));
        inventoryItemRepository.delete(item);
    }

    @Override
    public boolean isAvailable(UUID variantId, int quantity) {
        InventoryItem item = inventoryItemRepository.findByVariantId(variantId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for variant: " + variantId, "INVENTORY_NOT_FOUND"));
        return item.getTotalStock() >= quantity;
    }

    @Override
    public boolean isAvailableForProduct(UUID productId, Integer quantity) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for product: " + productId, "INVENTORY_NOT_FOUND"));
        return item.getTotalStock() >= quantity;
    }

    @Override
    @Transactional
    public void reserveStock(UUID variantId, int quantity) {
        InventoryItem item = inventoryItemRepository.findByVariantId(variantId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for variant: " + variantId, "INVENTORY_NOT_FOUND"));
        int totalStock = item.getTotalStock();
        if (totalStock < quantity) {
            throw new MerchantException("Insufficient stock for variant: " + variantId, "INSUFFICIENT_STOCK");
        }
        item.getInventoryLevels().stream()
                .filter(level -> level.getQuantity() >= quantity)
                .findFirst()
                .ifPresent(level -> {
                    level.setQuantity(level.getQuantity() - quantity);
                    inventoryItemRepository.save(item);
                });
    }

    @Override
    @Transactional
    public void reserveStockForProduct(UUID productId, int quantity) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for product: " + productId, "INVENTORY_NOT_FOUND"));
        int totalStock = item.getTotalStock();
        if (totalStock < quantity) {
            throw new MerchantException("Insufficient stock for product: " + productId, "INSUFFICIENT_STOCK");
        }
        item.getInventoryLevels().stream()
                .filter(level -> level.getQuantity() >= quantity)
                .findFirst()
                .ifPresent(level -> {
                    level.setQuantity(level.getQuantity() - quantity);
                    inventoryItemRepository.save(item);
                });
    }

    @Override
    @Transactional
    public void releaseStock(UUID variantId, int quantity) {
        InventoryItem item = inventoryItemRepository.findByVariantId(variantId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for variant: " + variantId, "INVENTORY_NOT_FOUND"));
        item.getInventoryLevels().stream()
                .findFirst()
                .ifPresent(level -> {
                    level.setQuantity(level.getQuantity() + quantity);
                    inventoryItemRepository.save(item);
                });
    }

    @Override
    @Transactional
    public void releaseStockForProduct(UUID productId, int quantity) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new MerchantException("Inventory item not found for product: " + productId, "INVENTORY_NOT_FOUND"));
        item.getInventoryLevels().stream()
                .findFirst()
                .ifPresent(level -> {
                    level.setQuantity(level.getQuantity() + quantity);
                    inventoryItemRepository.save(item);
                });
    }

    // Helper method for SKU generation (simplified)
    private String generateSkuForVariant(Variant variant) {
        return "VAR-" + variant.getId().toString().substring(0, 8);
    }

    private String generateSkuForProduct(Product product) {
        return "PROD-" + product.getId().toString().substring(0, 8);
    }
}