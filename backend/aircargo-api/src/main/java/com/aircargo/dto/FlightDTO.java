package com.aircargo.dto;

import com.aircargo.entity.AircraftType;
import com.aircargo.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {

    private UUID id;
    private UUID airlineId;
    private String flightNumber;
    private String origin;
    private String destination;
    private String aircraftReg;
    private AircraftType aircraftType;
    private LocalDate flightDate;
    private FlightStatus status;
    private Integer totalPositions;
    private OffsetDateTime createdAt;

}
