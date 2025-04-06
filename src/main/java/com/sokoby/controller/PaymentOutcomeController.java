package com.sokoby.controller;

import com.sokoby.entity.Merchant;
import com.sokoby.entity.Order;
import com.sokoby.enums.OrderStatus;
import com.sokoby.exception.MerchantException;
import com.sokoby.repository.MerchantRepository;
import com.sokoby.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class PaymentOutcomeController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentOutcomeController.class);

    private final MerchantRepository merchantRepository;

    @Autowired
    public PaymentOutcomeController(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @GetMapping("/success")
    public ResponseEntity<String> handlePaymentSuccess(@RequestParam("merchantId") UUID merchantId) {
        try {
            Merchant merchant = merchantRepository.findById(merchantId)
                    .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

            // For testing, log success and optionally update order status
            logger.info("Payment successful for merchant: {}", merchantId);

            return ResponseEntity.ok("Payment successful for merchant: " + merchantId);
        } catch (MerchantException e) {
            logger.error("Error handling payment success for order {}: {}", merchantId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> handlePaymentCancel(@RequestParam("merchantId") UUID merchantId) {
        try {
            Merchant merchant = merchantRepository.findById(merchantId)
                    .orElseThrow(() -> new MerchantException("Merchant not found", "MERCHANT_NOT_FOUND"));

            // For testing, log cancellation and optionally update order status
            logger.info("Payment cancelled for merchant: {}", merchantId);

            return ResponseEntity.ok("Payment cancelled for merchant: " + merchantId);
        } catch (MerchantException e) {
            logger.error("Error handling payment cancellation for merchant {}: {}", merchantId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}