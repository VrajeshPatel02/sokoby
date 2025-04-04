package com.sokoby.config;

public final class SecurityConstants {
    // Roles
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // Public endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/**",
            "/api/customer/create/{id}",
            "/payment/webhook",
            "/payment/**",
            "/api/cart/**",
            "/api/product/**"
    };

    // Merchant endpoints
    public static final String[] MERCHANT_ENDPOINTS = {
            "/api/merchant/**",
            "/api/discounts/**",
            "/api/inventory/**"
    };

    // Customer endpoints
    public static final String[] CUSTOMER_ENDPOINTS = {
            "/api/customer/**",
            "/api/reviews","/api/reviews/{id}"
    };

    // OAuth2 login page
    public static final String OAUTH2_LOGIN_PAGE = "/api/auth/google/login";
}
