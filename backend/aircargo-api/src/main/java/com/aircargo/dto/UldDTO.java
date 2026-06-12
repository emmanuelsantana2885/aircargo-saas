package com.aircargo.dto;

import com.aircargo.entity.UldStatus;
import com.aircargo.entity.UldType;
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
public class UldDTO {

    private UUID id;
    private UUID airlineId;
    private UUID flightId;

    private String uldNumber;
    private UldType uldtype;
    private String position;
    private String config;
    private String sealNumber;

    private BigDecimal tareLbs;
    private BigDecimal grossWeigthLbs;
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

}
