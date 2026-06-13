package com.aircargo.controller;

import com.aircargo.dto.HawbDTO;
import com.aircargo.service.HawbService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hawbs")
public class HawbController {

    private final HawbService hawbService;

    public HawbController(HawbService hawbService) {
        this.hawbService = hawbService;
    }

    @GetMapping
    public List<HawbDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID mawbId) {
        return hawbService.getAll(airlineId, mawbId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HawbDTO> getById(@PathVariable UUID id) {
        return hawbService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HawbDTO> create(@Valid @RequestBody HawbDTO dto) {
        HawbDTO created = hawbService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HawbDTO> update(@PathVariable UUID id, @Valid @RequestBody HawbDTO dto) {
        return hawbService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = hawbService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
