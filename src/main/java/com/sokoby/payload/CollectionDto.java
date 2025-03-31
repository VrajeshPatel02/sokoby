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
public class CollectionDto {
    private UUID id;

    private String type;
    @NotNull
    private UUID storeId;

    private String vendor;

    private List<ProductDto> products;

    private Date createdAt;
    private Date updatedAt;
}