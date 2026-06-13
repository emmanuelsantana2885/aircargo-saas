package com.aircargo.controller;

import com.aircargo.dto.LoadPlanningDTO;
import com.aircargo.dto.LoadPlanningImportResultDTO;
import com.aircargo.service.LoadPlanningImportService;
import com.aircargo.service.LoadPlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/load-planning")
public class LoadPlanningController {

    private final LoadPlanningService loadPlanningService;
    private final LoadPlanningImportService loadPlanningImportService;

    public LoadPlanningController(LoadPlanningService loadPlanningService,
                                   LoadPlanningImportService loadPlanningImportService) {
        this.loadPlanningService = loadPlanningService;
        this.loadPlanningImportService = loadPlanningImportService;
    }

    @GetMapping("/{flightId}")
    public ResponseEntity<LoadPlanningDTO> getByFlight(@PathVariable UUID flightId) {
        return loadPlanningService.getByFlightId(flightId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<LoadPlanningImportResultDTO> importLoadPlanning(
            @RequestParam("file") MultipartFile file) throws IOException {
        LoadPlanningImportResultDTO result = loadPlanningImportService.importLoadPlanning(file);
        return ResponseEntity.ok(result);
    }
}
