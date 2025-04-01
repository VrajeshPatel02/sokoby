package com.sokoby.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sokoby.enums.OrderStatus;
import com.sokoby.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "name", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    // Ensuring variants are deleted when product is deleted
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Variant> variants = new ArrayList<>();

    @Column(name = "price", nullable = false)
    private Double price;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(
            name = "product_collection",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Collection> collections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "compared_price")
    private Double comparedPrice;

    // Ensuring SKU is deleted when product is deleted
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JoinColumn(name = "sku_id")
    private SKU sku;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Ensuring product images are deleted when product is deleted
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProductImage> productImages = new ArrayList<>();

    // Ensuring inventory is deleted when product is deleted
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}