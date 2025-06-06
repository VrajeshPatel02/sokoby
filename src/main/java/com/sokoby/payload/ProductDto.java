package com.sokoby.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private UUID id;

    @NotNull
    @Size(min = 2, message = "Name should be at least 2 characters")
    private String title;
    private UUID storeId;
    private List<ImageDto> imageUrls;
    private String description;
    private List<VariantDto> variant;
    private Integer stock;
    private String status;
    private String sku;
    private Double comparedPrice;
    private Double price;
    private List<CollectionDto> collections;
    private Date createdAt;
    private Date updatedAt;
}