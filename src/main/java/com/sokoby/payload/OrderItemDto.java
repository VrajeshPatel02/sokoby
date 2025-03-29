package com.sokoby.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private UUID id;

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID variantId;
    private UUID productId;


    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull
    @Min(value = 0, message = "Price must be non-negative")
    private Double price;

    private Double subtotal;

    private Date createdAt;
    private Date updatedAt;
}