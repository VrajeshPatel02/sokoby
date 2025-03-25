package com.sokoby.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentDto {
    private String paymentIntentId;
    private String clientSecret;
    private String currency;
    private Long amount;
}