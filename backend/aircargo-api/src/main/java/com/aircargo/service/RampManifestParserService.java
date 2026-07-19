package com.aircargo.service;

import com.aircargo.entity.Uld;
import com.aircargo.entity.Flight;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.UldType;
import com.aircargo.entity.UldStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RampManifestParserService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Procesa el archivo Excel de rampa, descombina celdas y arma entidades Uld
     * respetando al 100% los tipos strict de la base de datos (Libras, Enums, UUIDs).
     */
    public List<Uld> parseExcelToNativeUld(MultipartFile file, UUID flightId, UUID airlineId) throws Exception {
        Map<String, Uld> uldMap = new LinkedHashMap<>();
        
        // Obtener proxies ligeros de Hibernate para no hacer SELECT previos inútiles
        Flight flightProxy = entityManager.getReference(Flight.class, flightId);
        Airline airlineProxy = entityManager.getReference(Airline.class, airlineId);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String uldNumberStr = getCellValueResolved(sheet, row.getCell(0));
                String mawb = getCellValueResolved(sheet, row.getCell(9));

                if ((uldNumberStr == null || uldNumberStr.isEmpty()) && (mawb == null || mawb.isEmpty())) {
                    continue;
                }

                if (uldNumberStr != null && !uldNumberStr.isEmpty()) {
                    uldNumberStr = uldNumberStr.trim();
                    if (!uldMap.containsKey(uldNumberStr)) {
                        String weightStr = getCellValueResolved(sheet, row.getCell(3)); 
                        String taraStr = getCellValueResolved(sheet, row.getCell(4));   
                        String configStr = getCellValueResolved(sheet, row.getCell(5));    
                        String posStr = getCellValueResolved(sheet, row.getCell(7));       

                        double grossAmount = (weightStr != null && !weightStr.isEmpty()) ? Double.parseDouble(weightStr) : 0.0;
                        double tareAmount = (taraStr != null && !taraStr.isEmpty()) ? Double.parseDouble(taraStr) : 0.0;

                        Uld uld = new Uld();
                        uld.setUldNumber(uldNumberStr);
                        uld.setFlight(flightProxy);
                        uld.setAirline(airlineProxy);
                        uld.setPosition(posStr != null ? posStr.trim() : "");
                        uld.setConfig(configStr != null ? configStr.trim() : "AAZ");
                        
                        // Mapeo seguro a los campos de inserción en Libras (Lbs)
                        uld.setGrossWeightLbs(BigDecimal.valueOf(grossAmount));
                        uld.setTareLbs(BigDecimal.valueOf(tareAmount));
                        
                        // Resolver de manera robusta el Enum de tipo de ULD
                        try {
                            String typeClean = configStr != null ? configStr.trim().toUpperCase() : "PMC";
                            uld.setUldType(UldType.valueOf(typeClean));
                        } catch (Exception e) {
                            uld.setUldType(UldType.PMC); // Fallback por defecto si no coincide el string
                        }
                        
                        uld.setStatus(UldStatus.OPEN);

                        uldMap.put(uldNumberStr, uld);
                    }
                }
            }
        }

        return new ArrayList<>(uldMap.values());
    }

    private String getCellValueResolved(Sheet sheet, Cell cell) {
        if (cell == null) return "";
        
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                Row masterRow = sheet.getRow(region.getFirstRow());
                Cell masterCell = masterRow.getCell(region.getFirstColumn());
                return getFormatterCellValue(masterCell);
            }
        }
        return getFormatterCellValue(cell);
    }

    private String getFormatterCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
