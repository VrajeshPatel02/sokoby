package com.sokoby.controller;
import com.sokoby.entity.Payment;
import com.sokoby.enums.PaymentStatus;
import com.sokoby.payload.PaymentDto;
import com.sokoby.repository.OrderRepository;
import com.sokoby.repository.PaymentRepository;
import com.sokoby.service.InventoryService;
import com.sokoby.service.PaymentService;
import com.sokoby.util.PaymentRequest;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.sokoby.exception.MerchantException;
import com.sokoby.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;


    @Autowired
    public PaymentController(PaymentService paymentService, PaymentRepository payementRepository, InventoryService inventoryService, OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = payementRepository;
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> createPayment(@PathVariable UUID orderId,
                                                    @RequestBody PaymentRequest paymentRequest) {
        PaymentDto payment = paymentService.createPayment(orderId, paymentRequest.getPaymentMethodId());
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable UUID id) {
        PaymentDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable UUID orderId) {
        PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, "whsec_your_webhook_secret");
            if ("checkout.session.completed".equals(event.getType())) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().get();

                Payment payment = paymentRepository.findByStripeCheckoutSessionId(session.getId())
                        .orElseThrow(() -> new MerchantException("Payment not found", "PAYMENT_NOT_FOUND"));

                payment.setStripePaymentIntentId(session.getPaymentIntent());
                payment.setStatus(PaymentStatus.valueOf("succeeded"));
                paymentRepository.save(payment);

                Order order = payment.getOrder();
                order.getOrderItems().forEach(item ->
                        inventoryService.reserveStock(item.getVariant().getId(), item.getQuantity()));
                orderRepository.save(order);
            }
            return ResponseEntity.ok().build();
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}