package com.sokoby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product; // Optional, for products without variants

    @OneToOne
    @JoinColumn(name = "variant_id")
    private Variant variant; // Optional, for variant-specific items

    @OneToMany(mappedBy = "inventoryItem", cascade = CascadeType.ALL)
    private List<InventoryLevel> inventoryLevels = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    @PreUpdate
    private void validateTarget() {
        if (product == null && variant == null) {
            throw new IllegalStateException("InventoryItem must reference either a product or a variant");
        }
        if (product != null && variant != null) {
            throw new IllegalStateException("InventoryItem cannot reference both a product and a variant");
        }
    }

    // Helper method to get total stock
    public int getTotalStock() {
        return inventoryLevels.stream().mapToInt(InventoryLevel::getQuantity).sum();
    }
}