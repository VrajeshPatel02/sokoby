package com.sokoby.service;

import com.sokoby.exception.PaymentException;
import com.sokoby.payload.PaymentIntentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    public PaymentIntentDto createPaymentIntent(Long amount, String currency, String description) {
        try {
            long amountInCents =  Math.round(amount * 100);
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents) // Convert to cents
                    .setCurrency(currency.toLowerCase())
                    .setDescription(description)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return new PaymentIntentDto(
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    paymentIntent.getCurrency(),
                    paymentIntent.getAmount());
        } catch (StripeException e) {
            throw new PaymentException("Failed to create payment intent: " + e.getMessage(), "STRIPE_ERROR");
        }
    }

    public PaymentIntent confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.confirm();
        } catch (StripeException e) {
            throw new PaymentException("Failed to confirm payment: " + e.getMessage(), "STRIPE_ERROR");
        }
    }

    public PaymentIntent cancelPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.cancel();
        } catch (StripeException e) {
            throw new PaymentException("Failed to cancel payment: " + e.getMessage(), "STRIPE_ERROR");
        }
    }
}