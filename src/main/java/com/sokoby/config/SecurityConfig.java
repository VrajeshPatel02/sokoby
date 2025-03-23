package com.sokoby.config;

import com.sokoby.security.JWTRequestFilter;
import com.sokoby.security.JWTSuccessHandler;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer; // Added for session management
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer; // Added for CORS
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer; // Added for CSRF
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer; // Added for OAuth2
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JWTRequestFilter jwtRequestFilter;
    private final JWTSuccessHandler jwtSuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    // Constructor injection for dependencies
    public SecurityConfig(JWTRequestFilter jwtRequestFilter,
                          JWTSuccessHandler jwtSuccessHandler,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtSuccessHandler = jwtSuccessHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(this::configureCors)
                .csrf(this::configureCsrf)
                .sessionManagement(this::configureSession)
                .authorizeHttpRequests(this::configureAuthorization)
                .oauth2Login(this::configureOAuth2)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configure authorization rules
    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(SecurityConstants.MERCHANT_ENDPOINTS).hasRole(SecurityConstants.ROLE_MERCHANT)
                .requestMatchers(SecurityConstants.CUSTOMER_ENDPOINTS).hasRole(SecurityConstants.ROLE_CUSTOMER)
                .anyRequest().authenticated();
    }

    // Configure CORS
    private void configureCors(CorsConfigurer<HttpSecurity> cors) {
        cors.configurationSource(corsConfigurationSource);
    }

    // Configure CSRF
    private void configureCsrf(CsrfConfigurer<HttpSecurity> csrf) {
        csrf.disable(); // Disable CSRF for stateless JWT-based auth
    }

    // Configure session management
    private void configureSession(SessionManagementConfigurer<HttpSecurity> session) {
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    // Configure OAuth2 login
    private void configureOAuth2(OAuth2LoginConfigurer<HttpSecurity> oauth2) {
        oauth2
                .loginPage(SecurityConstants.OAUTH2_LOGIN_PAGE)
                .successHandler(jwtSuccessHandler);
    }
}