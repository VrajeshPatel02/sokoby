package com.sokoby.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class PaymentDto {
    private UUID id;
    private UUID orderId;
    private Double amount;
    private String currency;
    private String stripePaymentIntentId;
    private String status;
    private String stripeCheckoutSessionId; // Add getter/setter
    private Date createdAt;
}