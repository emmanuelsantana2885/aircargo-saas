package com.aircargo.controller;

import com.aircargo.dto.WarehouseReceiptDTO;
import com.aircargo.service.WarehouseReceiptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
public class WarehouseReceiptController {

    private final WarehouseReceiptService receiptService;

    public WarehouseReceiptController(WarehouseReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public List<WarehouseReceiptDTO> getAll(@RequestParam(required = false) UUID airlineId) {
        return receiptService.getAll(airlineId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseReceiptDTO> getById(@PathVariable UUID id) {
        return receiptService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WarehouseReceiptDTO> create(@Valid @RequestBody WarehouseReceiptDTO dto) {
        WarehouseReceiptDTO created = receiptService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseReceiptDTO> update(@PathVariable UUID id, @Valid @RequestBody WarehouseReceiptDTO dto) {
        return receiptService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = receiptService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
