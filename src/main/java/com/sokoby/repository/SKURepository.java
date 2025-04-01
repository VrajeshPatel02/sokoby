package com.sokoby.repository;

import com.sokoby.entity.SKU;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SKURepository extends JpaRepository<SKU, UUID> {
    Optional<SKU> findByBarcode(String barcode);
    Optional<SKU> findBySkuCode(String skuCode);
}