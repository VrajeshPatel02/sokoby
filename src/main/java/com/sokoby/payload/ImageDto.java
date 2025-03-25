package com.sokoby.payload;

import lombok.Data;

import java.util.UUID;
@Data
public class ImageDto {
    private UUID id;
    private String imageUrl;
    private UUID productId;
}
