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
    private String name;

    private UUID storeId;

    private String imageUrl;
    private String description;

    private UUID variantId;

    private Integer stock;

    private Double price;
    private List<CategoryDto> categories;
    private Date createdAt;
    private Date updatedAt;
}