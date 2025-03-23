package com.sokoby.service.impl;

import com.sokoby.entity.Product;
import com.sokoby.entity.Store;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.ProductMapper;
import com.sokoby.payload.ProductDto;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.StoreRepository;
import com.sokoby.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public ProductDto createProduct(UUID storeId, ProductDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new MerchantException("Product name cannot be null or empty", "INVALID_PRODUCT_NAME");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));

        Product product = ProductMapper.toEntity(dto);
        product.setStore(store);

        try {
            Product savedProduct = productRepository.save(product);
            logger.info("Created product {} for store {}", dto.getName(), storeId);
            return ProductMapper.toDto(savedProduct);
        } catch (Exception e) {
            logger.error("Failed to create product for store {}: {}", storeId, e.getMessage());
            throw new MerchantException("Failed to create product", "PRODUCT_CREATION_ERROR");
        }
    }

    @Override
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
        logger.info("Retrieved product with ID: {}", id);
        return ProductMapper.toDto(product);
    }

    @Override
    public List<ProductDto> getProductsByStoreId(UUID storeId) {
        List<Product> products = productRepository.findByStoreId(storeId);
        if (products.isEmpty()) {
            logger.warn("No products found for store ID: {}", storeId);
        }
        return products.stream().map(ProductMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        logger.info("Searching products with query: {}, page: {}, size: {}, sort: {}",
                query, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Product> productPage = (query == null || query.trim().isEmpty())
                ? productRepository.findAll(pageable)
                : productRepository.searchProducts(query, pageable);

        if (productPage.isEmpty()) {
            logger.warn("No products found for query: {}", query);
        }
        return mapToDtoPage(productPage);
    }

    @Override
    public Page<ProductDto> searchProductsByStore(UUID storeId, String query, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new MerchantException("Store not found", "STORE_NOT_FOUND");
        }

        Page<Product> productPage;
        if (query == null || query.trim().isEmpty()) {
            // Use paginated findByStoreId when no query is provided
            productPage = productRepository.findByStoreId(storeId, pageable);
        } else {
            // Use searchProductsByStore for query-based search
            productPage = productRepository.searchProductsByStore(storeId, query, pageable);
        }

        return mapToDtoPage(productPage);
    }

    private Page<ProductDto> mapToDtoPage(Page<Product> productPage) {
        List<ProductDto> dtos = productPage.getContent()
                .stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, productPage.getPageable(), productPage.getTotalElements());
    }

    @Override
    public ProductDto updateProduct(UUID id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        try {
            Product updatedProduct = productRepository.save(product);
            logger.info("Updated product with ID: {}", id);
            return ProductMapper.toDto(updatedProduct);
        } catch (Exception e) {
            logger.error("Failed to update product with ID: {}", id, e);
            throw new MerchantException("Failed to update product", "PRODUCT_UPDATE_ERROR");
        }
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new MerchantException("Product not found", "PRODUCT_NOT_FOUND");
        }
        try {
            productRepository.deleteById(id);
            logger.info("Deleted product with ID: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete product with ID: {}", id, e);
            throw new MerchantException("Failed to delete product", "PRODUCT_DELETION_ERROR");
        }
    }
}