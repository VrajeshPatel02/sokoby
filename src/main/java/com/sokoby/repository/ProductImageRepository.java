package com.sokoby.repository;

import com.sokoby.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
  List<ProductImage> findAllByProductId(UUID productId);
}