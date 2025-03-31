package com.sokoby.repository;

import com.sokoby.entity.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {
    List<Collection> findByStoreId(UUID storeId);
    Page<Collection> findByStoreId(UUID storeId, Pageable pageable);
}