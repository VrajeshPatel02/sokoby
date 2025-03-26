package com.sokoby.repository;

import com.sokoby.entity.InventoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, UUID> {
}