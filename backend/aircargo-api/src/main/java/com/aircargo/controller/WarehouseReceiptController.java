package com.aircargo.controller;


import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.repository.WarehouseReceiptRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
public class WarehouseReceiptController {

    private final WarehouseReceiptRepository receiptRepository;

    public WarehouseReceiptController(WarehouseReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    //Get /api/receipts
    @GetMapping
    public List<WarehouseReceipt> getAll(
            @RequestParam(required = false) UUID airlineId) {
        if (airlineId != null) {
            return receiptRepository.findByAirlineId(airlineId);
        }
        return receiptRepository.findAll();
    }

    // GET /api/receipts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseReceipt> getById(@PathVariable UUID id) {
        return receiptRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/receipts/mawb/{mawbId}
    @GetMapping("/mawb/{mawbId}")
    public ResponseEntity<WarehouseReceipt> getByMawb(@PathVariable UUID mawbId) {
        return receiptRepository.findByMawbId(mawbId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
