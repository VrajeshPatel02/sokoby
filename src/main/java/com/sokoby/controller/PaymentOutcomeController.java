package com.sokoby.controller;

import com.sokoby.entity.Order;
import com.sokoby.enums.OrderStatus;
import com.sokoby.exception.MerchantException;
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

    private final OrderRepository orderRepository;

    @Autowired
    public PaymentOutcomeController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/success")
    public ResponseEntity<String> handlePaymentSuccess(@RequestParam("orderId") UUID orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

            // For testing, log success and optionally update order status
            logger.info("Payment successful for order: {}", orderId);

            // Optional: Update order status if not handled by webhook
            // Uncomment if webhook isn’t updating status
            /*
            order.setStatus(OrderStatus.PLACED);
            orderRepository.save(order);
            */

            return ResponseEntity.ok("Payment successful for order: " + orderId);
        } catch (MerchantException e) {
            logger.error("Error handling payment success for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> handlePaymentCancel(@RequestParam("orderId") UUID orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new MerchantException("Order not found", "ORDER_NOT_FOUND"));

            // For testing, log cancellation and optionally update order status
            logger.info("Payment cancelled for order: {}", orderId);

            // Optional: Mark order as cancelled if desired
            /*
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            */

            return ResponseEntity.ok("Payment cancelled for order: " + orderId);
        } catch (MerchantException e) {
            logger.error("Error handling payment cancellation for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}