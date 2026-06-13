package com.aircargo.controller;

import com.aircargo.dto.LoadPlanningDTO;
import com.aircargo.service.LoadPlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/load-planning")
public class LoadPlanningController {

    private final LoadPlanningService loadPlanningService;

    public LoadPlanningController(LoadPlanningService loadPlanningService) {
        this.loadPlanningService = loadPlanningService;
    }

    // GET /api/load-planning/{flightId}
    @GetMapping("/{flightId}")
    public ResponseEntity<LoadPlanningDTO> getByFlight(@PathVariable UUID flightId) {
        return loadPlanningService.getByFlightId(flightId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
