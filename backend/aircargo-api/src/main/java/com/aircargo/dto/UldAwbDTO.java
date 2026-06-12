package com.aircargo.dto;


import com.aircargo.entity.CommodityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UldAwbDTO {

    private UUID id;
    private UUID uldId;
    private UUID mawbId;

    private String mawbLabel;
    private CommodityType description;
    private String destination;
    private Integer pieces;
    private Integer piecesPct;

    private BigDecimal tempInbound;
    private BigDecimal tempOutbound;
    private Boolean hc;
    private String comments;

    private BigDecimal consumptionPallets;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer avgTimePerPieceSec;

    //Campos calculados
    private BigDecimal lapseMinutes;
    private BigDecimal pcsPerMin;
    private BigDecimal operativeWorkedHours;
    private BigDecimal earnedHours;

    private OffsetDateTime createdAt;
}
