package com.sokoby.service.impl;

import com.sokoby.entity.Customer;
import com.sokoby.entity.Order;
import com.sokoby.entity.Product;
import com.sokoby.entity.Review;
import com.sokoby.entity.Variant;
import com.sokoby.exception.MerchantException;
import com.sokoby.mapper.ReviewMapper;
import com.sokoby.payload.ReviewDto;
import com.sokoby.repository.CustomerRepository;
import com.sokoby.repository.OrderRepository;
import com.sokoby.repository.ProductRepository;
import com.sokoby.repository.ReviewRepository;
import com.sokoby.repository.VariantRepository;
import com.sokoby.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, OrderRepository orderRepository,
                             CustomerRepository customerRepository, ProductRepository productRepository,
                             VariantRepository variantRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        validateReviewInput(reviewDto);

        Order order = orderRepository.findById(reviewDto.getOrderId())
                .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));
        Customer customer = customerRepository.findById(reviewDto.getCustomerId())
                .orElseThrow(() -> new MerchantException("Customer not found", "CUSTOMER_NOT_FOUND"));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new MerchantException("Customer did not place this order", "UNAUTHORIZED_REVIEW");
        }

        Review review = ReviewMapper.toEntity(reviewDto);
        review.setOrder(order);
        review.setCustomer(customer);

        if (reviewDto.getProductId() != null) {
            Product product = productRepository.findById(reviewDto.getProductId())
                    .orElseThrow(() -> new MerchantException("Product not found", "PRODUCT_NOT_FOUND"));
            if (order.getOrderItems().stream().noneMatch(item ->
                    (item.getVariant() == null && item.getProduct().getId().equals(product.getId())) ||
                            (item.getVariant() != null && item.getVariant().getProduct().getId().equals(product.getId())))) {
                throw new MerchantException("Product not purchased in this order", "INVALID_PRODUCT_REVIEW");
            }
            review.setProduct(product);
        } else if (reviewDto.getVariantId() != null) {
            Variant variant = variantRepository.findById(reviewDto.getVariantId())
                    .orElseThrow(() -> new MerchantException("Variant not found", "VARIANT_NOT_FOUND"));
            if (order.getOrderItems().stream().noneMatch(item -> item.getVariant() != null &&
                    item.getVariant().getId().equals(variant.getId()))) {
                throw new MerchantException("Variant not purchased in this order", "INVALID_VARIANT_REVIEW");
            }
            review.setVariant(variant);
        } else {
            throw new MerchantException("Must specify either productId or variantId", "INVALID_REVIEW_TARGET");
        }

        Review savedReview = reviewRepository.save(review);
        logger.info("Created review {} for order {}", savedReview.getId(), order.getId());
        return ReviewMapper.toDto(savedReview);
    }

    @Override
    public ReviewDto getReviewById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Review not found", "REVIEW_NOT_FOUND"));
        logger.info("Retrieved review with ID: {}", id);
        return ReviewMapper.toDto(review);
    }

    @Override
    public List<ReviewDto> getReviewsByProductId(UUID productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        logger.info("Retrieved {} reviews for product {}", reviews.size(), productId);
        return reviews.stream().map(ReviewMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByVariantId(UUID variantId) {
        List<Review> reviews = reviewRepository.findByVariantId(variantId);
        logger.info("Retrieved {} reviews for variant {}", reviews.size(), variantId);
        return reviews.stream().map(ReviewMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDto> getReviewsByCustomerId(UUID customerId) {
        List<Review> reviews = reviewRepository.findByCustomerId(customerId);
        logger.info("Retrieved {} reviews for customer {}", reviews.size(), customerId);
        return reviews.stream().map(ReviewMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewDto updateReview(UUID id, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Review not found", "REVIEW_NOT_FOUND"));

        validateReviewInput(reviewDto);

        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review updatedReview = reviewRepository.save(review);
        logger.info("Updated review with ID: {}", id);
        return ReviewMapper.toDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new MerchantException("Review not found", "REVIEW_NOT_FOUND"));

        reviewRepository.delete(review);
        logger.info("Deleted review with ID: {}", id);
    }

    private void validateReviewInput(ReviewDto dto) {
        if (dto.getOrderId() == null) throw new MerchantException("Order ID cannot be null", "INVALID_ORDER_ID");
        if (dto.getCustomerId() == null) throw new MerchantException("Customer ID cannot be null", "INVALID_CUSTOMER_ID");
        if (dto.getProductId() == null && dto.getVariantId() == null) {
            throw new MerchantException("Must specify either productId or variantId", "INVALID_REVIEW_TARGET");
        }
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new MerchantException("Rating must be between 1 and 5", "INVALID_RATING");
        }
    }
}