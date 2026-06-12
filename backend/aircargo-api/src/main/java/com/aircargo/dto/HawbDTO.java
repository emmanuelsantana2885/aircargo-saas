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
public class HawbDTO {
    private UUID id;
    private UUID mawbId;
    private UUID airlineId;
    private String hawbNumber;
    private String consigneeName;
    private String destination;
    private Integer pieces;
    private BigDecimal weigthKg;
    private CommodityType commodityType;
    private MawbStatus status;
    private String notes;
    private OffsetDateTime createdAt;
}
