package com.sokoby.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private UUID id;

    @NotNull
    private UUID orderId;

    @NotNull
    @Min(value = 0, message = "Amount must be non-negative")
    private Double amount;

    @NotNull
    private String paymentMethod;

    @NotNull
    private String status;

    private String stripePaymentId;

    private String clientSecret;

    private String transactionId;

    private String description;

    private String currency;

    private Date createdAt;
    private Date updatedAt;


    public PaymentDto(String id, Long amount, String currency, String clientSecret, String status, String description) {
        this.id = UUID.fromString(id);
        this.amount = amount / 100.0;
        this.clientSecret = clientSecret;
        this.status = status;
        this.currency = currency;
        this.description=description;
    }
}