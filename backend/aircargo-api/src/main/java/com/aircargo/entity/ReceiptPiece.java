package com.aircargo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "receipt_piece")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "pieces"})
    private WarehouseReceipt receipt;

    @Column(name = "piece_number", nullable = false)
    private Integer pieceNumber;

    // Dimensiones en pulgadas
    @Column(name = "length_in", precision = 8, scale = 2)
    private BigDecimal lengthIn = BigDecimal.ZERO;

    @Column(name = "width_in", precision = 8, scale = 2)
    private BigDecimal widthIn = BigDecimal.ZERO;

    @Column(name = "height_in", precision = 8, scale = 2)
    private BigDecimal heightIn = BigDecimal.ZERO;

    // Pesos
    @Column(name = "scale_weight_lbs", precision = 10, scale = 3)
    private BigDecimal scaleWeightLbs = BigDecimal.ZERO;

    @Column(name = "scale_weight_kg", precision = 10, scale = 3)   // ← Corregido
    private BigDecimal scaleWeightKg = BigDecimal.ZERO;

    // Dim weight
    @Column(name = "dim_weight_lbs", precision = 10, scale = 3)
    private BigDecimal dimWeightLbs = BigDecimal.ZERO;

    @Column(name = "dim_weight_kg", precision = 10, scale = 3)
    private BigDecimal dimWeightKg = BigDecimal.ZERO;

    // Chargeable weight
    @Column(name = "chargeable_lbs", precision = 10, scale = 3)
    private BigDecimal chargeableLbs = BigDecimal.ZERO;

    @Column(name = "chargeable_kg", precision = 10, scale = 3)
    private BigDecimal chargeableKg = BigDecimal.ZERO;
}