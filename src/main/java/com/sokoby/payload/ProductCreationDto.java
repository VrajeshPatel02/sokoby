package com.sokoby.payload;

import com.sokoby.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductCreationDto {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    @NotNull(message = "Price is required")
    private Double price;

    private ProductStatus status;

    private Double comparedPrice;

    private String skuCode; // Optional SKU for product without variants

    private Integer stockQuantity; // Optional stock for product without variants

    private List<VariantDto> variants;

    private CollectionDto collection;

    private List<ImageDto> images;

    @Data
    public static class VariantDto {
        private UUID id;
        private String name;
        private String skuCode;
        private Double price;
        private Integer stockQuantity;
    }

    @Data
    public static class CollectionDto {
        @NotBlank(message = "Type is required if collection is provided")
        private UUID id;
        private String type;
        private String vendor;
    }
}