package com.aircargo.controller;

import com.aircargo.service.BiService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi")
public class BiController {

    private final BiService biService;

    public BiController(BiService biService) {
        this.biService = biService;
    }

    @GetMapping("/flights")
    public ResponseEntity<List<Map<String, Object>>> getFlights(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(biService.getFlights(dateFrom, dateTo));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(biService.getBookings(dateFrom, dateTo));
    }

    @GetMapping("/mawbs")
    public ResponseEntity<List<Map<String, Object>>> getMawbs() {
        return ResponseEntity.ok(biService.getMawbs());
    }

    @GetMapping("/receipts")
    public ResponseEntity<List<Map<String, Object>>> getReceipts() {
        return ResponseEntity.ok(biService.getReceipts());
    }

    @GetMapping("/ulds")
    public ResponseEntity<List<Map<String, Object>>> getUlds() {
        return ResponseEntity.ok(biService.getUlds());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(biService.getDashboard());
    }

    @GetMapping("/daily")
    public ResponseEntity<List<Map<String, Object>>> getDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(biService.getDaily(dateFrom, dateTo));
    }
}
