package com.aircargo.controller;

import com.aircargo.dto.LoadPlanningDTO;
import com.aircargo.entity.Uld;
import com.aircargo.repository.UldRepository;
import com.aircargo.service.LoadPlanningService;
import com.aircargo.service.RampManifestParserService;
import com.aircargo.service.LoadPlanningExportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/load-planning")
public class LoadPlanningController {

    private final UldRepository uldRepository;
    private final LoadPlanningService loadPlanningService;
    private final RampManifestParserService manifestParserService;
    private final LoadPlanningExportService exportService;

    public LoadPlanningController(UldRepository uldRepository,
                                  LoadPlanningService loadPlanningService,
                                  RampManifestParserService manifestParserService,
                                  LoadPlanningExportService exportService) {
        this.uldRepository = uldRepository;
        this.loadPlanningService = loadPlanningService;
        this.manifestParserService = manifestParserService;
        this.exportService = exportService;
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<?> getLoadPlanningByFlight(@PathVariable UUID flightId) {
        return loadPlanningService.getByFlightId(flightId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/flight/{flightId}/upload-manifest")
    @Transactional
    public ResponseEntity<?> uploadRampManifest(@PathVariable UUID flightId, 
                                                @RequestParam("airlineId") UUID airlineId,
                                                @RequestParam("file") MultipartFile file) {
        try {
            List<Uld> uldsExtraidos = manifestParserService.parseExcelToNativeUld(file, flightId, airlineId);
            uldRepository.saveAll(uldsExtraidos);
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", String.format("Éxito: Se inyectaron %d ULDs nativos al plan de vuelo.", uldsExtraidos.size())
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    /**
     * NUEVO ENDPOINT DE EXPORTACIÓN: Descarga el manifiesto consolidado en formato .XLSX
     */
    @GetMapping("/flight/{flightId}/export-manifest")
    public ResponseEntity<Resource> downloadRampManifest(@PathVariable UUID flightId) {
        try {
            ByteArrayInputStream in = exportService.exportFlightLoadPlan(flightId);
            InputStreamResource resource = new InputStreamResource(in);

            String filename = String.format("LOAD_PLAN_FLIGHT_%s.xlsx", flightId.toString().substring(0, 8));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
                    
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
