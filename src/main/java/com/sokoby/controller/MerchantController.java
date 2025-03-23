package com.sokoby.controller;

import com.sokoby.payload.MerchantDto;
import com.sokoby.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllMerchants() {
        return new ResponseEntity<>(merchantService.getAllMerchants(), HttpStatus.OK);
    }

    @GetMapping("/getById")
    public ResponseEntity<?> getMerchantById(@RequestParam UUID id) {
        MerchantDto merchant = merchantService.getMerchantById(id);
        return new ResponseEntity<>(merchant, HttpStatus.OK);
    }

    @PutMapping("/updateMerchant")
    public ResponseEntity<?> updateMerchant(@RequestParam UUID id, @RequestBody MerchantDto dto) {
        MerchantDto merchant = merchantService.updateMerchant(id, dto);
        return new ResponseEntity<>(merchant, HttpStatus.OK);
    }

    @DeleteMapping("/deleteMerchant")
    public ResponseEntity<?> deleteMerchant(@RequestParam UUID id) {
        merchantService.deleteMerchant(id);
        return new ResponseEntity<>("Merchant deleted successfully", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MerchantDto>> searchMerchants(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MerchantDto> merchantPage = merchantService.searchMerchants(query, pageable);
        return new ResponseEntity<>(merchantPage, HttpStatus.OK);
    }
}
