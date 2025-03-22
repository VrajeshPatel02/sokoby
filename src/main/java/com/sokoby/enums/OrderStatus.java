package com.sokoby.enums;

public enum OrderStatus {
    PLACED,
    PENDING,    // Order created, awaiting payment
    CONFIRMED,  // Payment successful, order placed
    CANCELED,   // Payment failed or order canceled
    SHIPPED,    // Order shipped
    DELIVERED   // Order delivered
}