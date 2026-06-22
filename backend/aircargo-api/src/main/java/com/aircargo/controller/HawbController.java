package com.aircargo.controller;

import com.aircargo.entity.Hawb;
import com.aircargo.repository.HawbRepository;
import com.aircargo.service.HawbValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cargo/hawbs")
public class HawbController {

    private final HawbRepository hawbRepository;
    private final HawbValidationService validationService;

    public HawbController(HawbRepository hawbRepository, HawbValidationService validationService) {
        this.hawbRepository = hawbRepository;
        this.validationService = validationService;
    }

    /**
     * Endpoint para listar el desglose completo de House AWBs de una guía maestra.
     */
    @GetMapping("/mawb/{mawbId}")
    public ResponseEntity<List<Hawb>> getHawbsByMawb(@PathVariable UUID mawbId) {
        return ResponseEntity.ok(hawbRepository.findByMawbId(mawbId));
    }

    /**
     * Endpoint para actualizar campos de una HAWB existente (consignee, destination, pieces, weight).
     */
    @PutMapping("/{hawbId}")
    public ResponseEntity<?> updateHawb(@PathVariable UUID hawbId, @RequestBody Hawb updates) {
        return hawbRepository.findById(hawbId).map(hawb -> {
            if (updates.getConsigneeName() != null) hawb.setConsigneeName(updates.getConsigneeName());
            if (updates.getDestination() != null) hawb.setDestination(updates.getDestination());
            if (updates.getPieces() != null) hawb.setPieces(updates.getPieces());
            if (updates.getWeightKg() != null) hawb.setWeightKg(updates.getWeightKg());
            if (updates.getCommodityType() != null) hawb.setCommodityType(updates.getCommodityType());
            if (updates.getNotes() != null) hawb.setNotes(updates.getNotes());
            return ResponseEntity.ok(hawbRepository.save(hawb));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para inyectar una House AWB auditando que encaje perfectamente en la MAWB madre.
     */
    @PostMapping
    public ResponseEntity<?> createHawb(@RequestBody Hawb hawb) {
        try {
            if (hawb.getMawb() == null || hawb.getMawb().getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Se requiere asociar una MAWB válida."));
            }

            double weight = hawb.getWeightKg() != null ? hawb.getWeightKg().doubleValue() : 0.0;
            int pieces = hawb.getPieces() != null ? hawb.getPieces() : 0;

            // Ejecutar validaciones estructurales de manifiesto consolidador
            validationService.validateHawbConsolidation(hawb.getMawb().getId(), pieces, weight);

            Hawb savedHawb = hawbRepository.save(hawb);
            return ResponseEntity.ok(savedHawb);

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(422).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error en persistencia de HAWB: " + ex.getMessage()));
        }
    }
}
