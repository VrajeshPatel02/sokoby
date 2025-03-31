package com.sokoby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "skus")
@Getter
@Setter
@NoArgsConstructor
public class SKU {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sku_id")
    private UUID id;

    @Column(name = "sku_code", unique = true, nullable = false)
    private String skuCode;

    @OneToOne(mappedBy = "sku")
    private Product product;

    @OneToOne(mappedBy = "sku")
    private Variant variant;

    @OneToOne(mappedBy = "sku", cascade = CascadeType.ALL)
    private Inventory inventory;
}