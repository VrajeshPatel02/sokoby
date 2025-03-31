package com.sokoby.service.impl;

import com.sokoby.entity.Collection;
import com.sokoby.entity.Product;
import com.sokoby.entity.Store;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.CollectionMapper;
import com.sokoby.payload.CollectionDto;
import com.sokoby.payload.ProductDto;
import com.sokoby.repository.CollectionRepository;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.StoreRepository;
import com.sokoby.service.CollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CollectionServiceImpl implements CollectionService {

    private static final Logger logger = LoggerFactory.getLogger(CollectionServiceImpl.class);

    private final CollectionRepository collectionRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CollectionServiceImpl(
            CollectionRepository collectionRepository,
            StoreRepository storeRepository,
            ProductRepository productRepository) {
        this.collectionRepository = collectionRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public CollectionDto createCategory(UUID productId, CollectionDto dto) {
        validateCollectionInput(dto);

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));

        Collection collection = CollectionMapper.toEntity(dto);
        collection.setStore(store);
        collection.setProducts(List.of(product)); // Initialize with the provided product

        try {
            Collection savedCollection = collectionRepository.save(collection);
            product.getCollections().add(savedCollection); // Update the product's collections
            productRepository.save(product);
            logger.info("Created collection with type: {} for store: {} and product: {}", dto.getType(), dto.getStoreId(), productId);
            return CollectionMapper.toDto(savedCollection);
        } catch (Exception e) {
            logger.error("Failed to create collection: {}", e.getMessage());
            throw new MerchantException("Failed to create collection", "COLLECTION_CREATION_ERROR");
        }
    }

    @Override
    public CollectionDto getCategoryById(UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Collection not found", "COLLECTION_NOT_FOUND"));
        logger.info("Retrieved collection with ID: {}", id);
        return CollectionMapper.toDto(collection);
    }

    @Override
    public List<CollectionDto> getCategoriesByStoreId(UUID storeId) {

        List<Collection> collections = collectionRepository.findByStoreId(storeId);
        if (collections.isEmpty()) {
            logger.warn("No collections found for store ID: {}", storeId);
        }
        return collections.stream()
                .map(CollectionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CollectionDto> getCategoriesByStoreId(UUID storeId, Pageable pageable) {
        Page<Collection> collectionPage = collectionRepository.findByStoreId(storeId, pageable);
        if (collectionPage.isEmpty()) {
            logger.warn("No collections found for store ID: {}", storeId);
        }
        return collectionPage.map(CollectionMapper::toDto);
    }

    @Override
    @Transactional
    public CollectionDto updateCategory(UUID id, CollectionDto dto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Collection not found", "COLLECTION_NOT_FOUND"));

        if (dto.getType() != null && !dto.getType().trim().isEmpty()) {
            collection.setType(dto.getType());
        }
        if (dto.getVendor() != null) {
            collection.setVendor(dto.getVendor());
        }
        if (dto.getProducts() != null && !dto.getProducts().isEmpty()) {
            List<UUID> productIds = dto.getProducts().stream()
                    .map(ProductDto::getId)
                    .collect(Collectors.toList());
            List<Product> products = productRepository.findAllById(productIds);
            if (products.size() != productIds.size()) {
                throw new MerchantException("One or more products not found", "PRODUCTS_NOT_FOUND");
            }
            collection.setProducts(products);
        }

        try {
            Collection updatedCollection = collectionRepository.save(collection);
            logger.info("Updated collection with ID: {}", id);
            return CollectionMapper.toDto(updatedCollection);
        } catch (Exception e) {
            logger.error("Failed to update collection with ID: {}: {}", id, e.getMessage());
            throw new MerchantException("Failed to update collection", "COLLECTION_UPDATE_ERROR");
        }
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Collection not found", "COLLECTION_NOT_FOUND"));
        try {
            // Remove collection from associated products
            collection.getProducts().forEach(product -> product.getCollections().remove(collection));
            productRepository.saveAll(collection.getProducts());
            collectionRepository.delete(collection);
            logger.info("Deleted collection with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete collection with ID: {}: {}", id, e.getMessage());
            throw new MerchantException("Failed to delete collection", "COLLECTION_DELETION_ERROR");
        }
    }

    private void validateCollectionInput(CollectionDto dto) {
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            throw new MerchantException("Collection type cannot be null or empty", "INVALID_COLLECTION_TYPE");
        }
        if (dto.getStoreId() == null) {
            throw new MerchantException("Store ID cannot be null", "INVALID_STORE_ID");
        }
    }
}