package com.sokoby.security;

import com.sokoby.entity.Merchant;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.MerchantRepository;
import com.sokoby.service.JWTService;
import com.sokoby.service.MerchantService;
import com.sokoby.util.PasswordGenerator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JWTSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final MerchantService merchantService;
    private final MerchantRepository merchantRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${client.url}")
    private String clientUrl;

    public JWTSuccessHandler(JWTService jwtService, MerchantService merchantService,
                             MerchantRepository merchantRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.merchantService = merchantService;
        this.merchantRepository = merchantRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String email = oauthToken.getPrincipal().getAttribute("email");
        String firstName = oauthToken.getPrincipal().getAttribute("given_name");
        String lastname = oauthToken.getPrincipal().getAttribute("family_name");
        Merchant merchant = merchantService.loadMerchantByEmail(email);
        try {
            if (merchant == null) {
                merchant = createNewMerchant(email, firstName, lastname);
                merchantRepository.save(merchant);
                merchant = merchantService.loadMerchantByEmail(email); // Ensure we get the persisted entity

            }
            String token = jwtService.generateToken(merchant.getEmail());
            String redirectUrl = clientUrl + "/auth/callback?token=" + token;
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new MerchantException("Merchant Not Found with email address", "MERCHANT_NOT_FOUND");
        }
    }

    public Merchant createNewMerchant(String email, String firstname, String lastname) {
        Merchant merchant = new Merchant();
        merchant.setEmail(email);
        merchant.setFirstName(firstname);
        merchant.setLastName(lastname);
        merchant.setPassword(BCrypt.hashpw(PasswordGenerator.generatePassword(12), BCrypt.gensalt(10)));
        return merchant;
    }
}