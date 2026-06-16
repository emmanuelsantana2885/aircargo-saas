package com.aircargo.controller;

import com.aircargo.entity.Mawb;
import com.aircargo.repository.MawbRepository;
import com.aircargo.service.MawbValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cargo/mawbs")
@CrossOrigin(origins = "*")
public class MawbController {

    private final MawbRepository mawbRepository;
    private final MawbValidationService validationService;

    public MawbController(MawbRepository mawbRepository, MawbValidationService validationService) {
        this.mawbRepository = mawbRepository;
        this.validationService = validationService;
    }

    /**
     * Endpoint para listar todas las guías maestras de un vuelo específico.
     */
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<Mawb>> getMawbsByFlight(@PathVariable UUID flightId) {
        return ResponseEntity.ok(mawbRepository.findByFlightId(flightId));
    }

    /**
     * Endpoint para registrar una nueva guía aérea validando formato y capacidad de carga.
     */
    @PostMapping
    public ResponseEntity<?> createMawb(@RequestBody Mawb mawb) {
        try {
            // 1. Validar sintaxis del número de guía (Formato 406-XXXXXXXX)
            validationService.validateAwbFormat(mawb.getAwbNumber());

            // 2. Validar que no se rompa el Payload estructural del avión asignado
            if (mawb.getFlight() != null && mawb.getChargeableWeightKg() != null) {
                validationService.validateFlightPayloadLimit(mawb.getFlight().getId(), mawb.getChargeableWeightKg());
            }

            Mawb savedMawb = mawbRepository.save(mawb);
            return ResponseEntity.ok(savedMawb);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(422).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error procesando el registro: " + ex.getMessage()));
        }
    }
}
