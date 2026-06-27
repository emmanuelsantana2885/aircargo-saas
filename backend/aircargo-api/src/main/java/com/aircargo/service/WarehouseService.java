package com.aircargo.service;

import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseReceiptRepository receiptRepository;
    private final ReceiptPieceRepository pieceRepository;
    private final MawbRepository mawbRepository;
    private final ObjectMapper objectMapper;
    private final PdfGenerationService pdfService;

    public WarehouseService(WarehouseReceiptRepository receiptRepository,
                            ReceiptPieceRepository pieceRepository,
                            MawbRepository mawbRepository,
                            ObjectMapper objectMapper,
                            PdfGenerationService pdfService) {
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
        this.mawbRepository = mawbRepository;
        this.objectMapper = objectMapper;
        this.pdfService = pdfService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Transactional
    public WarehouseReceipt processWarehouseReceipt(WarehouseReceipt receipt, List<ReceiptPiece> pieces) {
        return processWarehouseReceipt(receipt, pieces, null);
    }

    @Transactional
    public WarehouseReceipt updateWarehouseReceipt(UUID receiptId, WarehouseReceipt receipt, List<ReceiptPiece> pieces, List<Map<String, String>> supportingDocs) {
        WarehouseReceipt existing = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        existing.setShipperName(receipt.getShipperName());
        existing.setConsigneeName(receipt.getConsigneeName());
        existing.setOrigin(receipt.getOrigin());
        existing.setDestination(receipt.getDestination());
        existing.setAwbReportedPieces(receipt.getAwbReportedPieces());
        existing.setMawbWeightGreatest(receipt.getMawbWeightGreatest());
        existing.setPieceCount(receipt.getPieceCount());
        existing.setCashOnly(receipt.getCashOnly());
        existing.setBookedInAcoms(receipt.getBookedInAcoms());
        existing.setDocsProvided(receipt.getDocsProvided());
        existing.setCustomsCompleted(receipt.getCustomsCompleted());
        existing.setPreBuilt(receipt.getPreBuilt());
        existing.setRemarks(receipt.getRemarks());
        existing.setDockSignature(receipt.getDockSignature());
        existing.setPrintName(receipt.getPrintName());
        existing.setDeliveredByName(receipt.getDeliveredByName());
        existing.setDeliveredByIdNum(receipt.getDeliveredByIdNum());
        existing.setDeliveredBySigUrl(receipt.getDeliveredBySigUrl());
        existing.setReceivedByName(receipt.getReceivedByName());
        existing.setReceivedBySigUrl(receipt.getReceivedBySigUrl());
        existing.setBrokerName(receipt.getBrokerName());
        existing.setBrokerIdNum(receipt.getBrokerIdNum());
        existing.setBrokerSigUrl(receipt.getBrokerSigUrl());

        if (supportingDocs != null && !supportingDocs.isEmpty()) {
            try {
                existing.setSupportingDocs(objectMapper.writeValueAsString(supportingDocs));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializando documentos de soporte", e);
            }
        }

        WarehouseReceipt savedReceipt = receiptRepository.save(existing);

        // Delete old pieces and replace with new ones (non-cumulative)
        pieceRepository.deleteByReceiptId(receiptId);

        double dimFactor = (savedReceipt.getDimFactorIntl() != null) ? savedReceipt.getDimFactorIntl().doubleValue() : 366.0;
        BigDecimal lbsToKgFactor = BigDecimal.valueOf(0.45359237);

        for (ReceiptPiece piece : pieces) {
            piece.setId(null);
            piece.setReceipt(savedReceipt);

            double length = piece.getLengthIn() != null ? piece.getLengthIn().doubleValue() : 0.0;
            double width = piece.getWidthIn() != null ? piece.getWidthIn().doubleValue() : 0.0;
            double height = piece.getHeightIn() != null ? piece.getHeightIn().doubleValue() : 0.0;

            double volWeightLbs = (length * width * height) / dimFactor;
            piece.setDimWeightLbs(BigDecimal.valueOf(volWeightLbs).setScale(3, RoundingMode.HALF_UP));

            BigDecimal dimWeightKg = piece.getDimWeightLbs().multiply(lbsToKgFactor);
            piece.setDimWeightKg(dimWeightKg.setScale(3, RoundingMode.HALF_UP));

            if (piece.getScaleWeightLbs() != null && (piece.getScaleWeightKg() == null || piece.getScaleWeightKg().compareTo(BigDecimal.ZERO) == 0)) {
                piece.setScaleWeightKg(piece.getScaleWeightLbs().multiply(lbsToKgFactor).setScale(3, RoundingMode.HALF_UP));
            }

            BigDecimal scaleKg = piece.getScaleWeightKg() != null ? piece.getScaleWeightKg() : BigDecimal.ZERO;
            BigDecimal maxKg = scaleKg.max(piece.getDimWeightKg());
            piece.setChargeableKg(maxKg.setScale(3, RoundingMode.HALF_UP));

            BigDecimal maxLbs = piece.getScaleWeightLbs().max(piece.getDimWeightLbs());
            piece.setChargeableLbs(maxLbs.setScale(3, RoundingMode.HALF_UP));

            pieceRepository.save(piece);
        }

        return savedReceipt;
    }

    @Transactional
    public WarehouseReceipt processWarehouseReceipt(WarehouseReceipt receipt, List<ReceiptPiece> pieces, List<Map<String, String>> supportingDocs) {
        if (supportingDocs != null && !supportingDocs.isEmpty()) {
            try {
                receipt.setSupportingDocs(objectMapper.writeValueAsString(supportingDocs));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializando documentos de soporte", e);
            }
        }

        WarehouseReceipt savedReceipt = receiptRepository.save(receipt);

        if (savedReceipt.getMawb() != null && savedReceipt.getMawb().getId() != null) {
            mawbRepository.findById(savedReceipt.getMawb().getId()).ifPresent(mawb -> {
                mawb.setStatus(MawbStatus.RECEIVED);
                mawbRepository.save(mawb);
            });
        }

        double dimFactor = (receipt.getDimFactorIntl() != null) ? receipt.getDimFactorIntl().doubleValue() : 366.0;
        BigDecimal lbsToKgFactor = BigDecimal.valueOf(0.45359237);

        for (ReceiptPiece piece : pieces) {
            piece.setReceipt(savedReceipt);

            double length = piece.getLengthIn() != null ? piece.getLengthIn().doubleValue() : 0.0;
            double width = piece.getWidthIn() != null ? piece.getWidthIn().doubleValue() : 0.0;
            double height = piece.getHeightIn() != null ? piece.getHeightIn().doubleValue() : 0.0;

            double volWeightLbs = (length * width * height) / dimFactor;
            piece.setDimWeightLbs(BigDecimal.valueOf(volWeightLbs).setScale(3, RoundingMode.HALF_UP));

            BigDecimal dimWeightKg = piece.getDimWeightLbs().multiply(lbsToKgFactor);
            piece.setDimWeightKg(dimWeightKg.setScale(3, RoundingMode.HALF_UP));

            if (piece.getScaleWeightLbs() != null && (piece.getScaleWeightKg() == null || piece.getScaleWeightKg().compareTo(BigDecimal.ZERO) == 0)) {
                piece.setScaleWeightKg(piece.getScaleWeightLbs().multiply(lbsToKgFactor).setScale(3, RoundingMode.HALF_UP));
            }

            BigDecimal scaleKg = piece.getScaleWeightKg() != null ? piece.getScaleWeightKg() : BigDecimal.ZERO;
            BigDecimal maxKg = scaleKg.max(piece.getDimWeightKg());
            piece.setChargeableKg(maxKg.setScale(3, RoundingMode.HALF_UP));

            BigDecimal maxLbs = piece.getScaleWeightLbs().max(piece.getDimWeightLbs());
            piece.setChargeableLbs(maxLbs.setScale(3, RoundingMode.HALF_UP));

            pieceRepository.save(piece);
        }

        return savedReceipt;
    }

    public String generateSupportingDocsHtml(UUID receiptId) {
        WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        String rawDocs = receipt.getSupportingDocs();
        if (rawDocs == null || rawDocs.isBlank() || "[]".equals(rawDocs)) {
            return "<html><body style='font-family:monospace;padding:2rem;color:#333'><h2>Sin evidencias documentales</h2><p>Este recibo no tiene documentos de soporte asociados.</p></body></html>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<title>Evidencias Documentales - ").append(receiptId.toString().substring(0, 8)).append("</title>");
        sb.append("<style>");
        sb.append("*{margin:0;padding:0;box-sizing:border-box}");
        sb.append("body{font-family:'Courier New',monospace;background:#f5f5f5;color:#1a1a1a;padding:2rem}");
        sb.append(".header{max-width:900px;margin:0 auto 2rem;padding:1.5rem;background:#fff;border:1px solid #ddd;border-left:4px solid #1a1a1a}");
        sb.append(".header h1{font-size:1.2rem;text-transform:uppercase;letter-spacing:0.05em}");
        sb.append(".header p{font-size:0.75rem;color:#666;margin-top:0.25rem}");
        sb.append(".grid{max-width:900px;margin:0 auto;display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:1rem}");
        sb.append(".card{background:#fff;border:1px solid #e0e0e0;border-radius:4px;overflow:hidden;break-inside:avoid}");
        sb.append(".card img{width:100%;height:200px;object-fit:cover;display:block;border-bottom:1px solid #eee}");
        sb.append(".card .doc-icon{width:100%;height:120px;display:flex;align-items:center;justify-content:center;background:#fafafa;border-bottom:1px solid #eee;font-size:2.5rem;color:#999}");
        sb.append(".card .info{padding:0.6rem;font-size:0.7rem;color:#555;text-transform:uppercase;letter-spacing:0.03em}");
        sb.append(".footer{max-width:900px;margin:2rem auto 0;padding:1rem 1.5rem;background:#fff;border:1px solid #ddd;font-size:0.65rem;color:#999;text-align:center;text-transform:uppercase;letter-spacing:0.05em}");
        sb.append("@media print{body{background:#fff;padding:0}.header,.card,.footer{border-color:#ccc;box-shadow:none}.card{break-inside:avoid}}");
        sb.append("</style></head><body>");

        String mawbInfo = "";
        if (receipt.getMawb() != null) {
            mawbInfo = "MAWB: " + (receipt.getMawb().getAwbNumber() != null ? receipt.getMawb().getAwbNumber() : "—");
        }
        sb.append("<div class='header'><h1>Evidencias Documentales</h1>");
        sb.append("<p>Recibo: ").append(receiptId.toString().substring(0, 8)).append(" &middot; ").append(mawbInfo);
        sb.append(" &middot; ").append(receipt.getShipperName() != null ? receipt.getShipperName() : "").append("</p></div>");

        sb.append("<div class='grid'>");
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> docs = objectMapper.readValue(rawDocs, List.class);
            for (Map<String, String> doc : docs) {
                String name = doc.getOrDefault("name", "Documento");
                String type = doc.getOrDefault("type", "document");
                String url = doc.getOrDefault("url", "");
                sb.append("<div class='card'>");
                if ("image".equals(type) && url != null && !url.isEmpty()) {
                    sb.append("<img src='").append(url).append("' alt='").append(name).append("' loading='lazy' />");
                } else {
                    sb.append("<div class='doc-icon'>&#128196;</div>");
                }
                sb.append("<div class='info'>").append(name).append("</div>");
                sb.append("</div>");
            }
        } catch (Exception e) {
            sb.append("<p>Error al procesar evidencias</p>");
        }
        sb.append("</div>");
        sb.append("<div class='footer'>AirCargo &mdash; Documento generado el ").append(java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16)).append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    public byte[] generateSupportingDocsPdf(UUID receiptId) {
        WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'/>");
        sb.append("<meta name='viewport' content='width=device-width,initial-scale=1.0'/>");
        sb.append("<title>Evidencias - ").append(receiptId.toString().substring(0, 8)).append("</title>");
        sb.append("<style>");
        sb.append("@page{margin:1cm}");
        sb.append("body{font-family:Helvetica,Arial,sans-serif;color:#1a1a1a;font-size:10pt;margin:0;padding:0}");
        sb.append(".page{page-break-after:always;display:flex;flex-direction:column;align-items:center;min-height:100vh;box-sizing:border-box;padding:0.5cm 1cm 1cm}");
        sb.append(".page:last-child{page-break-after:auto}");
        sb.append(".page-header{width:100%;border-bottom:2px solid #333;padding-bottom:0.3cm;margin-bottom:0.5cm;text-align:center}");
        sb.append(".page-header h2{font-size:12pt;margin:0;color:#1a1a1a}");
        sb.append(".page-header .meta{font-size:7pt;color:#555;margin-top:0.15cm}");
        sb.append(".page img{display:block;margin:0 auto;max-width:100%;max-height:75vh;object-fit:contain}");
        sb.append(".page .placeholder{font-size:10pt;color:#999;text-align:center;padding:3cm 1cm}");
        sb.append(".footer-text{font-size:7pt;color:#999;text-align:center;margin-top:auto;padding-top:0.5cm;width:100%;border-top:1px solid #ddd}");
        sb.append(".sig-card{width:100%;border:1px solid #ccc;border-radius:4px;padding:0.4cm 0.5cm;margin-bottom:0.3cm;background:#fafafa}");
        sb.append(".sig-card h3{font-size:10pt;margin:0 0 0.15cm 0;color:#333;border-bottom:1px solid #ddd;padding-bottom:0.1cm}");
        sb.append(".sig-row{display:flex;gap:0.3cm;margin-top:0.2cm}");
        sb.append(".sig-field{flex:1}");
        sb.append(".sig-field label{font-size:7pt;color:#666;text-transform:uppercase;display:block;margin-bottom:0.05cm}");
        sb.append(".sig-field .value{font-size:9pt;color:#1a1a1a;font-weight:bold}");
        sb.append(".sig-img{max-height:1.2cm;max-width:5cm;display:block;margin:0.1cm 0;border:1px solid #eee;padding:0.1cm;background:white}");
        sb.append(".sig-grid{display:grid;grid-template-columns:1fr 1fr;gap:0.4cm;width:100%}");
        sb.append("</style></head><body>");

        String mawbInfo = "";
        String shipperInfo = "";
        if (receipt.getMawb() != null) {
            mawbInfo = xmlEscape(receipt.getMawb().getAwbNumber() != null ? receipt.getMawb().getAwbNumber() : "\u2014");
        }
        if (receipt.getShipperName() != null) {
            shipperInfo = xmlEscape(receipt.getShipperName());
        }

        String footer = "AirCargo \u2014 generado " + java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16);

        // ── Page 1: Signature Evidence ──
        sb.append("<div class='page'>");
        sb.append("<div class='page-header'><h2>Documentaci\u00f3n de Evidencias \u2014 Firmas</h2>");
        sb.append("<div class='meta'>MAWB: ").append(mawbInfo).append(" &#183; ").append(shipperInfo).append("</div></div>");

        sb.append("<div class='sig-grid'>");

        // Received by
        sb.append("<div class='sig-card'>");
        sb.append("<h3>Recibido por (Almac\u00e9n)</h3>");
        sb.append("<div class='sig-row'><div class='sig-field'><label>Nombre</label><div class='value'>").append(xmlEscape(receipt.getPrintName() != null ? receipt.getPrintName() : (receipt.getReceivedByName() != null ? receipt.getReceivedByName() : "\u2014"))).append("</div></div></div>");
        if (receipt.getDockSignature() != null && !receipt.getDockSignature().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getDockSignature()).append("' alt='Firma Recibido' />");
        } else if (receipt.getReceivedBySigUrl() != null && !receipt.getReceivedBySigUrl().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getReceivedBySigUrl()).append("' alt='Firma Recibido' />");
        }
        sb.append("</div>");

        // Delivered by
        sb.append("<div class='sig-card'>");
        sb.append("<h3>Entregado por (Transportista)</h3>");
        sb.append("<div class='sig-row'>");
        sb.append("<div class='sig-field'><label>Nombre</label><div class='value'>").append(xmlEscape(receipt.getDeliveredByName() != null ? receipt.getDeliveredByName() : "\u2014")).append("</div></div>");
        sb.append("<div class='sig-field'><label>ID / C\u00e9dula</label><div class='value'>").append(xmlEscape(receipt.getDeliveredByIdNum() != null ? receipt.getDeliveredByIdNum() : "\u2014")).append("</div></div>");
        sb.append("</div>");
        if (receipt.getDeliveredBySigUrl() != null && !receipt.getDeliveredBySigUrl().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getDeliveredBySigUrl()).append("' alt='Firma Entregado' />");
        }
        sb.append("</div>");

        // Broker representative
        sb.append("<div class='sig-card'>");
        sb.append("<h3>Representante de Broker / Agente de Carga</h3>");
        sb.append("<div class='sig-row'>");
        sb.append("<div class='sig-field'><label>Nombre</label><div class='value'>").append(xmlEscape(receipt.getBrokerName() != null ? receipt.getBrokerName() : "\u2014")).append("</div></div>");
        sb.append("<div class='sig-field'><label>ID / C\u00e9dula</label><div class='value'>").append(xmlEscape(receipt.getBrokerIdNum() != null ? receipt.getBrokerIdNum() : "\u2014")).append("</div></div>");
        sb.append("</div>");
        if (receipt.getBrokerSigUrl() != null && !receipt.getBrokerSigUrl().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getBrokerSigUrl()).append("' alt='Firma Broker' />");
        }
        sb.append("</div>");

        sb.append("</div>"); // end sig-grid
        sb.append("<div class='footer-text'>").append(footer).append("</div>");
        sb.append("</div>"); // end page

        // ── Evidence pages ──
        String rawDocs = receipt.getSupportingDocs();
        if (rawDocs != null && !rawDocs.isBlank() && !"[]".equals(rawDocs)) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> docs = objectMapper.readValue(rawDocs, List.class);
                int idx = 0;
                for (Map<String, String> doc : docs) {
                    idx++;
                    String name = xmlEscape(doc.getOrDefault("name", "Documento"));
                    String type = doc.getOrDefault("type", "document");
                    String url = doc.getOrDefault("url", "");

                    sb.append("<div class='page'>");
                    sb.append("<div class='page-header'><h2>Evidencia ").append(idx).append(" / ").append(docs.size()).append("</h2>");
                    sb.append("<div class='meta'>").append(name).append(" &#183; ").append("MAWB: ").append(mawbInfo).append("</div></div>");

                    if ("image".equals(type) && url != null && !url.isEmpty()) {
                        sb.append("<img src='").append(url).append("' alt='").append(name).append("' />");
                    } else {
                        sb.append("<div class='placeholder'>&#128196; ").append(name).append("</div>");
                    }

                    sb.append("<div class='footer-text'>").append(footer).append("</div>");
                    sb.append("</div>");
                }
            } catch (Exception e) {
                sb.append("<p>Error al procesar evidencias</p>");
            }
        } else {
            sb.append("<div class='page'><div class='page-header'><h2>Evidencias</h2></div>");
            sb.append("<div class='placeholder'>Sin evidencias documentales adicionales</div>");
            sb.append("<div class='footer-text'>").append(footer).append("</div></div>");
        }

        sb.append("</body></html>");
        return pdfService.generatePdf(sb.toString());
    }

    private static String xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
