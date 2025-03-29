package com.sokoby.repository;

import com.sokoby.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    Optional<InventoryItem> findByProductId(UUID productId);
    Optional<InventoryItem> findByVariantId(UUID variantId);
    boolean existsByProductId(UUID productId);
    boolean existsByVariantId(UUID variantId);
}