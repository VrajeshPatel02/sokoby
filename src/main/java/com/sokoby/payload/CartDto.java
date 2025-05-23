package com.sokoby.payload;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {
    private UUID id;

    @NotNull
    private UUID customerId;

    private List<CartItemDto> cartItems;

    private Date createdAt;
    private Date updatedAt;
}