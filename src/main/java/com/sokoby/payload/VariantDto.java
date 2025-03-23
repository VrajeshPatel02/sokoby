package com.sokoby.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantDto {
    private UUID id;

    private UUID productId;

    @NotNull
    @Size(min = 1, message = "Name should be at least 1 character")
    private String name;

    @NotNull
    @Min(value = 0, message = "Price must be non-negative")
    private Double price;

    @Size(min = 3, message = "SKU should be at least 3 characters")
    private String sku;

    private Integer stockQuantity;

    private Date createdAt;

    private Date updatedAt;
}