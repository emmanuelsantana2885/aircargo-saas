package com.aircargo.controller;

import com.aircargo.dto.MawbDTO;
import com.aircargo.entity.MawbStatus;
import com.aircargo.service.MawbService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mawbs")
public class MawbController {

    private final MawbService mawbService;

    public MawbController(MawbService mawbService){
        this.mawbService = mawbService;
    }

    @GetMapping
    public List<MawbDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId,
            @RequestParam(required = false) MawbStatus status){
        return mawbService.getAll(airlineId, flightId, status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MawbDTO> getById(@PathVariable UUID id){
        return mawbService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/awb/{awbNumber}")
    public ResponseEntity<MawbDTO> getByAwbNumber(
         @PathVariable String awbNumber,
         @RequestParam UUID airlineId) {
        return mawbService.getByAirlineIdAndAwbNumber(airlineId, awbNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MawbDTO> create(@Valid @RequestBody MawbDTO dto) {
        MawbDTO created = mawbService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MawbDTO> update(@PathVariable UUID id, @Valid @RequestBody MawbDTO dto) {
        return mawbService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MawbDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam MawbStatus status){
        return mawbService.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = mawbService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
