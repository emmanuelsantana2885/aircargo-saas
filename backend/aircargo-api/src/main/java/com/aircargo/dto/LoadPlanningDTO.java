package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Vista consolidada del Load Planning de un vuelo:
 * datos del vuelo + lista de ULDs con sus AWBs anidados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadPlanningDTO {

    private UUID flightId;
    private String flightNumber;
    private String origin;
    private String destination;
    private String aircraftReg;
    private LocalDate flightDate;
    private Integer totalPositions;
    private BigDecimal maxPayloadKg;

    private List<LoadPlanningUldDTO> ulds;
}
