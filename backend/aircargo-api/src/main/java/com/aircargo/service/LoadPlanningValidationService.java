package com.aircargo.service;

import com.aircargo.dto.FlightLoadPlanDTO;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.HashMap;

@Service
public class LoadPlanningValidationService {

    private static final Map<String, Double> POSITION_WEIGHT_LIMITS = new HashMap<>();

    static {
        // Fuselaje Ancho (Eje Central Main Deck) - Capacidad Estructural Máxima
        POSITION_WEIGHT_LIMITS.put("P1", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P2", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P3", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P4", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P5", 5200.0);
        POSITION_WEIGHT_LIMITS.put("P6", 5200.0);
        POSITION_WEIGHT_LIMITS.put("P7", 5000.0);

        // Posiciones de balance lateral simétrico restringido (Left / Right)
        POSITION_WEIGHT_LIMITS.put("10L", 3200.0);
        POSITION_WEIGHT_LIMITS.put("10R", 3200.0);
        POSITION_WEIGHT_LIMITS.put("11L", 3200.0);
        POSITION_WEIGHT_LIMITS.put("11R", 3200.0);
        POSITION_WEIGHT_LIMITS.put("12L", 2800.0);
        POSITION_WEIGHT_LIMITS.put("12R", 2800.0);
        POSITION_WEIGHT_LIMITS.put("13",  3500.0);
    }

    public void validateStructuralLimits(FlightLoadPlanDTO uldData) {
        if (uldData.getPos() == null || uldData.getPos().trim().isEmpty()) {
            return; // Permite guardar de manera temporal sin posición asignada ("W/O")
        }

        String positionKey = uldData.getPos().toUpperCase().trim();
        Double maxAllowedPayload = POSITION_WEIGHT_LIMITS.get(positionKey);

        if (maxAllowedPayload == null) {
            throw new IllegalArgumentException("La posición ingresada [" + positionKey + "] no existe en la configuración de la aeronave.");
        }

        double grossWeight = uldData.getWeight() != null ? uldData.getWeight() : 0.0;
        double tareWeight = uldData.getTara() != null ? uldData.getTara() : 0.0;
        
        // Cálculo del peso neto real
        double netPayload = grossWeight - tareWeight;

        if (netPayload <= 0) {
            throw new IllegalArgumentException("Error de Báscula: El Gross Weight debe ser mayor a la Tara del ULD.");
        }

        if (netPayload > maxAllowedPayload) {
            double excess = netPayload - maxAllowedPayload;
            throw new StructuralOverloadException(
                String.format("ALERTA DE TOLERANCIA CRÍTICA: La posición %s excede el límite por %.2f lbs. " +
                              "[Límite Max: %.0f lbs | Peso Neto Recibido: %.0f lbs]", 
                              positionKey, excess, maxAllowedPayload, netPayload)
            );
        }
    }

    // Excepción embebida con anotación de estado Spring para evitar dependencias cruzadas
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class StructuralOverloadException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public StructuralOverloadException(String message) {
            super(message);
        }
    }
}
