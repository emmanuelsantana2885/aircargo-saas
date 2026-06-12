package com.aircargo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ReceiptPieceDTO {
    private UUID id;
    private UUID receiptId;
    private Integer pieceNumber;


    //Dimensiones en pulgadas
    private BigDecimal lengthIn;
    private BigDecimal widthIn;
    private BigDecimal heigthIn;

    //Pesos
    private BigDecimal scaleWeightLbs;
    private BigDecimal scaleWeightKg;
    private BigDecimal dimWeightLbs;
    private BigDecimal dimWeightkg;
    private BigDecimal chargeableLbs;
    private BigDecimal chargeableKg;

}
