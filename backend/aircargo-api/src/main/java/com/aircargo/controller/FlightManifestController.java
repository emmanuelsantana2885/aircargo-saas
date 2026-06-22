package com.aircargo.controller;

import com.aircargo.service.FlightManifestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cargo/flights")
public class FlightManifestController {

    private final FlightManifestService manifestService;

    public FlightManifestController(FlightManifestService manifestService) {
        this.manifestService = manifestService;
    }

    /**
     * Endpoint crítico para ejecutar el cierre y congelamiento de pesos del vuelo.
     */
    @PostMapping("/{flightId}/finalize")
    public ResponseEntity<?> finalizeFlightManifest(@PathVariable UUID flightId) {
        try {
            Map<String, Object> summary = manifestService.finalizeFlight(flightId);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(422).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error procesando el cierre: " + ex.getMessage()));
        }
    }
}
