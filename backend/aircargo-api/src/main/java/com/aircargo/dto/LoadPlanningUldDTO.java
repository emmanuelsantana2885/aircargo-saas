package com.aircargo.dto;

import com.aircargo.entity.UldStatus;
import com.aircargo.entity.UldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Vista de un ULD para la pantalla de Load Planning,
 * incluye la lista de AWBs (uld_awb) cargados en ese ULD.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadPlanningUldDTO {

    private UUID id;
    private String uldNumber;
    private UldType uldType;
    private String position;
    private String config;
    private String sealNumber;

    private BigDecimal tareLbs;
    private BigDecimal grossWeightLbs;
    private BigDecimal netWeightLbs;

    private UldStatus status;

    private List<UldAwbDTO> awbs;
}
