package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseReceiptDTO {
    private UUID id;
    private UUID airlineId;
    private UUID mawbId;
    private UUID createdByUserId;

    private String gatewayCfs;
    private String ShipperName;
    private String consigneeName;
    private String agentName;
    private String origin;
    private String destination;

    private Integer awbReportedPieces;
    private BigDecimal mawbWeightGreatest;
    private BigDecimal shipperReportedWeight;

    private OffsetDateTime startDateTime;
    private OffsetDateTime receiptDate;

    private Boolean cashOnly;
    private Boolean bookedInAcoms;
    private Boolean docsProvided;
    private Boolean customsCompleted;
    private Boolean preBuilt;
    private Boolean looseTender;

    private Integer pieceCount;

    private BigDecimal actualWeightLbs;
    private BigDecimal actualWeightkg;
    private BigDecimal chargeableWeightlbs;
    private BigDecimal chargeableWeightKg;

    private String shipperComment;
    private String observations;
    private String remarks;
    private String createdByName;

    private List<ReceiptPieceDTO> pieces;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;


}
