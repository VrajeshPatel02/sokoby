package com.sokoby.config;

public final class SecurityConstants {
    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // Public endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/customer/create/{storeId}",
            "/api/store/{id}",
            "/api/store/search",
            "/api/store/merchant/{merchantId}",
            "/api/product/search/store/{storeId}",
            "/api/product/{id}",
            "/api/product/store/{id}",
            "/api/product/search"
    };

    // Merchant endpoints
    public static final String[] MERCHANT_ENDPOINTS = {
            "/api/product/create/{storeId}",
            "/api/product/update/{id}",
            "/api/product/delete/{id}",
            "/api/store/create/{merchantId}",
            "/api/store/update/{id}",
            "/api/store/delete/{id}",
            "/api/merchant/**"
    };

    // Customer endpoints
    public static final String[] CUSTOMER_ENDPOINTS = {
            "/api/customer/**"
    };

    // Admin endpoints
    public static final String[] ADMIN_ENDPOINTS = {
            "/admin/**"
    };

    // OAuth2 login page
    public static final String OAUTH2_LOGIN_PAGE = "/api/auth/google/login";
}
