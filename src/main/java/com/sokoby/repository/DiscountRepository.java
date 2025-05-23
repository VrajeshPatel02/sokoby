package com.sokoby.repository;

import com.sokoby.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscountRepository extends JpaRepository<Discount, UUID> {
  Optional<Discount> findByCode(String code);

    boolean existsByCode(String code);
}