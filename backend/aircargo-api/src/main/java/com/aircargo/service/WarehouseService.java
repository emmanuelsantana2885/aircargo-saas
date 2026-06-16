package com.aircargo.service;

import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class WarehouseService {

    private final WarehouseReceiptRepository receiptRepository;
    private final ReceiptPieceRepository pieceRepository;

    public WarehouseService(WarehouseReceiptRepository receiptRepository, ReceiptPieceRepository pieceRepository) {
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
    }

    /**
     * Registra un recibo de bodega y calcula dinámicamente las métricas de peso volumétrico IATA
     * para cada pieza individual mapeando exactamente sobre los nombres reales de la entidad.
     */
    @Transactional
    public WarehouseReceipt processWarehouseReceipt(WarehouseReceipt receipt, List<ReceiptPiece> pieces) {
        // 1. Guardar el encabezado
        WarehouseReceipt savedReceipt = receiptRepository.save(receipt);

        // Factor IATA estándar (Intl: 366.0)
        double dimFactor = (receipt.getDimFactorIntl() != null) ? receipt.getDimFactorIntl().doubleValue() : 366.0;
        BigDecimal lbsToKgFactor = BigDecimal.valueOf(0.45359237);
        BigDecimal kgToLbsFactor = BigDecimal.valueOf(2.20462262);

        for (ReceiptPiece piece : pieces) {
            piece.setReceipt(savedReceipt); // Ajustado a tu @ManyToOne original

            double length = piece.getLengthIn() != null ? piece.getLengthIn().doubleValue() : 0.0;
            double width = piece.getWidthIn() != null ? piece.getWidthIn().doubleValue() : 0.0;
            double height = piece.getHeightIn() != null ? piece.getHeightIn().doubleValue() : 0.0;

            // 2. Calcular Pesos Volumétricos (Lbs y Kg)
            double volWeightLbs = (length * width * height) / dimFactor;
            piece.setDimWeightLbs(BigDecimal.valueOf(volWeightLbs).setScale(3, RoundingMode.HALF_UP));
            
            BigDecimal dimWeightKg = piece.getDimWeightLbs().multiply(lbsToKgFactor);
            piece.setDimWeightKg(dimWeightKg.setScale(3, RoundingMode.HALF_UP));

            // 3. Normalizar pesos de Báscula si es necesario
            if (piece.getScaleWeightLbs() != null && (piece.getScaleWeightKg() == null || piece.getScaleWeightKg().compareTo(BigDecimal.ZERO) == 0)) {
                piece.setScaleWeightKg(piece.getScaleWeightLbs().multiply(lbsToKgFactor).setScale(3, RoundingMode.HALF_UP));
            }

            // 4. Regla de Negocio IATA: El peso cobrable es el Mayor entre báscula y cubicaje
            BigDecimal scaleKg = piece.getScaleWeightKg() != null ? piece.getScaleWeightKg() : BigDecimal.ZERO;
            BigDecimal maxKg = scaleKg.max(piece.getDimWeightKg());
            piece.setChargeableKg(maxKg.setScale(3, RoundingMode.HALF_UP));

            BigDecimal maxLbs = piece.getScaleWeightLbs().max(piece.getDimWeightLbs());
            piece.setChargeableLbs(maxLbs.setScale(3, RoundingMode.HALF_UP));

            // Persistencia
            pieceRepository.save(piece);
        }

        return savedReceipt;
    }
}
