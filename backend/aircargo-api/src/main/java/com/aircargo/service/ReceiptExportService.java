package com.aircargo.service;

import com.aircargo.entity.Hawb;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.repository.HawbRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import com.aircargo.repository.WarehouseReceiptRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
public class ReceiptExportService {

    private final WarehouseReceiptRepository receiptRepository;
    private final ReceiptPieceRepository pieceRepository;
    private final HawbRepository hawbRepository;

    public ReceiptExportService(WarehouseReceiptRepository receiptRepository,
                                ReceiptPieceRepository pieceRepository,
                                HawbRepository hawbRepository) {
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
        this.hawbRepository = hawbRepository;
    }

    public ByteArrayInputStream exportReceipt(UUID receiptId) throws Exception {
        WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        List<ReceiptPiece> pieces = pieceRepository.findByReceiptId(receiptId);
        List<Hawb> hawbs = receipt.getMawb() != null
                ? hawbRepository.findByMawbId(receipt.getMawb().getId())
                : List.of();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DockReceipt");

            // ── STYLES ──
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 10);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 9);

            Font dataFont = workbook.createFont();
            dataFont.setFontHeightInPoints((short) 9);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle labelStyle = workbook.createCellStyle();
            labelStyle.setFont(boldFont);
            labelStyle.setBorderBottom(BorderStyle.THIN);
            labelStyle.setBorderLeft(BorderStyle.THIN);
            labelStyle.setBorderRight(BorderStyle.THIN);
            labelStyle.setBorderTop(BorderStyle.THIN);
            labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(dataFont);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);

            CellStyle rightDataStyle = workbook.createCellStyle();
            rightDataStyle.cloneStyleFrom(dataStyle);
            rightDataStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle centerDataStyle = workbook.createCellStyle();
            centerDataStyle.cloneStyleFrom(dataStyle);
            centerDataStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);

            CellStyle checkStyle = workbook.createCellStyle();
            checkStyle.setFont(dataFont);
            checkStyle.setBorderBottom(BorderStyle.THIN);
            checkStyle.setBorderLeft(BorderStyle.THIN);
            checkStyle.setBorderRight(BorderStyle.THIN);
            checkStyle.setBorderTop(BorderStyle.THIN);
            checkStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            checkStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle totalLabelStyle = workbook.createCellStyle();
            totalLabelStyle.setFont(boldFont);
            totalLabelStyle.setBorderBottom(BorderStyle.THIN);
            totalLabelStyle.setBorderLeft(BorderStyle.THIN);
            totalLabelStyle.setBorderRight(BorderStyle.THIN);
            totalLabelStyle.setBorderTop(BorderStyle.THIN);
            totalLabelStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle totalDataStyle = workbook.createCellStyle();
            totalDataStyle.setFont(boldFont);
            totalDataStyle.setBorderBottom(BorderStyle.THIN);
            totalDataStyle.setBorderLeft(BorderStyle.THIN);
            totalDataStyle.setBorderRight(BorderStyle.THIN);
            totalDataStyle.setBorderTop(BorderStyle.THIN);
            totalDataStyle.setAlignment(HorizontalAlignment.RIGHT);

            String awbNum = receipt.getMawb() != null ? receipt.getMawb().getAwbNumber() : "—";
            String mawbIdShort = receipt.getMawb() != null ? receipt.getMawb().getId().toString().substring(0, 8) : "—";
            String yes = "✓";
            String no = "☐";

            int r = 0;

            // ═══ ROW 1: TITLE ═══
            Row titleRow = sheet.createRow(r++);
            Cell t = titleRow.createCell(0);
            t.setCellValue("UPS Air Cargo Dock Receipt");
            t.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 13));

            // ═══ ROW 2: blank ═══
            r++;

            // ═══ ROW 3: Gateway/CFS ═══
            Row row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Gateway/ CFS Name:");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(2); // blank spacer
            row.createCell(3).setCellValue(receipt.getGatewayCfs() != null ? receipt.getGatewayCfs() : "SDQ");
            row.getCell(3).setCellStyle(dataStyle);

            // ═══ ROW 4: Shipper Name ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Shipper Name:");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(3).setCellValue(receipt.getShipperName() != null ? receipt.getShipperName() : "—");
            row.getCell(3).setCellStyle(dataStyle);

            // ═══ ROW 5: MAWB Number + Cash Only ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("MAWB Number:");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(3).setCellValue(awbNum);
            row.getCell(3).setCellStyle(dataStyle);
            row.createCell(5).setCellValue("Cash Only:");
            row.getCell(5).setCellStyle(labelStyle);
            row.createCell(6).setCellValue(Boolean.TRUE.equals(receipt.getCashOnly()) ? yes : no);
            row.getCell(6).setCellStyle(checkStyle);

            // ═══ ROW 6: Origin + Booked in ACOMS ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Origin:");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(3).setCellValue(receipt.getOrigin() != null ? receipt.getOrigin() : "—");
            row.getCell(3).setCellStyle(dataStyle);
            row.createCell(5).setCellValue("Booked in ACOMS:");
            row.getCell(5).setCellStyle(labelStyle);
            row.createCell(6).setCellValue(Boolean.TRUE.equals(receipt.getBookedInAcoms()) ? yes : no);
            row.getCell(6).setCellStyle(checkStyle);

            // ═══ ROW 7: Destination + Documents Provided ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Destination:");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(3).setCellValue(receipt.getDestination() != null ? receipt.getDestination() : "—");
            row.getCell(3).setCellStyle(dataStyle);
            row.createCell(5).setCellValue("Documents Provided:");
            row.getCell(5).setCellStyle(labelStyle);
            row.createCell(6).setCellValue(Boolean.TRUE.equals(receipt.getDocsProvided()) ? yes : no);
            row.getCell(6).setCellStyle(checkStyle);

            // ═══ ROW 8: AWB Reported Piece + Customs Completed ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("AWB Reported Piece:");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(3).setCellValue(receipt.getAwbReportedPieces() != null ? receipt.getAwbReportedPieces().doubleValue() : 0);
            row.getCell(3).setCellStyle(dataStyle);
            row.createCell(5).setCellValue("Export Customs Completed:");
            row.getCell(5).setCellStyle(labelStyle);
            row.createCell(6).setCellValue(Boolean.TRUE.equals(receipt.getCustomsCompleted()) ? yes : no);
            row.getCell(6).setCellStyle(checkStyle);

            // ═══ ROW 9: MAWB Weight + Pre-built ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("MAWB Weight (Greatest Shipper Reported Weight):");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(5).setCellValue("Pre-built / Shipper Built:");
            row.getCell(5).setCellStyle(labelStyle);
            row.createCell(6).setCellValue(Boolean.TRUE.equals(receipt.getPreBuilt()) ? yes : no);
            row.getCell(6).setCellStyle(checkStyle);

            // ═══ ROW 10: Weight value ═══
            row = sheet.createRow(r++);
            row.createCell(3).setCellValue(receipt.getMawbWeightGreatest() != null ? receipt.getMawbWeightGreatest().doubleValue() : 0.0);
            row.getCell(3).setCellStyle(dataStyle);

            r++; // blank row

            // ═══ HAWB BREAKDOWN (if any) ═══
            if (!hawbs.isEmpty()) {
                Row hawbTitle = sheet.createRow(r++);
                Cell ht = hawbTitle.createCell(1);
                ht.setCellValue("Desglose por HAWB (" + hawbs.size() + ")");
                ht.setCellStyle(totalLabelStyle);

                Row hawbHeader = sheet.createRow(r++);
                String[] hawbCols = {"", "HAWB #", "Consignee", "Dest", "Pieces", "Weight (kg)", "Commodity"};
                for (int c = 1; c < hawbCols.length; c++) {
                    Cell cell = hawbHeader.createCell(c);
                    cell.setCellValue(hawbCols[c]);
                    cell.setCellStyle(headerCellStyle);
                }
                for (Hawb h : hawbs) {
                    Row hawbRow = sheet.createRow(r++);
                    hawbRow.createCell(1).setCellValue(h.getHawbNumber() != null ? h.getHawbNumber() : "—");
                    hawbRow.getCell(1).setCellStyle(dataStyle);
                    hawbRow.createCell(2).setCellValue(h.getConsigneeName() != null ? h.getConsigneeName() : "—");
                    hawbRow.getCell(2).setCellStyle(dataStyle);
                    hawbRow.createCell(3).setCellValue(h.getDestination() != null ? h.getDestination() : "—");
                    hawbRow.getCell(3).setCellStyle(centerDataStyle);
                    hawbRow.createCell(4).setCellValue(h.getPieces() != null ? h.getPieces() : 0);
                    hawbRow.getCell(4).setCellStyle(rightDataStyle);
                    hawbRow.createCell(5).setCellValue(h.getWeightKg() != null ? h.getWeightKg().doubleValue() : 0.0);
                    hawbRow.getCell(5).setCellStyle(rightDataStyle);
                    hawbRow.createCell(6).setCellValue(h.getCommodityType() != null ? h.getCommodityType().toString() : "—");
                    hawbRow.getCell(6).setCellStyle(centerDataStyle);
                }
                r++;
            }

            // ═══ LOOSE TENDER / PIECES TABLE ═══
            Row ltRow = sheet.createRow(r++);
            Cell lt = ltRow.createCell(1);
            lt.setCellValue("Loose Tender:");
            lt.setCellStyle(labelStyle);

            // Piece Count
            int totalPieceCount = pieces.stream().mapToInt(p -> 1).sum();
            row = sheet.createRow(r++);
            row.createCell(2).setCellValue("Piece Count:");
            row.getCell(2).setCellStyle(labelStyle);
            row.createCell(3).setCellValue(totalPieceCount);
            row.getCell(3).setCellStyle(dataStyle);

            // IATA formula explanation
            row = sheet.createRow(r++);
            row.createCell(5).setCellValue("((L x W x H) x # pieces) / 194 pounds");
            row.getCell(5).setCellStyle(dataStyle);
            row = sheet.createRow(r++);
            row.createCell(5).setCellValue("((L x W x H) x # pieces) / 366 kilos");
            row.getCell(5).setCellStyle(dataStyle);

            r++; // blank row

            // Column headers matching the reference template
            Row piecesHeader = sheet.createRow(r++);
            String[] pieceCols = {"", "Pieces", "Length", "Width", "Height", "Dim Weight", "Scale Weight in LBS", "Dim Wight for LBS", "Scale Weight in KGS", "Dim Weight in KGS", "INTL Chargeable WT In KGS", "DOM CHG WT"};
            for (int c = 1; c < pieceCols.length; c++) {
                Cell cell = piecesHeader.createCell(c);
                cell.setCellValue(pieceCols[c]);
                cell.setCellStyle(headerCellStyle);
            }

            double totalScaleLbs = 0, totalDimLbs = 0, totalScaleKg = 0, totalDimKg = 0;
            int pieceNum = 1;
            for (int pi = 0; pi < Math.max(pieces.size(), 1); pi++) {
                row = sheet.createRow(r++);
                row.createCell(0).setCellValue(pieceNum);
                row.getCell(0).setCellStyle(centerDataStyle);
                if (pi < pieces.size()) {
                    ReceiptPiece p = pieces.get(pi);
                    row.createCell(1).setCellValue(1);
                    row.getCell(1).setCellStyle(centerDataStyle);
                    row.createCell(2).setCellValue(p.getLengthIn() != null ? p.getLengthIn().doubleValue() : 0.0);
                    row.getCell(2).setCellStyle(rightDataStyle);
                    row.createCell(3).setCellValue(p.getWidthIn() != null ? p.getWidthIn().doubleValue() : 0.0);
                    row.getCell(3).setCellStyle(rightDataStyle);
                    row.createCell(4).setCellValue(p.getHeightIn() != null ? p.getHeightIn().doubleValue() : 0.0);
                    row.getCell(4).setCellStyle(rightDataStyle);
                    row.createCell(6).setCellValue(p.getScaleWeightLbs() != null ? p.getScaleWeightLbs().doubleValue() : 0.0);
                    row.getCell(6).setCellStyle(rightDataStyle);
                    row.createCell(7).setCellValue(p.getDimWeightLbs() != null ? p.getDimWeightLbs().doubleValue() : 0.0);
                    row.getCell(7).setCellStyle(rightDataStyle);
                    row.createCell(8).setCellValue(p.getScaleWeightKg() != null ? p.getScaleWeightKg().doubleValue() : 0.0);
                    row.getCell(8).setCellStyle(rightDataStyle);
                    row.createCell(9).setCellValue(p.getDimWeightKg() != null ? p.getDimWeightKg().doubleValue() : 0.0);
                    row.getCell(9).setCellStyle(rightDataStyle);
                    row.createCell(10).setCellValue(p.getChargeableKg() != null ? p.getChargeableKg().doubleValue() : 0.0);
                    row.getCell(10).setCellStyle(rightDataStyle);
                    row.createCell(11).setCellValue(p.getChargeableLbs() != null ? p.getChargeableLbs().doubleValue() : 0.0);
                    row.getCell(11).setCellStyle(rightDataStyle);
                    totalScaleLbs += p.getScaleWeightLbs() != null ? p.getScaleWeightLbs().doubleValue() : 0;
                    totalDimLbs += p.getDimWeightLbs() != null ? p.getDimWeightLbs().doubleValue() : 0;
                    totalScaleKg += p.getScaleWeightKg() != null ? p.getScaleWeightKg().doubleValue() : 0;
                    totalDimKg += p.getDimWeightKg() != null ? p.getDimWeightKg().doubleValue() : 0;
                } else {
                    for (int c = 1; c <= 11; c++) {
                        row.createCell(c).setCellStyle(dataStyle);
                    }
                }
                pieceNum++;
            }

            // Totals row
            Row totalRow = sheet.createRow(r++);
            totalRow.createCell(0).setCellStyle(centerDataStyle);
            totalRow.createCell(1).setCellStyle(totalLabelStyle);
            for (int c = 2; c <= 5; c++) { totalRow.createCell(c).setCellStyle(totalLabelStyle); }
            totalRow.createCell(6).setCellValue(totalScaleLbs);
            totalRow.getCell(6).setCellStyle(totalDataStyle);
            totalRow.createCell(7).setCellValue(totalDimLbs);
            totalRow.getCell(7).setCellStyle(totalDataStyle);
            totalRow.createCell(8).setCellValue(totalScaleKg);
            totalRow.getCell(8).setCellStyle(totalDataStyle);
            totalRow.createCell(9).setCellValue(totalDimKg);
            totalRow.getCell(9).setCellStyle(totalDataStyle);
            totalRow.createCell(10).setCellValue(Math.max(totalDimKg, totalScaleKg));
            totalRow.getCell(10).setCellStyle(totalDataStyle);
            totalRow.createCell(11).setCellValue(Math.max(totalDimLbs, totalScaleLbs));
            totalRow.getCell(11).setCellStyle(totalDataStyle);

            r++;

            // Actual weight
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Actual weight of this shipment is: ");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(4).setCellValue(totalScaleKg);
            row.getCell(4).setCellStyle(totalDataStyle);
            row.createCell(5).setCellValue("KGS");
            row.getCell(5).setCellStyle(boldFontStyle(workbook));
            row.createCell(6).setCellValue(totalScaleLbs);
            row.getCell(6).setCellStyle(totalDataStyle);
            row.createCell(7).setCellValue("LBS");
            row.getCell(7).setCellStyle(boldFontStyle(workbook));

            // Chargeable weight
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Chargeable weight of this shipment is: ");
            row.getCell(1).setCellStyle(labelStyle);
            double chgKg = Math.max(totalDimKg, totalScaleKg);
            double chgLbs = Math.max(totalDimLbs, totalScaleLbs);
            row.createCell(4).setCellValue(chgKg);
            row.getCell(4).setCellStyle(totalDataStyle);
            row.createCell(5).setCellValue("KGS");
            row.getCell(5).setCellStyle(boldFontStyle(workbook));
            row.createCell(6).setCellValue(chgLbs);
            row.getCell(6).setCellStyle(totalDataStyle);
            row.createCell(7).setCellValue("LBS");
            row.getCell(7).setCellStyle(boldFontStyle(workbook));

            r++;

            // ═══ REMARKS ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Remarks:");
            row.getCell(1).setCellStyle(labelStyle);
            String remarks = receipt.getRemarks() != null ? receipt.getRemarks() : "";
            if (!remarks.isEmpty()) {
                row.createCell(2).setCellValue(remarks);
                row.getCell(2).setCellStyle(dataStyle);
            }

            r++;

            // ═══ SIGNATURES ═══
            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Dock Signature:");
            row.getCell(1).setCellStyle(labelStyle);
            String dockSig = (receipt.getDockSignature() != null && !receipt.getDockSignature().isEmpty())
                    ? "✓ Firmado" : "—";
            row.createCell(2).setCellValue(dockSig);
            row.getCell(2).setCellStyle(dataStyle);

            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Print Name:");
            row.getCell(1).setCellStyle(labelStyle);
            row.createCell(2).setCellValue(receipt.getPrintName() != null ? receipt.getPrintName() : "—");
            row.getCell(2).setCellStyle(dataStyle);
            row.createCell(5).setCellValue("Date / Time:");
            row.getCell(5).setCellStyle(labelStyle);
            row.createCell(6).setCellValue(receipt.getReceiptDate() != null ? receipt.getReceiptDate().toString().replace("T", " ").substring(0, 19) : "—");
            row.getCell(6).setCellStyle(dataStyle);

            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Delivered By:");
            row.getCell(1).setCellStyle(labelStyle);
            String delInfo = (receipt.getDeliveredByName() != null ? receipt.getDeliveredByName() : "—")
                    + " / " + (receipt.getDeliveredByIdNum() != null ? receipt.getDeliveredByIdNum() : "—");
            row.createCell(2).setCellValue(delInfo);
            row.getCell(2).setCellStyle(dataStyle);

            row = sheet.createRow(r++);
            row.createCell(1).setCellValue("Broker:");
            row.getCell(1).setCellStyle(labelStyle);
            String brokerInfo = (receipt.getBrokerName() != null ? receipt.getBrokerName() : "—")
                    + " / " + (receipt.getBrokerIdNum() != null ? receipt.getBrokerIdNum() : "—");
            row.createCell(2).setCellValue(brokerInfo);
            row.getCell(2).setCellStyle(dataStyle);

            // Auto-size columns
            for (int c = 0; c <= 11; c++) {
                sheet.autoSizeColumn(c);
            }
            // Ensure minimum width for key columns
            sheet.setColumnWidth(0, 1800);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(11, 3500);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private CellStyle boldFontStyle(Workbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        CellStyle s = wb.createCellStyle();
        s.setFont(f);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setAlignment(HorizontalAlignment.LEFT);
        return s;
    }
}
