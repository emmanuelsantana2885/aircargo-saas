package com.aircargo.dto;

import com.aircargo.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Uld;
import com.aircargo.entity.UldStatus;
import com.aircargo.entity.UldType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UldDTO {

    private UUID id;
    private UUID airlineId;
    private UUID flightId;

    private String uldNumber;
    private UldType uldType;
    private String position;
    private String config;
    private String sealNumber;

    private BigDecimal tareLbs;
    private BigDecimal grossWeightLbs;
    private BigDecimal netWeightLbs;

    private BigDecimal tareKg;
    private BigDecimal grossWeightKg;
    private BigDecimal netWeightKg;

    private UldStatus status;
    private OffsetDateTime builtAt;
    private OffsetDateTime loadedAt;
    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private List<UldAwbDTO> awbs;

    public static UldDTO fromEntity(Uld entity) {
        if (entity == null) return null;
        return UldDTO.builder()
            .id(entity.getId())
            .airlineId(
                entity.getAirline() != null ? entity.getAirline().getId() : null
            )
            .flightId(
                entity.getFlight() != null ? entity.getFlight().getId() : null
            )
            .uldNumber(entity.getUldNumber())
            .uldType(entity.getUldType())
            .position(entity.getPosition())
            .config(entity.getConfig())
            .sealNumber(entity.getSealNumber())
            .tareLbs(entity.getTareLbs())
            .grossWeightLbs(entity.getGrossWeightLbs())
            .netWeightLbs(entity.getNetWeightLbs())
            .tareKg(entity.getTareKg())
            .grossWeightKg(entity.getGrossWeightKg())
            .netWeightKg(entity.getNetWeightKg())
            .status(entity.getStatus())
            .builtAt(entity.getBuiltAt())
            .loadedAt(entity.getLoadedAt())
            .notes(entity.getNotes())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    public static Uld toEntity(UldDTO dto) {
        if (dto == null) return null;
        Uld entity = new Uld();
        entity.setId(dto.getId());
        if (dto.getAirlineId() != null) {
            Airline airline = new Airline();
            airline.setId(dto.getAirlineId());
            entity.setAirline(airline);
        }
        if (dto.getFlightId() != null) {
            Flight flight = new Flight();
            flight.setId(dto.getFlightId());
            entity.setFlight(flight);
        }
        entity.setUldNumber(dto.getUldNumber());
        entity.setUldType(dto.getUldType());
        entity.setPosition(dto.getPosition());
        entity.setConfig(dto.getConfig());
        entity.setSealNumber(dto.getSealNumber());
        entity.setTareLbs(dto.getTareLbs());
        entity.setGrossWeightLbs(dto.getGrossWeightLbs());
        entity.setNetWeightLbs(dto.getNetWeightLbs());
        entity.setTareKg(dto.getTareKg());
        entity.setGrossWeightKg(dto.getGrossWeightKg());
        entity.setNetWeightKg(dto.getNetWeightKg());
        entity.setStatus(dto.getStatus());
        entity.setBuiltAt(dto.getBuiltAt());
        entity.setLoadedAt(dto.getLoadedAt());
        entity.setNotes(dto.getNotes());
        return entity;
    }
}
