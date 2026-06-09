package com.aircargo.controller;

import com.aircargo.entity.Airline;
import com.aircargo.repository.AirlineRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.RequestEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/airlines")
public class AirlineController {

    private final AirlineRepository airlineRepository;

    public AirlineController(AirlineRepository airlineRepository) {
        this.airlineRepository = airlineRepository;
    }

    //Get /api/airlines - lista todas las aerolineas
    @GetMapping
    public List<Airline> getAll() {
        return airlineRepository.findAll();
    }

    // Get /api/airlines/{id} -- buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Airline> getById(@PathVariable UUID id) {
        return airlineRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
