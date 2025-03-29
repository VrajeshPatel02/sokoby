package com.sokoby.enums;

public enum OrderStatus {
    PLACED,
    PAYMENT_PENDING,    // Order created, awaiting payment
    PAYMENT_FAILED, // Payment failed, order canceled
    CONFIRMED,  // Payment successful, order placed
    CANCELED,   // Payment failed or order canceled
    SHIPPED,    // Order shipped
    DELIVERED   // Order delivered
}