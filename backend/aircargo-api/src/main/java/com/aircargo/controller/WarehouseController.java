package com.aircargo.controller;

import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import com.aircargo.service.ReceiptExportService;
import com.aircargo.service.WarehouseService;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouse/receipts")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseReceiptRepository receiptRepository;
    private final ReceiptPieceRepository pieceRepository;
    private final ReceiptExportService exportService;

    public WarehouseController(WarehouseService warehouseService, 
                               WarehouseReceiptRepository receiptRepository, 
                               ReceiptPieceRepository pieceRepository,
                               ReceiptExportService exportService) {
        this.warehouseService = warehouseService;
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
        this.exportService = exportService;
    }

    /**
     * DTO interno temporal para recibir de golpe el encabezado y sus piezas en el payload JSON.
     */
    public static class ReceiptPayload {
        public WarehouseReceipt receipt;
        public List<ReceiptPiece> pieces;
    }

    /**
     * Endpoint para emitir un nuevo recibo de bodega con cálculo en tiempo real de dimensiones.
     */
    @PostMapping("/emit")
    public ResponseEntity<?> emitWarehouseReceipt(@RequestBody ReceiptPayload payload) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "DATOS INCOMPLETOS: El recibo debe contener al menos una pieza para cubicar."));
            }

            WarehouseReceipt processed = warehouseService.processWarehouseReceipt(payload.receipt, payload.pieces);
            return ResponseEntity.ok(processed);

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error procesando el recibo en bodega: " + ex.getMessage()));
        }
    }

    /**
     * Endpoint para consultar el desglose de piezas cubicadas asociadas a un recibo.
     */
    @GetMapping("/{receiptId}/pieces")
    public ResponseEntity<List<ReceiptPiece>> getPiecesByReceipt(@PathVariable UUID receiptId) {
        return ResponseEntity.ok(pieceRepository.findByReceiptId(receiptId));
    }

    /**
     * Endpoint para exportar un recibo de bodega a Excel con desglose completo.
     */
    @GetMapping("/{receiptId}/export")
    public ResponseEntity<?> exportReceipt(@PathVariable UUID receiptId) {
        try {
            ByteArrayInputStream excel = exportService.exportReceipt(receiptId);
            String filename = "RECIBO_BODEGA_" + receiptId.toString().substring(0, 8) + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excel));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Error exportando recibo: " + ex.getMessage()));
        }
    }
}
