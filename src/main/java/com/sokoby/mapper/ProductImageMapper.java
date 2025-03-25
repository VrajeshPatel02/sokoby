package com.sokoby.mapper;

import com.sokoby.entity.ProductImage;
import com.sokoby.payload.ImageDto;

public class ProductImageMapper {
    private ProductImageMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

        public static ImageDto toDto(ProductImage image){
            ImageDto dto = new ImageDto();
            dto.setId(image.getId());
            dto.setProductId(image.getProduct().getId());
            dto.setImageUrl(image.getImageUrl());
            return dto;
        }

        public  static ProductImage toEntity(ImageDto dto){
            ProductImage entity = new ProductImage();
            entity.setId(dto.getId());
            entity.setImageUrl(dto.getImageUrl());
            // Set ProductImage in service
            return entity;
        }
}
