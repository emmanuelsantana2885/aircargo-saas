package com.aircargo.service;

import com.aircargo.entity.Hawb;
import com.aircargo.entity.Mawb;
import com.aircargo.repository.HawbRepository;
import com.aircargo.repository.MawbRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HawbValidationService {

    private final HawbRepository hawbRepository;
    private final MawbRepository mawbRepository;

    public HawbValidationService(HawbRepository hawbRepository, MawbRepository mawbRepository) {
        this.hawbRepository = hawbRepository;
        this.mawbRepository = mawbRepository;
    }

    /**
     * Verifica que la nueva House AWB no rompa los techos de piezas o peso de la Master AWB.
     */
    public void validateHawbConsolidation(UUID mawbId, int incomingPieces, double incomingWeightKg) {
        Mawb master = mawbRepository.findById(mawbId)
                .orElseThrow(() -> new IllegalArgumentException("LA GUÍA MAESTRA (MAWB) ESPECIFICADA NO EXISTE."));

        List<Hawb> existingHawbs = hawbRepository.findByMawbId(mawbId);

        int currentPieces = existingHawbs.stream().mapToInt(Hawb::getPieces).sum();
        double currentWeight = existingHawbs.stream().mapToDouble(h -> h.getWeightKg() != null ? h.getWeightKg().doubleValue() : 0.0).sum();

        int maxPieces = master.getPieces() != null ? master.getPieces() : 0;
        double maxWeight = master.getChargeableWeightKg() != null ? master.getChargeableWeightKg().doubleValue() : 0.0;

        // Auditoría de Piezas
        if ((currentPieces + incomingPieces) > maxPieces) {
            throw new IllegalStateException(String.format(
                "DISCREPANCIA EN PIEZAS: El consolidado superaría el límite de la MAWB (%d de un máximo de %d).",
                (currentPieces + incomingPieces), maxPieces
            ));
        }

        // Auditoría de Masa
        if ((currentWeight + incomingWeightKg) > maxWeight) {
            throw new IllegalStateException(String.format(
                "DISCREPANCIA EN PESO: El peso acumulado de las House AWBs (%.2f kg) excede el total de la Master (%.2f kg).",
                (currentWeight + incomingWeightKg), maxWeight
            ));
        }
    }
}
