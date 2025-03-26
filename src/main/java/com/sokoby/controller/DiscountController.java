package com.sokoby.controller;

import com.sokoby.payload.DiscountDto;
import com.sokoby.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountService discountService;

    @Autowired
    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public ResponseEntity<List<DiscountDto>> getAllDiscounts() {
        List<DiscountDto> discounts = discountService.getAllDiscounts();
        return ResponseEntity.ok(discounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountDto> getDiscountById(@PathVariable UUID id) {
        DiscountDto discount = discountService.getDiscountById(id);
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<DiscountDto> getDiscountByCode(@PathVariable String code) {
        DiscountDto discount = discountService.getDiscountByCode(code);
        return ResponseEntity.ok(discount);
    }

    @PostMapping
    public ResponseEntity<DiscountDto> createDiscount(@RequestBody DiscountDto dto) {
        DiscountDto createdDiscount = discountService.createDiscount(dto);
        return new ResponseEntity<>(createdDiscount, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountDto> updateDiscount(@PathVariable UUID id, @RequestBody DiscountDto dto) {
        DiscountDto updatedDiscount = discountService.updateDiscount(id, dto);
        return ResponseEntity.ok(updatedDiscount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable UUID id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
}