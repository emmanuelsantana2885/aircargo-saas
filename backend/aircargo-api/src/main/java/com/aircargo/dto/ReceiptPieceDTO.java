package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.entity.WarehouseReceipt;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptPieceDTO {
    private UUID id;
    private UUID receiptId;
    private UUID hawbId;
    private Integer pieceNumber;
    private Integer pieces;

    // Dimensiones en pulgadas
    private BigDecimal lengthIn;
    private BigDecimal widthIn;
    private BigDecimal heightIn;

    // Pesos
    private BigDecimal scaleWeightLbs;
    private BigDecimal scaleWeightKg;
    private BigDecimal dimWeightLbs;
    private BigDecimal dimWeightKg;
    private BigDecimal chargeableLbs;
    private BigDecimal chargeableKg;

    public static ReceiptPieceDTO fromEntity(ReceiptPiece entity) {
        if (entity == null) return null;
        return ReceiptPieceDTO.builder()
                .id(entity.getId())
                .receiptId(entity.getReceipt() != null ? entity.getReceipt().getId() : null)
                .hawbId(entity.getHawbId())
                .pieceNumber(entity.getPieceNumber())
                .pieces(entity.getPieces())
                .lengthIn(entity.getLengthIn())
                .widthIn(entity.getWidthIn())
                .heightIn(entity.getHeightIn())
                .scaleWeightLbs(entity.getScaleWeightLbs())
                .scaleWeightKg(entity.getScaleWeightKg())
                .dimWeightLbs(entity.getDimWeightLbs())
                .dimWeightKg(entity.getDimWeightKg())
                .chargeableLbs(entity.getChargeableLbs())
                .chargeableKg(entity.getChargeableKg())
                .build();
    }

    public static ReceiptPiece toEntity(ReceiptPieceDTO dto) {
        if (dto == null) return null;
        ReceiptPiece entity = new ReceiptPiece();
        entity.setId(dto.getId());
        if (dto.getReceiptId() != null) {
            WarehouseReceipt r = new WarehouseReceipt();
            r.setId(dto.getReceiptId());
            entity.setReceipt(r);
        }
        entity.setHawbId(dto.getHawbId());
        entity.setPieceNumber(dto.getPieceNumber());
        entity.setPieces(dto.getPieces());
        entity.setLengthIn(dto.getLengthIn());
        entity.setWidthIn(dto.getWidthIn());
        entity.setHeightIn(dto.getHeightIn());
        entity.setScaleWeightLbs(dto.getScaleWeightLbs());
        entity.setScaleWeightKg(dto.getScaleWeightKg());
        entity.setDimWeightLbs(dto.getDimWeightLbs());
        entity.setDimWeightKg(dto.getDimWeightKg());
        entity.setChargeableLbs(dto.getChargeableLbs());
        entity.setChargeableKg(dto.getChargeableKg());
        return entity;
    }
}

