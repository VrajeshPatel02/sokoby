package com.sokoby.mapper;

import com.sokoby.entity.Review;
import com.sokoby.payload.ReviewDto;

public class ReviewMapper {
    private ReviewMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static ReviewDto toDto(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review entity cannot be null for mapping to DTO");
        }

        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrder().getId());
        dto.setCustomerId(review.getCustomer().getId());
        dto.setProductId(review.getProduct() != null ? review.getProduct().getId() : null);
        dto.setVariantId(review.getVariant() != null ? review.getVariant().getId() : null);
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

    public static Review toEntity(ReviewDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ReviewDto cannot be null for mapping to entity");
        }

        Review review = new Review();
        review.setId(dto.getId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return review;
    }
}