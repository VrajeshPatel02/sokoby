package com.sokoby.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sokoby.entity.Collection;
import com.sokoby.entity.Inventory;
import com.sokoby.entity.Product;
import com.sokoby.entity.ProductImage;
import com.sokoby.entity.SKU;
import com.sokoby.entity.Store;
import com.sokoby.entity.Variant;
import com.sokoby.enums.CollectionType;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.ProductCreationMapper;
import com.sokoby.mapper.ProductMapper;
import com.sokoby.payload.ImageDto;
import com.sokoby.payload.ProductCreationDto;
import com.sokoby.payload.ProductDto;
import com.sokoby.repository.CollectionRepository;
import com.sokoby.repository.InventoryRepository;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.SKURepository;
import com.sokoby.repository.StoreRepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.ImageService;
import com.sokoby.service.ProductService;

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

//    @Override
//    public ProductDto getProductById(UUID id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
//        logger.info("Retrieved product with ID: {}", id);
//        return ProductMapper.toDto(product);
//    }

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

        if (dto.getSkuCode() != null || dto.getStockQuantity() != null || dto.getBarcode() != null) {
            SKU sku = ProductCreationMapper.toSkuEntity(dto.getSkuCode(), dto.getBarcode(), skuRepository);
            sku = skuRepository.save(sku);
            product.setSku(sku);
            if (dto.getStockQuantity() != null) {
                Inventory inventory = ProductCreationMapper.toInventoryEntity(sku, dto.getStockQuantity());
                inventory = inventoryRepository.save(inventory);
                product.setInventory(inventory);
            }
        }

        // Handle Variants (optional)
        List<ProductCreationDto.VariantDto> variantDtos = new ArrayList<>();
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (ProductCreationDto.VariantDto variantDto : dto.getVariants()) {
                Variant variant = ProductCreationMapper.toVariantEntity(variantDto, product);
                SKU variantSku = ProductCreationMapper.toSkuEntity(variantDto.getSkuCode(), variantDto.getBarcode(), skuRepository);
                variantSku = skuRepository.save(variantSku);
                variant.setSku(variantSku);
                variant = variantRepository.save(variant);
                if (variantDto.getStockQuantity() != null) {
                    Inventory inventory = ProductCreationMapper.toInventoryEntity(variantSku, variantDto.getStockQuantity());
                    inventory = inventoryRepository.save(inventory);
                    variant.setInventoryItem(inventory);
                    variantRepository.save(variant);
                    variantDto.setStockQuantity(inventory.getStockQuantity());
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
            if (product.getCollections() == null) {
                product.setCollections(new ArrayList<>());
            }
            product.getCollections().add(collection);
            dto.getCollection().setId(collection.getId());
            dto.getCollection().setProductType(collection.getProductType());
            dto.getCollection().setType(collection.getType().toString());
            dto.getCollection().setVendor(collection.getVendor());
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


    @Override
    public ProductCreationDto getProductById(UUID productId) {
        // Fetch product with all relationships
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));

        // Create a list of unique images to prevent duplicates
        List<ImageDto> uniqueImageDtos = product.getProductImages() != null 
            ? product.getProductImages().stream()
                .map(image -> {
                    ImageDto imageDto = new ImageDto();
                    imageDto.setId(image.getId());
                    imageDto.setImageUrl(image.getImageUrl());
                    imageDto.setProductId(product.getId());
                    return imageDto;
                })
                .collect(Collectors.toList())
            : new ArrayList<>();

        // Ensure no duplicate image URLs
        uniqueImageDtos = uniqueImageDtos.stream()
            .collect(Collectors.toMap(
                ImageDto::getImageUrl, 
                img -> img, 
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .collect(Collectors.toList());

        // Construct response DTO using existing mapper
        ProductCreationDto responseDto = ProductCreationMapper.toDto(
                product,
                null, // Pass null for variantDtos since mapper will populate from product.variants
                null, // Pass null for collectionDto since mapper will populate from product.collections
                uniqueImageDtos  // Pass unique images
        );

        logger.info("Retrieved product {} with details", product.getId());
        return responseDto;
    }

    @Transactional
    @Override
    public ProductCreationDto updateProductWithDetails(UUID productId, ProductCreationDto dto, MultipartFile[] files) {
        // Fetch existing product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));

        // Validate mandatory fields if provided
        if (dto.getTitle() != null && dto.getTitle().trim().isEmpty()) {
            throw new MerchantException("Product title cannot be empty", "INVALID_PRODUCT_TITLE");
        }
        if (dto.getStoreId() != null) {
            Store store = storeRepository.findById(dto.getStoreId())
                    .orElseThrow(() -> new MerchantException("Store not found", "STORE_NOT_FOUND"));
            product.setStore(store);
        }
        if (dto.getPrice() != null && dto.getPrice() < 0) {
            throw new MerchantException("Price cannot be negative", "INVALID_PRICE");
        }

        // Update basic product fields if provided
        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getStatus() != null) product.setStatus(dto.getStatus());
        if (dto.getComparedPrice() != null) product.setComparedPrice(dto.getComparedPrice());

            if (dto.getSkuCode() != null || dto.getStockQuantity() != null || dto.getBarcode() != null) {
                SKU sku = product.getSku();
                if (sku == null) {
                    sku = ProductCreationMapper.toSkuEntity(dto.getSkuCode(), dto.getBarcode(), skuRepository);
                    sku = skuRepository.save(sku);
                    product.setSku(sku);
                } else {
                    if (dto.getSkuCode() != null && !dto.getSkuCode().equals(sku.getSkuCode())) {
                        sku.setSkuCode(dto.getSkuCode());
                    }
                    if (dto.getBarcode() != null && !dto.getBarcode().equals(sku.getBarcode())) {
                        if (skuRepository.findByBarcode(dto.getBarcode()).isPresent()) {
                            throw new MerchantException("Barcode " + dto.getBarcode() + " already exists", "DUPLICATE_BARCODE");
                        }
                        sku.setBarcode(dto.getBarcode());
                    }
                    skuRepository.save(sku);
                }
                if (dto.getStockQuantity() != null) {
                    Inventory inventory = product.getInventory();
                    if (inventory == null) {
                        inventory = ProductCreationMapper.toInventoryEntity(sku, dto.getStockQuantity());
                        inventory = inventoryRepository.save(inventory);
                        product.setInventory(inventory);
                    } else {
                        inventory.setStockQuantity(dto.getStockQuantity());
                        inventoryRepository.save(inventory);
                    }
                }
            } else {
            // If variants are provided, remove product-level SKU and inventory
            if (product.getSku() != null) {
                if (product.getInventory() != null) {
                    inventoryRepository.delete(product.getInventory());
                    product.setInventory(null);
                }
                skuRepository.delete(product.getSku());
                product.setSku(null);
            }
        }

        // Handle Variants (optional)
        List<ProductCreationDto.VariantDto> variantDtos = new ArrayList<>();
        if (dto.getVariants() != null) {
            // Delete existing variants not in the new list
            List<Variant> existingVariants = product.getVariants();
            List<String> newSkuCodes = dto.getVariants().stream()
                    .map(ProductCreationDto.VariantDto::getSkuCode)
                    .filter(Objects::nonNull)
                    .toList();
            for (Variant existingVariant : existingVariants) {
                if (!newSkuCodes.contains(existingVariant.getSku().getSkuCode())) {
                    if (existingVariant.getInventoryItem() != null) {
                        inventoryRepository.delete(existingVariant.getInventoryItem());
                    }
                    skuRepository.delete(existingVariant.getSku());
                    variantRepository.delete(existingVariant);
                }
            }

            // Update or create variants
            for (ProductCreationDto.VariantDto variantDto : dto.getVariants()) {
                Variant variant = existingVariants.stream()
                        .filter(v -> v.getSku().getSkuCode().equals(variantDto.getSkuCode()))
                        .findFirst()
                        .orElseGet(() -> {
                            Variant newVariant = ProductCreationMapper.toVariantEntity(variantDto, product);
                            SKU newSku = ProductCreationMapper.toSkuEntity(variantDto.getSkuCode(), variantDto.getBarcode(), skuRepository);
                            newSku = skuRepository.save(newSku);
                            newVariant.setSku(newSku);
                            return newVariant;
                        });

                if (variantDto.getPrice() != null) variant.setPrice(variantDto.getPrice());
                if (variantDto.getName() != null) variant.setName(variantDto.getName());
                if (variantDto.getBarcode() != null && !variantDto.getBarcode().equals(variant.getSku().getBarcode())) {
                    if (skuRepository.findByBarcode(variantDto.getBarcode()).isPresent()) {
                        throw new MerchantException("Barcode " + variantDto.getBarcode() + " already exists", "DUPLICATE_BARCODE");
                    }
                    variant.getSku().setBarcode(variantDto.getBarcode());
                    skuRepository.save(variant.getSku());
                }
                variant = variantRepository.save(variant);

                if (variantDto.getStockQuantity() != null) {
                    Inventory inventory = variant.getInventoryItem();
                    if (inventory == null) {
                        inventory = ProductCreationMapper.toInventoryEntity(variant.getSku(), variantDto.getStockQuantity());
                        inventory = inventoryRepository.save(inventory);
                        variant.setInventoryItem(inventory);
                    } else {
                        inventory.setStockQuantity(variantDto.getStockQuantity());
                        inventoryRepository.save(inventory);
                    }
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
            if (dto.getCollection().getType() != null && dto.getCollection().getType().trim().isEmpty()) {
                throw new MerchantException("Collection type cannot be empty", "INVALID_COLLECTION_TYPE");
            }
            Collection collection = product.getCollections().isEmpty()
                    ? ProductCreationMapper.toCollectionEntity(dto.getCollection(), product.getStore(), product)
                    : product.getCollections().get(0);
            if (dto.getCollection().getType() != null) collection.setProductType((dto.getCollection().getProductType()));
            if (dto.getCollection().getVendor() != null) collection.setVendor(dto.getCollection().getVendor());
            if (dto.getCollection().getType() != null) collection.setType(CollectionType.valueOf(dto.getCollection().getType()));
            collection = collectionRepository.save(collection);
            if (product.getCollections().isEmpty()) {
                product.setCollections(new ArrayList<>());
                product.getCollections().add(collection);
            }
            collectionDto = new ProductCreationDto.CollectionDto();
            collectionDto.setId(collection.getId());
            collectionDto.setProductType(collection.getProductType());
            collectionDto.setType(collection.getType().toString());
            collectionDto.setVendor(collection.getVendor());
        }

        // Handle Images (updated logic)
        List<ImageDto> imageDtos = new ArrayList<>();
        List<ProductImage> existingImages = product.getProductImages() != null ? new ArrayList<>(product.getProductImages()) : new ArrayList<>();

        // Delete specified images
        if (dto.getRemoveImageIds() != null && !dto.getRemoveImageIds().isEmpty()) {
            for (UUID removeImageId : dto.getRemoveImageIds()) {
                if (imageService.deleteImage(removeImageId, bucketName, productId)) {
                    existingImages.removeIf(image -> image.getId().equals(removeImageId));
                    logger.info("Deleted image {} for product {}", removeImageId, productId);
                } else {
                    logger.warn("Failed to delete image {} for product {}", removeImageId, productId);
                }
            }
            product.setProductImages(existingImages);
        }

        // Add new images (append to existing)
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                ImageDto imageDto = imageService.uploadImageFile(file, bucketName, product.getId());
                ProductImage newImage = ProductCreationMapper.toProductImageEntity(product, imageDto);
                existingImages.add(newImage);
                imageDtos.add(imageDto);
            }
            product.setProductImages(existingImages);
        } else {
            // Include remaining existing images in the response
            imageDtos = existingImages.stream()
                    .map(image -> {
                        ImageDto imageDto = new ImageDto();
                        imageDto.setImageUrl(image.getImageUrl());
                        return imageDto;
                    })
                    .collect(Collectors.toList());
        }

        // Save updated product with all relationships
        productRepository.save(product);

        // Prepare response DTO
        ProductCreationDto responseDto = ProductCreationMapper.toDto(product, variantDtos, collectionDto, imageDtos);
        logger.info("Updated product {} with details", product.getId());
        return responseDto;
    }

    @Override
    public List<ProductDto> getAllProducts() {
        try {
            List<Product> all = productRepository.findAll();
            return all.stream()
                    .map(ProductMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all products", e);
        }
    }

    @Override
    public List<ProductDto> getProductsByCollection(String collectionType){
        try {
            List<Product> allByCollectionType = productRepository.findAllByCollectionType(CollectionType.valueOf(collectionType.toUpperCase()));
            return allByCollectionType.stream()
                    .map(ProductMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new MerchantException("Collection not Found","COLLECTION_NOT_FOUND");
        }
    }

    @Override
    @Transactional
    public ProductDto updateProductWithMultipart(UUID productId, String productDataJson, List<MultipartFile> newImages) {
        try {
            // Parse JSON to ProductDto
            ObjectMapper objectMapper = new ObjectMapper();
            ProductCreationDto dto = objectMapper.readValue(productDataJson, ProductCreationDto.class);

            // Validate mandatory fields if provided
            if (dto.getTitle() != null && dto.getTitle().trim().isEmpty()) {
                throw new MerchantException("Product title cannot be empty", "INVALID_PRODUCT_TITLE");
            }

            // Fetch existing product
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));

            // Update basic product fields if provided
            if (dto.getTitle() != null) product.setTitle(dto.getTitle());
            if (dto.getDescription() != null) product.setDescription(dto.getDescription());
            if (dto.getPrice() != null) product.setPrice(dto.getPrice());
            if (dto.getStatus() != null) product.setStatus(dto.getStatus());
            if (dto.getComparedPrice() != null) product.setComparedPrice(dto.getComparedPrice());

            // Handle SKU and Inventory
            if (dto.getSkuCode() != null || dto.getStockQuantity() != null || dto.getBarcode() != null) {
                SKU sku = product.getSku();
                if (sku == null) {
                    sku = ProductCreationMapper.toSkuEntity(dto.getSkuCode(), dto.getBarcode(), skuRepository);
                    sku = skuRepository.save(sku);
                    product.setSku(sku);
                } else {
                    if (dto.getSkuCode() != null && !dto.getSkuCode().equals(sku.getSkuCode())) {
                        sku.setSkuCode(dto.getSkuCode());
                    }
                    if (dto.getBarcode() != null && !dto.getBarcode().equals(sku.getBarcode())) {
                        sku.setBarcode(dto.getBarcode());
                    }
                    skuRepository.save(sku);
                }
                if (dto.getStockQuantity() != null) {
                    Inventory inventory = product.getInventory();
                    if (inventory == null) {
                        inventory = ProductCreationMapper.toInventoryEntity(sku, dto.getStockQuantity());
                        inventory = inventoryRepository.save(inventory);
                        product.setInventory(inventory);
                    } else {
                        inventory.setStockQuantity(dto.getStockQuantity());
                        inventoryRepository.save(inventory);
                    }
                }
            }

            // Handle Variants (optional)
            if (dto.getVariants() != null) {
                List<Variant> existingVariants = product.getVariants();
                List<Variant> updatedVariants = new ArrayList<>();

                for (ProductCreationDto.VariantDto variantDto : dto.getVariants()) {
                    Variant variant = existingVariants.stream()
                            .filter(v -> v.getSku().getSkuCode().equals(variantDto.getSkuCode()))
                            .findFirst()
                            .orElseGet(() -> {
                                Variant newVariant = ProductCreationMapper.toVariantEntity(variantDto, product);
                                SKU newSku = ProductCreationMapper.toSkuEntity(variantDto.getSkuCode(), variantDto.getBarcode(), skuRepository);
                                newSku = skuRepository.save(newSku);
                                newVariant.setSku(newSku);
                                return newVariant;
                            });

                    if (variantDto.getPrice() != null) variant.setPrice(variantDto.getPrice());
                    if (variantDto.getName() != null) variant.setName(variantDto.getName());
                    variant = variantRepository.save(variant);

                    if (variantDto.getStockQuantity() != null) {
                        Inventory inventory = variant.getInventoryItem();
                        if (inventory == null) {
                            inventory = ProductCreationMapper.toInventoryEntity(variant.getSku(), variantDto.getStockQuantity());
                            inventory = inventoryRepository.save(inventory);
                            variant.setInventoryItem(inventory);
                        } else {
                            inventory.setStockQuantity(variantDto.getStockQuantity());
                            inventoryRepository.save(inventory);
                        }
                    }
                    updatedVariants.add(variant);
                }

                // Remove variants not in the new list
                existingVariants.stream()
                        .filter(ev -> updatedVariants.stream().noneMatch(uv -> uv.getId().equals(ev.getId())))
                        .forEach(variantRepository::delete);

                product.setVariants(updatedVariants);
            }

            // Handle Images (updated logic)
            List<ProductImage> existingImages = product.getProductImages() != null ? new ArrayList<>(product.getProductImages()) : new ArrayList<>();

            // Delete specified images
            if (dto.getRemoveImageIds() != null && !dto.getRemoveImageIds().isEmpty()) {
                for (UUID removeImageId : dto.getRemoveImageIds()) {
                    if (imageService.deleteImage(removeImageId, bucketName, productId)) {
                        existingImages.removeIf(image -> image.getId().equals(removeImageId));
                        logger.info("Deleted image {} for product {}", removeImageId, productId);
                    } else {
                        logger.warn("Failed to delete image {} for product {}", removeImageId, productId);
                    }
                }
                product.setProductImages(existingImages);
            }

            // Add new images (append to existing)
            if (newImages != null && !newImages.isEmpty()) {
                for (MultipartFile file : newImages) {
                    ImageDto imageDto = imageService.uploadImageFile(file, bucketName, product.getId());
                    ProductImage newImage = ProductCreationMapper.toProductImageEntity(product, imageDto);
                    existingImages.add(newImage);
                }
                product.setProductImages(existingImages);
            }

            // Save updated product with all relationships
            productRepository.save(product);

            // Return basic product DTO
            return ProductMapper.toDto(product);
        } catch (Exception e) {
            logger.error("Failed to update product with multipart data", e);
            throw new MerchantException("Failed to update product", "PRODUCT_UPDATE_ERROR");
        }
    }
}
