package com.sokoby.payload;

import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionDto {
    private UUID id;
    private UUID merchant; // Merchant ID
    private Double amount; // Subscription amount
    private String interval; // "MONTH" or "YEAR"
    private String stripeCheckoutSessionId;
    private String stripeSubscriptionId;
    private String status; // Maps to SubscriptionStatus enum
}