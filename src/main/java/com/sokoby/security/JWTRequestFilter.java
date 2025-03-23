package com.sokoby.security;

import com.sokoby.entity.Customer;
import com.sokoby.entity.Merchant;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.CustomerRepository;
import com.sokoby.repository.MerchantRepository;
import com.sokoby.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final MerchantRepository merchantRepository;
    private final CustomerRepository customerRepository;

    public JWTRequestFilter(JWTService jwtService, MerchantRepository merchantRepository,
                            CustomerRepository customerRepository) {
        this.jwtService = jwtService;
        this.merchantRepository = merchantRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessTokenHeader = request.getHeader("Authorization");

        if (accessTokenHeader != null && accessTokenHeader.startsWith("Bearer ")) {
            String accessToken = accessTokenHeader.substring(7);
            System.out.println("Access token header from client : " + accessToken);
            processAccessToken(accessToken, request, response);
        }

        filterChain.doFilter(request, response);
    }

    private void processAccessToken(String accessToken, HttpServletRequest request, HttpServletResponse response) {
        if (jwtService.isTokenValid(accessToken)) {
            authenticateUser(accessToken, request);
        }
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String email = jwtService.getUserEmail(token);
        try{
            Optional<Merchant> userOptional = merchantRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                Merchant user = userOptional.get();
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(user.getRole())); // Ensure "ROLE_MERCHANT"
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, authorities);
                authentication.setDetails(new WebAuthenticationDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            else {
                Optional<Customer> customerOptional = customerRepository.findByEmail(email);
                if (customerOptional.isPresent()) {
                    Customer user = customerOptional.get();
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(user.getRole())); // Ensure "ROLE_CUSTOMER"
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
           throw new MerchantException("User Not Found", "USER_NOT_FOUND");
        }
    }
}