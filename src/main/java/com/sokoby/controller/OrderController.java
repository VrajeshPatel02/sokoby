package com.sokoby.controller;

import com.sokoby.payload.OrderDto;
import com.sokoby.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        // Note: This assumes you might want a general list; adjust based on your needs
        throw new UnsupportedOperationException("Use customer or store-specific endpoints");
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable UUID id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(@PathVariable UUID customerId) {
        List<OrderDto> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<OrderDto>> getOrdersByStoreId(@PathVariable UUID storeId) {
        List<OrderDto> orders = orderService.getOrdersByStoreId(storeId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto dto) {
        OrderDto createdOrder = orderService.createOrder(dto);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PostMapping("/with-customer-details")
    public ResponseEntity<OrderDto> createOrderWithCustomerDetails(@RequestBody OrderDto dto) {
        OrderDto createdOrder = orderService.createOrderWithCustomerDetails(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable UUID id, @RequestBody OrderDto dto) {
        OrderDto updatedOrder = orderService.updateOrder(id, dto);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable UUID id, @RequestBody String status) {
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}