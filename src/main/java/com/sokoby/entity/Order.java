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
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>(); // Changed to One-to-Many

    @Embedded
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal = 0.0; // Sum of order item subtotals

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0; // Amount discounted

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount = 0.0; // Final amount after discount

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        if (this.status == null) this.status = OrderStatus.PLACED;
        calculateTotals();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
        calculateTotals();
    }

    // Method to calculate subtotal and total amount
    public void calculateTotals() {
        this.subtotal = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
        this.discountAmount = (discount != null) ? discount.calculateDiscount(this.subtotal) : 0.0;
        this.totalAmount = Math.max(0, this.subtotal - this.discountAmount); // Ensure non-negative
    }

    // Helper method to add an order item
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    // Helper method to remove an order item
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
        calculateTotals();
    }
}