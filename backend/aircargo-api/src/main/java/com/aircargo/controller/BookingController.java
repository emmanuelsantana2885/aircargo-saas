package com.aircargo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aircargo.dto.BookingAwbUpdateRequest;
import com.aircargo.dto.BookingDTO;
import com.aircargo.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId) {
        return bookingService.getAll(airlineId, flightId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getById(@PathVariable UUID id) {
        return bookingService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookingDTO> create(@Valid @RequestBody BookingDTO dto) {
        BookingDTO created = bookingService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> update(@PathVariable UUID id, @Valid @RequestBody BookingDTO dto) {
        return bookingService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = bookingService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/awb")
    public ResponseEntity<BookingDTO> updateAwb(@PathVariable UUID id, @Valid @RequestBody BookingAwbUpdateRequest request) {
        if (request == null || request.getAwbNumber() == null || request.getAwbNumber().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return bookingService.updateAwb(id, request.getAwbNumber())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
