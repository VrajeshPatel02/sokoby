package com.sokoby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code; // e.g., "SAVE10"

    @Column(name = "discount_type", nullable = false)
    private String discountType; // "PERCENTAGE" or "FIXED"

    @Column(name = "value", nullable = false)
    private Double value; // e.g., 10.0 for 10% or $10

    @Column(name = "minimum_order_amount")
    private Double minimumOrderAmount; // Minimum subtotal to apply discount

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valid_from")
    private Date validFrom;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valid_until")
    private Date validUntil;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    // Calculate discount based on subtotal
    public double calculateDiscount(double subtotal) {
        if (!isActive || !isValid() || (minimumOrderAmount != null && subtotal < minimumOrderAmount)) {
            return 0.0;
        }

        if ("PERCENTAGE".equals(discountType)) {
            return subtotal * (value / 100.0);
        } else if ("FIXED".equals(discountType)) {
            return Math.min(value, subtotal); // Can't discount more than subtotal
        }
        return 0.0;
    }

    private boolean isValid() {
        Date now = new Date();
        return (validFrom == null || now.after(validFrom)) &&
                (validUntil == null || now.before(validUntil));
    }
}