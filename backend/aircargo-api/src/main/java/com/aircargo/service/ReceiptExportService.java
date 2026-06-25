package com.aircargo.service;

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

    public ReceiptExportService(WarehouseReceiptRepository receiptRepository,
                                ReceiptPieceRepository pieceRepository,
                                HawbRepository hawbRepository) {
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
    }

    public ByteArrayInputStream exportReceipt(UUID receiptId) throws Exception {
        WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        List<ReceiptPiece> pieces = pieceRepository.findByReceiptId(receiptId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DockReceipt");

            Font tahoma10 = workbook.createFont();
            tahoma10.setFontHeightInPoints((short) 10);
            tahoma10.setFontName("Tahoma");

            Font tahoma10Bold = workbook.createFont();
            tahoma10Bold.setBold(true);
            tahoma10Bold.setFontHeightInPoints((short) 10);
            tahoma10Bold.setFontName("Tahoma");

            Font tahoma8 = workbook.createFont();
            tahoma8.setFontHeightInPoints((short) 8);
            tahoma8.setFontName("Tahoma");

            CellStyle labelRightStyle = style(workbook, tahoma10, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null);
            CellStyle valueStyle = style(workbook, tahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null);
            CellStyle titleStyle = style(workbook, tahoma10Bold, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null);
            CellStyle sectionStyle = style(workbook, tahoma10Bold, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null);
            CellStyle headerStyle = hdrStyle(workbook, tahoma10, IndexedColors.GREY_25_PERCENT);
            CellStyle dataStyle = style(workbook, tahoma10, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null);
            CellStyle dataNoBorderStyle = style(workbook, tahoma10, HorizontalAlignment.GENERAL, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null);
            CellStyle rowNumStyle = style(workbook, tahoma8, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null);
            CellStyle boldStyle = style(workbook, tahoma10Bold, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null);
            CellStyle weightLabelStyle = style(workbook, tahoma10Bold, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.LIGHT_YELLOW);
            CellStyle weightNumStyle = style(workbook, tahoma10Bold, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.LIGHT_YELLOW);
            CellStyle remarksLabelStyle = style(workbook, tahoma10, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null);
            CellStyle remarksValStyle = style(workbook, tahoma10, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null);
            CellStyle sigValueStyle = style(workbook, tahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null);

            String awbNum = receipt.getMawb() != null ? receipt.getMawb().getAwbNumber() : "\u2014";

            sheet.setColumnWidth(0, (int)(3.57 * 256));
            sheet.setColumnWidth(1, (int)(12.71 * 256));
            sheet.setColumnWidth(2, (int)(12.71 * 256));
            sheet.setColumnWidth(3, (int)(12.71 * 256));
            sheet.setColumnWidth(4, (int)(12.71 * 256));
            sheet.setColumnWidth(5, (int)(12.71 * 256));
            sheet.setColumnWidth(6, (int)(12.71 * 256));
            sheet.setColumnWidth(7, (int)(9.57 * 256));
            sheet.setColumnWidth(8, (int)(9.14 * 256));
            sheet.setColumnWidth(9, (int)(14.29 * 256));
            sheet.setColumnWidth(10, (int)(9.14 * 256));
            sheet.setColumnWidth(11, (int)(9.14 * 256));

            int r = 0;

            // Row 1: Title
            Row titleRow = sheet.createRow(r++);
            titleRow.setHeightInPoints(18);
            Cell t = titleRow.createCell(1);
            t.setCellValue("UPS Air Cargo Dock Receipt");
            t.setCellStyle(titleStyle);
            for (int c = 2; c <= 4; c++) titleRow.createCell(c).setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));

            // Row 2: blank
            sheet.createRow(r++).setHeightInPoints(18);

            // Rows 3-8: header fields
            r = headerRow(sheet, r, 13.5f, labelRightStyle, valueStyle, receipt, awbNum);

            // Rows 9-10: MAWB Weight (2-row label merged B-C, value D-E)
            Row r9 = sheet.createRow(r++);
            r9.setHeightInPoints(13.5f);
            Row r10 = sheet.createRow(r++);
            r10.setHeightInPoints(14.25f);
            double wtVal = receipt.getMawbWeightGreatest() != null ? receipt.getMawbWeightGreatest().doubleValue() : 0.0;
            Cell lbl = r9.createCell(1);
            lbl.setCellValue("MAWB Weight (Greatest Shipper Reported Weight):");
            lbl.setCellStyle(labelRightStyle);
            r9.createCell(2).setCellStyle(labelRightStyle);
            sheet.addMergedRegion(new CellRangeAddress(r - 2, r - 1, 1, 2));
            Cell val = r10.createCell(3);
            val.setCellValue(wtVal);
            val.setCellStyle(valueStyle);
            r10.createCell(4).setCellStyle(valueStyle);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 3, 4));

            // Rows 11-13: blank
            blank(sheet, r++, 5.25f);
            blank(sheet, r++, 6.75f);
            blank(sheet, r++, 6.75f);

            // Row 14: Loose Tender
            {
                Row rr = sheet.createRow(r++);
                rr.setHeightInPoints(13.5f);
                rr.createCell(1).setCellValue("Loose Tender:");
                rr.getCell(1).setCellStyle(sectionStyle);
            }

            // Row 15: Piece Count
            {
                Row rr = sheet.createRow(r++);
                rr.setHeightInPoints(13.5f);
                mergeLabel(sheet, rr, 3, 3, "Piece Count:", labelRightStyle);
                Cell pv = rr.createCell(4);
                pv.setCellValue(pieces.size());
                pv.setCellStyle(valueStyle);
                rr.createCell(5).setCellStyle(valueStyle);
                sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 5));
            }

            // Row 16: blank
            blank(sheet, r++, 6.75f);

            // Rows 17-18: dim formula notes
            {
                Row rr = sheet.createRow(r++);
                rr.setHeightInPoints(13.5f);
                rr.createCell(0).setCellValue(366);
                rr.getCell(0).setCellStyle(dataNoBorderStyle);
                mergeValue(sheet, rr, 4, 7, "[((L x W x H) x # pieces) / 194 pounds]", dataNoBorderStyle);
            }
            {
                Row rr = sheet.createRow(r++);
                rr.setHeightInPoints(13.5f);
                mergeValue(sheet, rr, 4, 7, "[((L x W x H) x # pieces) / 366 kilos]", dataNoBorderStyle);
            }

            // Rows 19-20: blank
            blank(sheet, r++, 6.75f);
            blank(sheet, r++, 6.75f);

            // Row 21: table header
            Row hdr = sheet.createRow(r++);
            hdr.setHeightInPoints(27.75f);
            String[] headers = {"", "Pieces", "Length", "Width", "Height", "Dim Weight", "Scale Weight\nin LBS", "Dim Wight\nfor LBS", "Scale Weight\nin KGS", "Dim Weight\nin KGS", "INTL Chargeable\nWT In KGS", "DOM CHG WT"};
            for (int c = 0; c < headers.length; c++) {
                Cell cell = hdr.createCell(c);
                cell.setCellStyle(headerStyle);
                if (c > 0) cell.setCellValue(headers[c]);
            }

            // Rows 22-46: 25 data rows
            double totalScaleLbs = 0, totalDimLbs = 0, totalScaleKg = 0, totalDimKg = 0;

            for (int pi = 0; pi < 25; pi++) {
                Row row = sheet.createRow(r++);
                row.setHeightInPoints(13.5f);
                Cell numCell = row.createCell(0);
                numCell.setCellValue(pi + 1);
                numCell.setCellStyle(rowNumStyle);

                if (pi < pieces.size()) {
                    ReceiptPiece p = pieces.get(pi);
                    double l = p.getLengthIn() != null ? p.getLengthIn().doubleValue() : 0.0;
                    double w = p.getWidthIn() != null ? p.getWidthIn().doubleValue() : 0.0;
                    double h = p.getHeightIn() != null ? p.getHeightIn().doubleValue() : 0.0;
                    double vol = l * w * h;
                    double dimKg = vol > 0 ? vol / 366.0 : 0.0;
                    double dimLbs = vol > 0 ? vol / 194.0 : 0.0;
                    double scaleLbs = p.getScaleWeightLbs() != null ? p.getScaleWeightLbs().doubleValue() : 0.0;
                    double scaleKg = p.getScaleWeightKg() != null ? p.getScaleWeightKg().doubleValue() : 0.0;

                    setCell(row, 1, 1, dataStyle);
                    setCell(row, 2, l, dataStyle);
                    setCell(row, 3, w, dataStyle);
                    setCell(row, 4, h, dataStyle);
                    setCell(row, 5, dimKg, dataStyle);
                    setCell(row, 6, scaleLbs, dataNoBorderStyle);
                    setCell(row, 7, dimLbs, dataNoBorderStyle);
                    setCell(row, 8, scaleKg, dataNoBorderStyle);
                    setCell(row, 9, dimKg, dataNoBorderStyle);
                    setCell(row, 10, Math.max(scaleKg, dimKg), dataNoBorderStyle);
                    setCell(row, 11, Math.max(scaleLbs, dimLbs), dataNoBorderStyle);

                    totalScaleLbs += scaleLbs;
                    totalDimLbs += dimLbs;
                    totalScaleKg += scaleKg;
                    totalDimKg += dimKg;
                } else {
                    setCell(row, 1, 0, dataStyle);
                    for (int c = 2; c <= 5; c++) setCell(row, c, 0, dataStyle);
                    for (int c = 6; c <= 11; c++) setCell(row, c, 0, dataNoBorderStyle);
                }
            }

            // Row 47: total row
            Row totalRow = sheet.createRow(r++);
            totalRow.setHeightInPoints(13.5f);
            totalRow.createCell(0).setCellStyle(rowNumStyle);
            setCell(totalRow, 1, pieces.size(), boldStyle);
            for (int c = 2; c <= 5; c++) totalRow.createCell(c).setCellStyle(boldStyle);
            setCell(totalRow, 6, totalDimKg, boldStyle);
            setCell(totalRow, 7, totalScaleLbs, boldStyle);
            setCell(totalRow, 8, totalDimLbs, boldStyle);
            setCell(totalRow, 9, totalScaleKg, boldStyle);
            setCell(totalRow, 10, Math.max(totalDimKg, totalScaleKg), boldStyle);
            setCell(totalRow, 11, Math.max(totalDimLbs, totalScaleLbs), boldStyle);

            // Row 48: blank
            blank(sheet, r++, 13.5f);

            // Row 49: Actual weight
            {
                Row rr = sheet.createRow(r++);
                rr.setHeightInPoints(16.5f);
                mergeLabel(sheet, rr, 1, 2, "Actual weight of this shipment is: ", weightLabelStyle);
                setCell(rr, 4, round0(totalScaleKg) + " KGS", weightNumStyle);
                setCell(rr, 5, round2(totalScaleKg), weightNumStyle);
                setCell(rr, 6, round0(totalScaleLbs) + " LBS", weightNumStyle);
                setCell(rr, 7, round2(totalScaleLbs), weightNumStyle);
            }

            // Row 50: blank
            blank(sheet, r++, 13.5f);

            // Row 51: Chargeable weight
            double finalChgKg = Math.max(totalDimKg, totalScaleKg);
            double finalChgLbs = Math.max(totalDimLbs, totalScaleLbs);
            {
                Row rr = sheet.createRow(r++);
                rr.setHeightInPoints(16.5f);
                mergeLabel(sheet, rr, 1, 2, "Chargeable weight of this shipment is: ", weightLabelStyle);
                setCell(rr, 4, round0(finalChgKg) + " KGS ", weightNumStyle);
                setCell(rr, 5, round2(finalChgKg), weightNumStyle);
                setCell(rr, 6, round0(finalChgLbs) + " LBS", weightNumStyle);
                setCell(rr, 7, round2(finalChgLbs), weightNumStyle);
            }

            // Row 52: blank
            blank(sheet, r++, 16.5f);

            // Rows 53-54: Remarks (2-row merged)
            String remarks = receipt.getRemarks() != null ? receipt.getRemarks() : "";
            Row rr = sheet.createRow(r++);
            rr.setHeightInPoints(13.5f);
            mergeLabel(sheet, rr, 2, 3, "Remarks:", remarksLabelStyle);
            Cell rv = rr.createCell(4);
            rv.setCellValue(remarks);
            rv.setCellStyle(remarksValStyle);
            for (int c = 5; c <= 8; c++) rr.createCell(c).setCellStyle(remarksValStyle);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r, 4, 8));
            Row rr2 = sheet.createRow(r++);
            rr2.setHeightInPoints(13.5f);
            rr2.createCell(2).setCellStyle(remarksLabelStyle);
            rr2.createCell(3).setCellStyle(remarksLabelStyle);
            for (int c = 4; c <= 8; c++) rr2.createCell(c).setCellStyle(remarksValStyle);

            // Row 55: blank
            blank(sheet, r++, 6.75f);

            // Row 56: Dock Signature
            {
                Row rA = sheet.createRow(r++);
                rA.setHeightInPoints(13.5f);
                mergeLabel(sheet, rA, 2, 3, "Dock Signature:", remarksLabelStyle);
                String dockSig = (receipt.getPrintName() != null && !receipt.getPrintName().isEmpty())
                        ? receipt.getPrintName() : (receipt.getDockSignature() != null ? "\u2713 Firmado" : "\u2014");
                setCell(rA, 4, dockSig, sigValueStyle);
                sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 6));
            }

            // Row 57: Print Name + Date/Time
            {
                Row rB = sheet.createRow(r++);
                rB.setHeightInPoints(13.5f);
                mergeLabel(sheet, rB, 2, 3, "Print Name:", remarksLabelStyle);
                setCell(rB, 4, safeStr(receipt.getPrintName(), "\u2014"), sigValueStyle);
                sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 4, 6));
                mergeLabel(sheet, rB, 7, 7, "Date / Time:", remarksLabelStyle);
                String dt = receipt.getReceiptDate() != null
                        ? receipt.getReceiptDate().toString().replace("T", " ").substring(0, 19) : "\u2014";
                setCell(rB, 8, dt, sigValueStyle);
                sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 8, 9));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private void setCell(Row row, int col, double val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    private void setCell(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    private void mergeLabel(Sheet sheet, Row row, int startCol, int endCol, String value, CellStyle style) {
        Cell cell = row.createCell(startCol);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        if (startCol != endCol) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol));
            for (int c = startCol + 1; c <= endCol; c++) {
                row.createCell(c).setCellStyle(style);
            }
        }
    }

    private void mergeValue(Sheet sheet, Row row, int startCol, int endCol, String value, CellStyle style) {
        Cell cell = row.createCell(startCol);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        if (startCol != endCol) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol));
            for (int c = startCol + 1; c <= endCol; c++) {
                row.createCell(c).setCellStyle(style);
            }
        }
    }

    private int headerRow(Sheet sheet, int startRow, float height, CellStyle labelStyle, CellStyle valueStyle,
                          WarehouseReceipt receipt, String awbNum) {
        boolean[] chkVals = {
            Boolean.TRUE.equals(receipt.getCashOnly()), Boolean.TRUE.equals(receipt.getBookedInAcoms()),
            Boolean.TRUE.equals(receipt.getDocsProvided()), Boolean.TRUE.equals(receipt.getCustomsCompleted()),
            Boolean.TRUE.equals(receipt.getPreBuilt())
        };
        String[] chkLabels = {
            "Cash Only:", "Booked in ACOMS:", "Documents Provided:",
            "Export Customs Completed:", "Pre-built / Shipper Built:"
        };
        String[][] fields = {
            {"Gateway/ CFS Name:", safeStr(receipt.getGatewayCfs(), "SDQ"), chkLabel(chkVals[0], chkLabels[0])},
            {"Shipper Name:", safeStr(receipt.getShipperName(), "\u2014"), chkLabel(chkVals[1], chkLabels[1])},
            {"MAWB Number:", awbNum, chkLabel(chkVals[2], chkLabels[2])},
            {"Origin:", safeStr(receipt.getOrigin(), "\u2014"), chkLabel(chkVals[3], chkLabels[3])},
            {"Destination:", safeStr(receipt.getDestination(), "\u2014"), chkLabel(chkVals[4], chkLabels[4])},
            {"AWB Reported Piece:", receipt.getAwbReportedPieces() != null ? String.valueOf(receipt.getAwbReportedPieces()) : "0", null},
        };
        int r = startRow;
        for (String[] f : fields) {
            Row row = sheet.createRow(r);
            row.setHeightInPoints(height);
            mergeLabel(sheet, row, 1, 2, f[0], labelStyle);
            mergeValue(sheet, row, 3, 4, f[1], valueStyle);
            if (f[2] != null) {
                mergeValue(sheet, row, 5, 6, f[2], labelStyle);
            }
            r++;
        }
        return r;
    }

    private String chkLabel(boolean val, String label) {
        return (val ? "\u2713 " : "\u2610 ") + label;
    }

    private void blank(Sheet sheet, int rowNum, float height) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(height);
    }

    private CellStyle style(Workbook wb, Font font, HorizontalAlignment align,
                            BorderStyle left, BorderStyle right, BorderStyle top, BorderStyle bottom,
                            IndexedColors fill) {
        CellStyle s = wb.createCellStyle();
        s.setFont(font);
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        if (left != BorderStyle.NONE) s.setBorderLeft(left);
        if (right != BorderStyle.NONE) s.setBorderRight(right);
        if (top != BorderStyle.NONE) s.setBorderTop(top);
        if (bottom != BorderStyle.NONE) s.setBorderBottom(bottom);
        if (fill != null) {
            s.setFillForegroundColor(fill.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        s.setWrapText(true);
        return s;
    }

    private CellStyle hdrStyle(Workbook wb, Font font, IndexedColors fill) {
        CellStyle s = wb.createCellStyle();
        s.setFont(font);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        s.setFillForegroundColor(fill.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private String safeStr(String s, String fallback) {
        return s != null && !s.isEmpty() ? s : fallback;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private long round0(double v) {
        return Math.round(v);
    }
}
