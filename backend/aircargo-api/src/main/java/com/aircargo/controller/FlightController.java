package com.aircargo.controller;

import com.aircargo.dto.FlightDTO;
import com.aircargo.entity.FlightStatus;
import com.aircargo.service.FlightService;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService){
        this.flightService = flightService;
    }

    // GET /api/flights?airlineId=xxx
    @GetMapping
    public List<FlightDTO> getAll(
            @RequestParam(required=false) UUID airlineId,
            @RequestParam(required =false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) FlightStatus status,
            @RequestParam(required = false) String flightNumber) {

        return flightService.getAll(airlineId, date, status, flightNumber);
    }

    // GET /api/flights/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getById(@PathVariable UUID id) {
        return flightService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/flights
    @PostMapping
    public ResponseEntity<FlightDTO> create(@Valid @RequestBody FlightDTO dto){
        FlightDTO created = flightService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/flights/{id}
    @PutMapping("/{id}")
    public ResponseEntity<FlightDTO> update(@PathVariable UUID id, @Valid @RequestBody FlightDTO dto){
        return flightService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/flights/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        boolean removed = flightService.delete(id);
        if(!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

}
