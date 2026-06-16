package com.aircargo.service;

import com.aircargo.entity.Uld;
import com.aircargo.repository.UldRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
public class LoadPlanningExportService {

    private final UldRepository uldRepository;

    public LoadPlanningExportService(UldRepository uldRepository) {
        this.uldRepository = uldRepository;
    }

    /**
     * Genera un manifiesto Excel operativo con los ULDs asignados a un vuelo.
     */
    public ByteArrayInputStream exportFlightLoadPlan(UUID flightId) throws Exception {
        List<Uld> ulds = uldRepository.findByFlightId(flightId);

        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("MANIFIESTO_ESTIBA");

            // --- ESTILOS DE LA HOJA ---
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            // CORRECCIÓN: Usamos un color estándar y válido de Apache POI
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex()); 
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);

            // --- ENCABEZADOS OPERATIVOS ---
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ULD NUMBER", "TYPE", "POSITION", "CONFIG", "SEAL #", "TARE (LBS)", "GROSS WT (LBS)", "STATUS"};

            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // --- LLENADO DE DATOS DESDE POSTGRESQL ---
            int rowIndex = 1;
            for (Uld uld : ulds) {
                Row row = sheet.createRow(rowIndex++);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(uld.getUldNumber());
                cell0.setCellStyle(dataCellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(uld.getUldType() != null ? uld.getUldType().toString() : "");
                cell1.setCellStyle(dataCellStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(uld.getPosition() != null ? uld.getPosition() : "W/O");
                cell2.setCellStyle(dataCellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(uld.getConfig() != null ? uld.getConfig() : "");
                cell3.setCellStyle(dataCellStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(uld.getSealNumber() != null ? uld.getSealNumber() : "-");
                cell4.setCellStyle(dataCellStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(uld.getTareLbs() != null ? uld.getTareLbs().doubleValue() : 0.0);
                cell5.setCellStyle(dataCellStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(uld.getGrossWeightLbs() != null ? uld.getGrossWeightLbs().doubleValue() : 0.0);
                cell6.setCellStyle(dataCellStyle);

                Cell cell7 = row.createCell(7);
                cell7.setCellValue(uld.getStatus() != null ? uld.getStatus().toString() : "OPEN");
                cell7.setCellStyle(dataCellStyle);
            }

            // Autoajustar el tamaño de las columnas
            for (int col = 0; col < columns.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
