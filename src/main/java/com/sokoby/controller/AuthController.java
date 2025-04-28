package com.sokoby.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sokoby.payload.JWTTokenDto;
import com.sokoby.payload.LoginDto;
import com.sokoby.payload.MerchantDto;
import com.sokoby.service.CustomerService;
import com.sokoby.service.MerchantService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")  // API endpoint for user authentication and registration.
public class AuthController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@Valid @RequestBody MerchantDto dto, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(Objects.requireNonNull(result.getFieldError()).getDefaultMessage(), HttpStatus.OK);
        }
        MerchantDto created = merchantService.createNewMerchant(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> verifyLogin(@RequestBody LoginDto loginDto) {


        JWTTokenDto jwtTokenDto = merchantService.verifyUser(loginDto);
        if (jwtTokenDto.getToken() != null) {
            return new ResponseEntity<>(jwtTokenDto, HttpStatus.CREATED);
        }
        return new ResponseEntity<>("Invalid token", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping("/customer/login")
    public ResponseEntity<?> verifyCustomerLogin(@RequestBody LoginDto loginDto) {
        JWTTokenDto jwtTokenDto = customerService.verifyUser(loginDto);
        if(jwtTokenDto.getToken() != null) {
            return new ResponseEntity<>(jwtTokenDto, HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid token", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Clear the auth_token cookie
        Cookie authCookie = new Cookie("auth_token", null);
        authCookie.setHttpOnly(true);
        authCookie.setPath("/"); // Must match the path set in JWTSuccessHandler
        authCookie.setMaxAge(0); // Expire immediately
        authCookie.setSecure(false); // Set to true in production with HTTPS
        response.addCookie(authCookie);

        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }
}
