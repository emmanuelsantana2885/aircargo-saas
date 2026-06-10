package com.aircargo.controller;

import com.aircargo.entity.Uld;
import com.aircargo.entity.UldStatus;
import com.aircargo.repository.UldRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ulds")
public class UldController {

    private final UldRepository uldRepository;

    public UldController(UldRepository uldRepository) {
        this.uldRepository = uldRepository;
    }

    @GetMapping
    public List<Uld> getAll(
            @RequestParam(required = false) UUID flightId,
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UldStatus status) {

        if (flightId != null && status != null) {
            return uldRepository.findByFlightIdAndStatus(flightId, status);
        }
        if (flightId != null) {
            return uldRepository.findByFlightId(flightId);
        }
        if (airlineId != null) {
            return uldRepository.findByAirlineId(airlineId);
        }
        return uldRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Uld> getById(@PathVariable UUID id) {
        return uldRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
