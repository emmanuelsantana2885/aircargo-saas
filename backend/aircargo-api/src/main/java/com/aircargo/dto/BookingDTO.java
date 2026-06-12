package com.aircargo.dto;

import com.aircargo.entity.CommodityType;
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

}
