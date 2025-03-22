package com.sokoby.payload;

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
public class StoreDto {
    private UUID id;

    @NotNull
    @Size(min = 2, message = "Name should be at least 2 characters")
    private String name;

    @NotNull
    @Size(min = 3, message = "Domain should be at least 3 characters")
    private String domain;

    private String description;
    private Date createdAt;
    private Date updatedAt;
    private UUID merchantId; // To associate with Merchant without exposing the entity
}