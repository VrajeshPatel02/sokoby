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

    private String transactionId;

    private Date createdAt;
    private Date updatedAt;
}