package com.aircargo.flightservice.dto;

import com.aircargo.common.entity.AircraftType;
import com.aircargo.common.entity.Airline;
import com.aircargo.flightservice.entity.Flight;
import com.aircargo.flightservice.entity.FlightStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private BigDecimal maxPayloadKg;
    private Integer totalPositions;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static FlightDTO fromEntity(Flight entity) {
        if (entity == null) return null;
        return FlightDTO.builder()
            .id(entity.getId())
            .airlineId(
                entity.getAirline() != null ? entity.getAirline().getId() : null
            )
            .flightNumber(entity.getFlightNumber())
            .origin(entity.getOrigin())
            .destination(entity.getDestination())
            .aircraftReg(entity.getAircraftReg())
            .aircraftType(entity.getAircraftType())
            .flightDate(entity.getFlightDate())
            .status(entity.getStatus())
            .maxPayloadKg(entity.getMaxPayloadKg())
            .totalPositions(entity.getTotalPositions())
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public static Flight toEntity(FlightDTO dto) {
        if (dto == null) return null;
        Flight entity = new Flight();
        entity.setId(dto.getId());
        if (dto.getAirlineId() != null) {
            Airline airline = new Airline();
            airline.setId(dto.getAirlineId());
            entity.setAirline(airline);
        }
        entity.setFlightNumber(dto.getFlightNumber());
        entity.setOrigin(dto.getOrigin());
        entity.setDestination(dto.getDestination());
        entity.setAircraftReg(dto.getAircraftReg());
        entity.setAircraftType(dto.getAircraftType());
        entity.setFlightDate(dto.getFlightDate());
        entity.setStatus(dto.getStatus());
        entity.setMaxPayloadKg(dto.getMaxPayloadKg());
        entity.setTotalPositions(dto.getTotalPositions());
        entity.setNotes(dto.getNotes());
        return entity;
    }
}
