package com.sokoby.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class ReviewDto {
    private UUID id;
    private UUID orderId;
    private UUID customerId;
    private UUID productId; // Optional
    private UUID variantId; // Optional
    private Integer rating;
    private String comment;
    private Date createdAt;
}