package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadPlanningBatchImportResultDTO {
    private int totalSheets;
    private int successSheets;
    private int failedSheets;
    private int totalUldsCreated;
    private int totalUldsUpdated;
    private int totalMawbsCreated;
    private int totalBookingsCreated;
    private int totalUldAwbsCreated;
    private List<LoadPlanningSheetImportResultDTO> sheetResults;
}
