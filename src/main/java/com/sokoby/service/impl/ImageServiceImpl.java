package com.sokoby.service.impl;

import com.sokoby.entity.Product;
import com.sokoby.entity.ProductImage;
import com.sokoby.entity.Store;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.ProductImageMapper;
import com.sokoby.payload.ImageDto;
import com.sokoby.repository.ProductImageRepository;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.StoreRepository;
import com.sokoby.service.BucketService;
import com.sokoby.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final BucketService bucketService;
    private final StoreRepository storeRepository;

    public ImageServiceImpl(ProductImageRepository productImageRepository, ProductRepository productRepository, BucketService bucketService, StoreRepository storeRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.bucketService = bucketService;
        this.storeRepository = storeRepository;
    }

    @Override
    public boolean deleteImage(UUID imageId, String bucketName, UUID productId) {
        Optional<ProductImage> optionalImage = productImageRepository.findById(imageId);
        if (optionalImage.isPresent()) {
            ProductImage image = optionalImage.get();

            // Extract the image URL or key for S3 deletion
            String imageUrl = image.getImageUrl();
            String fileName = extractFileNameFromUrl(imageUrl);

            // Delete the file from S3 bucket
            boolean isDeletedFromBucket = bucketService.deleteFile(fileName, bucketName);

            if (isDeletedFromBucket) {
                // Delete the image record from the database
                productImageRepository.delete(image);
                return true;
            }
        }

        return false; // Return false if the image wasn't found or deletion failed
    }

    @Override
    public ImageDto uploadImageFile(MultipartFile file, String bucketName, UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isPresent()){
            String imageUrl = "";
            try {
                imageUrl = bucketService.uploadFile(file, bucketName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ProductImage image = new ProductImage();
            image.setImageUrl(imageUrl);
            image.setProduct(product.get());
            final ProductImage save = productImageRepository.save(image);
            ImageDto imageDto = new ImageDto();
            imageDto.setId(save.getId());
            imageDto.setImageUrl(save.getImageUrl());
            imageDto.setProductId(save.getProduct().getId());
            return imageDto;
        }
        return null;
    }

    @Override
    public List<ImageDto> getImagesByProduct(UUID productId) {
        List<ProductImage> productImages= productImageRepository.findAllByProductId(productId);
        return productImages.stream().map(ProductImageMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public String uploadStoreLogoFile(MultipartFile logo, String bucketName, UUID storeId) {
        Optional<Store> store = storeRepository.findById(storeId);
        if (store.isPresent()){
            String imageUrl = "";
            try {
                imageUrl = bucketService.uploadFile(logo, bucketName);
                return imageUrl;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw  new MerchantException("Store Not Found", "STORE_NOT_FOUND");
    }

    private String extractFileNameFromUrl(String imageUrl) {
        // Assuming the imageUrl contains the file name as the last part after '/'
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
}
