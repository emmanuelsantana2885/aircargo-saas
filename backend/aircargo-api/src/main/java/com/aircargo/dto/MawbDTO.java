package com.aircargo.dto;

import com.aircargo.entity.CommodityType;
import com.aircargo.entity.MawbStatus;
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
public class MawbDTO {

    private UUID id;
    private UUID airlineId;
    private UUID flightId;
    private String awbNumber;
    private String shipperName;
    private String consigneeName;
    private String origin;
    private String destination;
    private Integer pieces;
    private BigDecimal reportedWeightKg;
    private BigDecimal chargeableWeightKg;
    private CommodityType commodityType;
    private MawbStatus status;
    private Boolean cashOnly;
    private OffsetDateTime createdAt;


}
