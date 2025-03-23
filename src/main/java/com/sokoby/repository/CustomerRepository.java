package com.sokoby.repository;

import com.sokoby.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

  List<Customer> findByStoreId(UUID storeId);

  Page<Customer> findByStoreId(UUID storeId, Pageable pageable);

  Optional<Customer> findByEmailAndStoreId(String email, UUID storeId);

  @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
          "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%'))")
  Page<Customer> searchCustomers(@Param("query") String query, Pageable pageable);

  @Query("SELECT c FROM Customer c WHERE c.store.id = :storeId AND " +
          "(LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
          "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')))")
  Page<Customer> searchCustomersByStore(@Param("storeId") UUID storeId, @Param("query") String query, Pageable pageable);

  boolean existsByEmailAndStoreId(String email, UUID storeId);

  Optional<Customer> findByEmail(String email);
  }