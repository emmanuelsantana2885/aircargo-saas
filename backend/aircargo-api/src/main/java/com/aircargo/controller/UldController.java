package com.aircargo.controller;

import com.aircargo.dto.UldDTO;
import com.aircargo.entity.UldStatus;
import com.aircargo.service.UldService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ulds")
public class UldController {

    private final UldService uldService;

    public UldController(UldService uldService) {
        this.uldService = uldService;
    }

    @GetMapping
    public List<UldDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId) {
        return uldService.getAll(airlineId, flightId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UldDTO> getById(@PathVariable UUID id) {
        return uldService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UldDTO> create(@Valid @RequestBody UldDTO dto) {
        UldDTO created = uldService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UldDTO> update(@PathVariable UUID id, @Valid @RequestBody UldDTO dto) {
        return uldService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = uldService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
