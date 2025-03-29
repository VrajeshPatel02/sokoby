package com.sokoby.service;

import com.sokoby.payload.ReviewDto;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    ReviewDto createReview(ReviewDto reviewDto);
    ReviewDto getReviewById(UUID id);
    List<ReviewDto> getReviewsByProductId(UUID productId);
    List<ReviewDto> getReviewsByVariantId(UUID variantId);
    List<ReviewDto> getReviewsByCustomerId(UUID customerId);
    ReviewDto updateReview(UUID id, ReviewDto reviewDto);
    void deleteReview(UUID id);
}