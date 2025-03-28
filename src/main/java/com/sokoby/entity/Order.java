package com.sokoby.entity;

import com.sokoby.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    private Address shippingAddress;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "subtotal")
    private Double subtotal;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    public void addOrderItem(OrderItem item) {
        if (item != null) {
            item.setOrder(this);
            this.orderItems.add(item);
            calculateTotals(); // Recalculate totals after adding item
        }
    }

    public void calculateTotals() {
        this.subtotal = orderItems.stream()
                .mapToDouble(item -> {
                    if (item.getVariant() != null) {
                        return item.getVariant().getPrice() * item.getQuantity();
                    } else {
                        return item.getProduct().getPrice() * item.getQuantity();
                    }
                })
                .sum();
        this.discountAmount = discount != null ? discount.calculateDiscount(subtotal) : 0.0;
        this.totalAmount = subtotal - discountAmount;
    }
}