package com.sokoby.entity;

import com.sokoby.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", nullable = false)
    private String currency = "usd"; // Default to USD, adjust as needed

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "stripe_checkout_session_id")
    private String stripeCheckoutSessionId; // Add this field

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId; // Add this field

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        if (this.status == null) this.status = PaymentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}