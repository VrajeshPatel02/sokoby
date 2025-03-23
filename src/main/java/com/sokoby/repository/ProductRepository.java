package com.sokoby.repository;

import com.sokoby.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByStoreId(UUID storeId);

    // Retrieves all products for a store with pagination
    Page<Product> findByStoreId(UUID storeId, Pageable pageable);

    // Search products globally by name or description with pagination
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    // Search products within a specific store by name or description with pagination
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProductsByStore(@Param("storeId") UUID storeId, @Param("query") String query, Pageable pageable);
}