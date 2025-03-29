package com.sokoby.controller;

import com.sokoby.payload.ReviewDto;
import com.sokoby.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody ReviewDto reviewDto) {
        ReviewDto createdReview = reviewService.createReview(reviewDto);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable UUID id) {
        ReviewDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByProductId(@PathVariable UUID productId) {
        List<ReviewDto> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByVariantId(@PathVariable UUID variantId) {
        List<ReviewDto> reviews = reviewService.getReviewsByVariantId(variantId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByCustomerId(@PathVariable UUID customerId) {
        List<ReviewDto> reviews = reviewService.getReviewsByCustomerId(customerId);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable UUID id, @RequestBody ReviewDto reviewDto) {
        ReviewDto updatedReview = reviewService.updateReview(id, reviewDto);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}