package com.sokoby.service;

import com.sokoby.payload.ReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    ReviewDto createReview(ReviewDto dto);

    ReviewDto getReviewById(UUID id);

    List<ReviewDto> getReviewsByProductId(UUID productId);

    Page<ReviewDto> getReviewsByProductId(UUID productId, Pageable pageable);

    List<ReviewDto> getReviewsByCustomerId(UUID customerId);

    Page<ReviewDto> getReviewsByCustomerId(UUID customerId, Pageable pageable);

    ReviewDto updateReview(UUID id, ReviewDto dto);

    void deleteReview(UUID id);
}