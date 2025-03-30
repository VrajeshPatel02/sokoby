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
    private String stripeCheckoutUrl;
    private String stripeCheckoutSessionId;
    private String errorMessage;
    private Date updatedAt;
    private Date createdAt;
}