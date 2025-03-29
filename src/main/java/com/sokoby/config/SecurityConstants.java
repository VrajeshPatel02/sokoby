package com.sokoby.config;

public final class SecurityConstants {
    // Roles
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // Public endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/**","/api/customer/create/{id}", "/payment/**"
    };

    // Merchant endpoints
    public static final String[] MERCHANT_ENDPOINTS = {
            "/api/merchant/**"
    };

    // Customer endpoints
    public static final String[] CUSTOMER_ENDPOINTS = {
            "/api/customer/**"
    };

    // OAuth2 login page
    public static final String OAUTH2_LOGIN_PAGE = "/api/auth/google/login";
}
