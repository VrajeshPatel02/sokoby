package com.sokoby.repository;

import com.sokoby.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findFirstByOrderByCreatedAtAsc();
}