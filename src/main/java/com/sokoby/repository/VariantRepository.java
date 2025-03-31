package com.sokoby.repository;

import com.sokoby.entity.Variant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VariantRepository extends JpaRepository<Variant, UUID> {
  List<Variant> findByProductId(UUID productId);

  @Query("SELECT v FROM Variant v  WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
          "OR v.sku.skuCode LIKE LOWER(CONCAT('%', :query, '%'))")
  Page<Variant> searchVariants(@Param("query") String query, Pageable pageable);

//  @Query("SELECT v FROM Variant v WHERE v.product.id = :productId AND " +
//          "(LOWER(v.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
//          "OR v.sku LIKE LOWER(CONCAT('%', :query, '%')))")
//  Page<Variant> searchVariantsByProduct(@Param("productId") UUID productId, @Param("query") String query, Pageable pageable);
  }