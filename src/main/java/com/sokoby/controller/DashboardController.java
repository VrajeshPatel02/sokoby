package com.sokoby.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sokoby.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}", allowCredentials = "true")
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/store/{storeId}")
    public ResponseEntity<?> getDashboardStats(@PathVariable String storeId) {
        log.info("Fetching dashboard stats for store: {}", storeId);
        try {
            var stats = dashboardService.getDashboardStats(storeId);
            log.info("Successfully fetched dashboard stats for store: {}", storeId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats for store: {}", storeId, e);
            return ResponseEntity.internalServerError().body("Error fetching dashboard stats: " + e.getMessage());
        }
    }
} 