package com.aircargo.dto;

import com.aircargo.entity.Booking;
import com.aircargo.entity.CommodityType;
import com.aircargo.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Mawb;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private UUID id;
    private UUID airlineId;
    private UUID flightId;
    private UUID mawbId;
    private String clientName;
    private String contactName;
    private String cnee;
    private String shipperName;
    private String awbNumber;

    private Integer skids;
    private Integer units;

    private BigDecimal reservedKg;
    private BigDecimal confirmedKg;
    private BigDecimal receivedKg;
    private BigDecimal fulfillmentPct;

    private String destination;
    private Integer priority;
    private CommodityType commodityType;

    private String dayReceived;
    private BigDecimal timeHours;
    private BigDecimal positions;
    private BigDecimal realPositions;

    private BigDecimal lastWeekKg;
    private BigDecimal lastWeekPositions;

    private Boolean isConfirmed;
    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static BookingDTO fromEntity(Booking entity){
        if(entity == null) return null;
        return BookingDTO.builder()
                .id(entity.getId())
                .airlineId(entity.getAirline() != null ? entity.getAirline().getId() : null)
                .flightId(entity.getFlight() != null ? entity.getFlight().getId() : null)
                .mawbId(entity.getMawb() != null ? entity.getMawb().getId() : null)
                .clientName(entity.getClientName())
                .contactName(entity.getContactName())
                .cnee(entity.getCnee())
                .shipperName(entity.getShipperName())
                .awbNumber(entity.getAwbNumber())
                .skids(entity.getSkids())
                .units(entity.getUnits())
                .reservedKg(entity.getReservedKg())
                .confirmedKg(entity.getConfirmedKg())
                .receivedKg(entity.getReceivedKg())
                .fulfillmentPct(entity.getFulfillmentPct())
                .destination(entity.getDestination())
                .priority(entity.getPriority())
                .commodityType(entity.getCommodityType())
                .dayReceived(entity.getDayReceived())
                .timeHours(entity.getTimeHours())
                .positions(entity.getPositions())
                .realPositions(entity.getRealPositions())
                .lastWeekKg(entity.getLastWeekKg())
                .lastWeekPositions(entity.getLastWeekPositions())
                .isConfirmed(entity.getIsConfirmed())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Booking toEntity(BookingDTO dto){
        if(dto == null) return null;
        Booking entity = new Booking();
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
        if (dto.getMawbId() != null) {
            Mawb mawb = new Mawb();
            mawb.setId(dto.getMawbId());
            entity.setMawb(mawb);
        }
        entity.setClientName(dto.getClientName());
        entity.setContactName(dto.getContactName());
        entity.setCnee(dto.getCnee());
        entity.setShipperName(dto.getShipperName());
        entity.setAwbNumber(dto.getAwbNumber());
        entity.setSkids(dto.getSkids());
        entity.setUnits(dto.getUnits());
        entity.setReservedKg(dto.getReservedKg() != null ? dto.getReservedKg() : BigDecimal.ZERO);
        entity.setConfirmedKg(dto.getConfirmedKg());
        entity.setReceivedKg(dto.getReceivedKg());
        java.math.BigDecimal fpct = dto.getFulfillmentPct();
        if (fpct != null && fpct.compareTo(new java.math.BigDecimal("9999.9999")) > 0) fpct = new java.math.BigDecimal("9999.9999");
        entity.setFulfillmentPct(fpct);
        entity.setDestination(dto.getDestination());
        entity.setPriority(dto.getPriority());
        entity.setCommodityType(dto.getCommodityType());
        entity.setDayReceived(dto.getDayReceived());
        entity.setTimeHours(dto.getTimeHours());
        entity.setPositions(dto.getPositions());
        entity.setRealPositions(dto.getRealPositions());
        entity.setLastWeekKg(dto.getLastWeekKg());
        entity.setLastWeekPositions(dto.getLastWeekPositions());
        entity.setIsConfirmed(dto.getIsConfirmed());
        entity.setNotes(dto.getNotes());
        return entity;
    }
}
