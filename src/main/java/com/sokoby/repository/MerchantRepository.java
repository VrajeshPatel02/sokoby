package com.sokoby.repository;

import com.sokoby.entity.Merchant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    Optional<Merchant> findByEmail(String email);

    boolean existsByEmail(@NotNull @Email(message = "Email should be valid") String email);

    @Query("SELECT m FROM Merchant m WHERE " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(m.firstName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<Merchant> searchMerchants(@Param("searchText") String query, Pageable pageable);
}