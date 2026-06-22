package com.aircargo.controller;

import com.aircargo.entity.Hawb;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.repository.HawbRepository;
import com.aircargo.repository.MawbRepository;
import com.aircargo.service.MawbValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/cargo/mawbs")
public class MawbController {

    private final MawbRepository mawbRepository;
    private final HawbRepository hawbRepository;
    private final MawbValidationService validationService;

    public MawbController(MawbRepository mawbRepository, HawbRepository hawbRepository, MawbValidationService validationService) {
        this.mawbRepository = mawbRepository;
        this.hawbRepository = hawbRepository;
        this.validationService = validationService;
    }

    /**
     * Endpoint para listar todas las guías maestras (sin filtro de vuelo).
     */
    @GetMapping
    public ResponseEntity<List<Mawb>> getAllMawbs() {
        return ResponseEntity.ok(mawbRepository.findAll());
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
     * Si ya existe una MAWB con el mismo airline + awbNumber y tiene menos de 2 HAWBs,
     * se reutiliza la existente en lugar de crear un duplicado.
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

            // 3. Dedup: si ya existe una MAWB con el mismo número para esta aerolínea
            UUID airlineId = mawb.getAirline() != null ? mawb.getAirline().getId() : null;
            if (airlineId != null) {
                Optional<Mawb> existing = mawbRepository.findByAirlineIdAndAwbNumber(airlineId, mawb.getAwbNumber());
                if (existing.isPresent()) {
                    Mawb existingMawb = existing.get();
                    List<Hawb> hawbs = hawbRepository.findByMawbId(existingMawb.getId());
                    if (hawbs.size() < 2) {
                        // Reutilizar la MAWB existente (tiene 0 o 1 HAWBs — no es consolidación)
                        return ResponseEntity.ok(existingMawb);
                    }
                    // Tiene 2+ HAWBs: es una MAWB de consolidación, permitir crear nuevo duplicado
                }
            }

            Mawb savedMawb = mawbRepository.save(mawb);
            return ResponseEntity.ok(savedMawb);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(422).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error procesando el registro: " + ex.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar el estado de una MAWB (BOOKED → RECEIVED → MANIFESTED → DEPARTED).
     */
    @PatchMapping("/{mawbId}/status")
    public ResponseEntity<?> updateMawbStatus(@PathVariable UUID mawbId, @RequestBody Map<String, String> body) {
        return mawbRepository.findById(mawbId).map(mawb -> {
            String newStatus = body.get("status");
            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Se requiere el campo 'status'."));
            }
            try {
                MawbStatus status = MawbStatus.valueOf(newStatus.toUpperCase());
                mawb.setStatus(status);
                mawbRepository.save(mawb);
                return ResponseEntity.ok(Map.of("success", true, "status", status.name()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Estado inválido: " + newStatus));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
