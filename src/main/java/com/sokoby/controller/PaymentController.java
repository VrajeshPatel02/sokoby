package com.sokoby.controller;

import com.sokoby.payload.PaymentDto;
import com.sokoby.payload.PaymentIntentDto;
import com.sokoby.service.PaymentService;
import com.sokoby.service.StripeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final StripeService stripeService;

    @Autowired
    public PaymentController(PaymentService paymentService, StripeService stripeService) {
        this.paymentService = paymentService;
        this.stripeService = stripeService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody PaymentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomerId(customerId));
    }

    @GetMapping("/customer/{customerId}/page")
    public ResponseEntity<Page<PaymentDto>> getPaymentsByCustomerIdPaginated(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(paymentService.getPaymentsByCustomerId(customerId, pageable));
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<PaymentDto>> getPaymentsByStoreId(@PathVariable UUID storeId) {
        return ResponseEntity.ok(paymentService.getPaymentsByStoreId(storeId));
    }

    @GetMapping("/store/{storeId}/page")
    public ResponseEntity<Page<PaymentDto>> getPaymentsByStoreIdPaginated(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(paymentService.getPaymentsByStoreId(storeId, pageable));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('MERCHANT') or (hasRole('CUSTOMER') and authentication.principal.id == @orderService.getOrderById(@paymentService.getPaymentById(#id).orderId).customerId)")
    public ResponseEntity<PaymentDto> updatePayment(
            @PathVariable UUID id,
            @RequestBody PaymentDto dto) {
        return ResponseEntity.ok(paymentService.updatePayment(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Void> deletePayment(@PathVariable UUID id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentDto> createPaymentIntent(@Valid @RequestBody PaymentDto paymentDto) {
        PaymentDto payment = paymentService.createPayment(paymentDto);
        return ResponseEntity.ok(new PaymentIntentDto(
                payment.getStripePaymentId(),
                payment.getTransactionId(),
                payment.getPaymentMethod(),
                Long.parseLong( payment.getAmount().toString())
        ));
    }

    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentDto> confirmPayment(@PathVariable String paymentIntentId) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentIntentId));
    }

    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<PaymentDto> cancelPayment(@PathVariable String paymentIntentId) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentIntentId));
    }

//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleStripeWebhook(
//            @RequestBody String payload,
//            @RequestHeader("Stripe-Signature") String sigHeader) {
//        try {
//            paymentService.handleStripeWebhook(payload, sigHeader);
//            return ResponseEntity.ok("Webhook processed successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Webhook processing failed");
//        }
//    }
}