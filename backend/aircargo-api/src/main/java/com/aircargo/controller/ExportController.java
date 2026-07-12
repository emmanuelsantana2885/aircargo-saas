package com.aircargo.controller;

import com.aircargo.dto.LoadPlanningBatchImportResultDTO;
import com.aircargo.service.ExportService;
import com.aircargo.service.LoadPlanningBatchImportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

    private final ExportService exportService;
    private final LoadPlanningBatchImportService batchImportService;

    public ExportController(ExportService exportService,
                            LoadPlanningBatchImportService batchImportService) {
        this.exportService = exportService;
        this.batchImportService = batchImportService;
    }

    @GetMapping("/{type}")
    public ResponseEntity<?> export(@PathVariable String type,
                                     @RequestParam(required = false) String format,
                                     @RequestParam(required = false) LocalDate dateFrom,
                                     @RequestParam(required = false) LocalDate dateTo,
                                     @RequestParam(defaultValue = "false") boolean audit) {
        String fmt = format != null ? format.toLowerCase() : "csv";

        if ("json".equals(fmt)) {
            List<Map<String, Object>> data = exportService.exportJson(type, dateFrom, dateTo, audit);
            return ResponseEntity.ok(data);
        }

        ByteArrayInputStream data = exportService.exportCsvPublic(type, fmt, dateFrom, dateTo, audit);
        String prefix = audit ? "AUDIT_" : "";
        String filename = prefix + type + "_" + LocalDate.now() + "." + fmt;
        String contentType = "csv".equals(fmt) ? "text/csv" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(data));
    }

    @PostMapping("/import/load-planning")
    public ResponseEntity<?> importLoadPlanning(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Archivo vacio"));
            }
            LoadPlanningBatchImportResultDTO result = batchImportService.importBatch(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
