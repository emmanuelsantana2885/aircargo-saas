package com.aircargo.controller;

import com.aircargo.entity.Flight;
import com.aircargo.repository.FlightRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightRepository flightRepository;

    public FlightController(FlightRepository flightRepository){
        this.flightRepository = flightRepository;
    }

    // GET /api/flights?airlineId=xxx
    @GetMapping
    public List<Flight> getAll(
            @RequestParam(required=false) UUID airlineId,
            @RequestParam(required =false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if(airlineId!= null && date != null){
            return flightRepository.findByAirlineIdAndFlightDate(airlineId, date);
        }
        if (airlineId != null){
            return flightRepository.findByAirlineId(airlineId);
        }
        return flightRepository.findAll();
    }

    // GET /api/flights/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Flight> getById(@PathVariable UUID id) {
        return flightRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
