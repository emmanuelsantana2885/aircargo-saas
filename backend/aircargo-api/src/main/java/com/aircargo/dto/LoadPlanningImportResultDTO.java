package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadPlanningImportResultDTO {
    private UUID flightId;
    private String flightNumber;

    private int uldsCreated;
    private int uldsUpdated;
    private int mawbsCreated;
    private int bookingsCreated;
    private int uldAwbsCreated;

    private List<String> warnings;
}
