package com.sokoby.service.impl;

import com.sokoby.entity.*;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.CollectionMapper;
import com.sokoby.mapper.ProductCreationMapper;
import com.sokoby.mapper.ProductMapper;
import com.sokoby.payload.CollectionDto;
import com.sokoby.payload.ImageDto;
import com.sokoby.payload.ProductCreationDto;
import com.sokoby.payload.ProductDto;
import com.sokoby.repository.*;
import com.sokoby.service.ImageService;
import com.sokoby.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.sokoby.repository.InventoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ImageService imageService;
    private final VariantRepository variantRepository;
    private final CollectionRepository collectionRepository;
    private final InventoryRepository inventoryRepository;
    private final SKURepository skuRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, StoreRepository storeRepository, ImageService imageService, VariantRepository variantRepository, CollectionRepository collectionRepository, InventoryRepository inventoryRepository, SKURepository skuRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.imageService = imageService;
        this.variantRepository = variantRepository;
        this.collectionRepository = collectionRepository;
        this.inventoryRepository = inventoryRepository;
        this.skuRepository = skuRepository;
    }


    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public ProductDto createProduct(UUID storeId, ProductDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new MerchantException("Product name cannot be null or empty", "INVALID_PRODUCT_NAME");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));

        Product product = ProductMapper.toEntity(dto);
        product.setStore(store);

        try {
            Product savedProduct = productRepository.save(product);
            logger.info("Created product {} for store {}", dto.getTitle(), storeId);
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

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            product.setTitle(dto.getTitle());
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

    @Override
    public ProductDto createProductWithImages(UUID storeId, ProductDto dto, MultipartFile[] files) {

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new MerchantException("Product name cannot be null or empty", "INVALID_PRODUCT_NAME");
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));


        Product product = ProductMapper.toEntity(dto);
        product.setStore(store);


        try {
            Product savedProduct = productRepository.save(product);
            List<ImageDto> imageDto = new ArrayList<>();
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    ImageDto dto1 = imageService.uploadImageFile(file, bucketName, savedProduct.getId());
                    imageDto.add(dto1);
                }
            }
                logger.info("Created product {} for store {}", dto.getTitle(), storeId);
                return ProductMapper.toDtoWithImageDto(savedProduct, imageDto);

            } catch(Exception e){
                logger.error("Failed to create product for store {}: {}", storeId, e.getMessage());
                throw new MerchantException("Failed to create product", "PRODUCT_CREATION_ERROR");
            }
    }

    @Override
    @Transactional
    public ProductCreationDto createProductWithDetails(ProductCreationDto dto, MultipartFile[] files) {
        // Validate mandatory fields
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new MerchantException("Product title is required", "INVALID_PRODUCT_TITLE");
        }
        if (dto.getStoreId() == null) {
            throw new MerchantException("Store ID is required", "INVALID_STORE_ID");
        }
        if (dto.getPrice() == null) {
            throw new MerchantException("Price is required", "INVALID_PRICE");
        }

        // Fetch Store
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));

        // Create Product
        Product product = ProductCreationMapper.toEntity(dto, store);
        product = productRepository.save(product);

        // Handle SKU and Inventory for Product (if no variants)
        if (dto.getVariants() == null || dto.getVariants().isEmpty()) {
            if (dto.getSkuCode() != null || dto.getStockQuantity() != null) {
                SKU sku = ProductCreationMapper.toSkuEntity(dto.getSkuCode());
                sku = skuRepository.save(sku);
                product.setSku(sku);
                if (dto.getStockQuantity() != null) {
                    Inventory inventory = ProductCreationMapper.toInventoryEntity(sku, dto.getStockQuantity());
                    inventoryRepository.save(inventory);
                }
            }
        }

        // Handle Variants (optional)
        List<ProductCreationDto.VariantDto> variantDtos = new ArrayList<>();
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (ProductCreationDto.VariantDto variantDto : dto.getVariants()) {
                Variant variant = ProductCreationMapper.toVariantEntity(variantDto, product);
                SKU sku = ProductCreationMapper.toSkuEntity(variantDto.getSkuCode());
                sku = skuRepository.save(sku);
                variant.setSku(sku);
                variant = variantRepository.save(variant);
                if (variantDto.getStockQuantity() != null) {
                    Inventory inventory = ProductCreationMapper.toInventoryEntity(sku, variantDto.getStockQuantity());
                    inventoryRepository.save(inventory);
                }
                variantDto.setId(variant.getId());
                variantDto.setName(variant.getName());
                variantDtos.add(variantDto);
            }
            product.setVariants(variantRepository.findByProductId(product.getId()));
        }

        // Handle Collection (optional)
        ProductCreationDto.CollectionDto collectionDto = null;
        if (dto.getCollection() != null) {
            if (dto.getCollection().getType() == null || dto.getCollection().getType().trim().isEmpty()) {
                throw new MerchantException("Collection type is required if collection is provided", "INVALID_COLLECTION_TYPE");
            }
            Collection collection = ProductCreationMapper.toCollectionEntity(dto.getCollection(), store, product);
            collection = collectionRepository.save(collection);
            if(product.getCollections() == null){
                product.setCollections(new ArrayList<>());
            }
            product.getCollections().add(collection);
            dto.getCollection().setId(collection.getId());
            collectionDto = dto.getCollection();
        }

        // Handle Images (optional)
        List<ImageDto> imageDtos = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                ImageDto imageDto = imageService.uploadImageFile(file, bucketName, product.getId());
                product.getProductImages().add(ProductCreationMapper.toProductImageEntity(product, imageDto));
                imageDtos.add(imageDto);
            }
        }

        // Save product with all relationships
        productRepository.save(product);

        // Prepare response DTO with all details
        ProductCreationDto responseDto = ProductCreationMapper.toDto(product, variantDtos, collectionDto, imageDtos);
        logger.info("Created product {} with optional details and images", product.getId());
        return responseDto;
    }
}
