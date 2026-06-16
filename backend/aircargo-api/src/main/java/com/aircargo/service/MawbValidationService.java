package com.aircargo.service;

import com.aircargo.entity.Mawb;
import com.aircargo.entity.Flight;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class MawbValidationService {

    private final MawbRepository mawbRepository;
    private final FlightRepository flightRepository;

    // Regla IATA: Prefijo de 3 dígitos (ej: 406), guion y 8 dígitos correlativos
    private static final Pattern AWB_PATTERN = Pattern.compile("^\\d{3}-\\d{8}$");

    public MawbValidationService(MawbRepository mawbRepository, FlightRepository flightRepository) {
        this.mawbRepository = mawbRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Valida el formato IATA estándar de un Air Waybill.
     */
    public void validateAwbFormat(String awbNumber) {
        if (awbNumber == null || !AWB_PATTERN.matcher(awbNumber).matches()) {
            throw new IllegalArgumentException("FORMATO DE GUÍA INVÁLIDO. DEBE CUMPLIR CON EL ESTÁNDAR IATA (EJ: 406-12345678).");
        }
    }

    /**
     * Verifica que el peso acumulado de las guías no sature el Payload Máximo del avión.
     */
    public void validateFlightPayloadLimit(UUID flightId, BigDecimal incomingWeightKg) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("EL VUELO ESPECIFICADO NO EXISTE EN EL SISTEMA."));

        List<Mawb> currentMawbs = mawbRepository.findByFlightId(flightId);

        // Sumar los pesos de las guías ya asignadas al vuelo
        double currentTotalWeight = currentMawbs.stream()
                .mapToDouble(m -> m.getChargeableWeightKg() != null ? m.getChargeableWeightKg().doubleValue() : 0.0)
                .sum();

        double limit = flight.getMaxPayloadKg() != null ? flight.getMaxPayloadKg().doubleValue() : 0.0;

        if ((currentTotalWeight + incomingWeightKg.doubleValue()) > limit) {
            throw new IllegalStateException(
                String.format("ALERTA DE SOBREPESO: El peso total propuesto (%.2f kg) excede la capacidad máxima del avión (%s: %.2f kg).", 
                    (currentTotalWeight + incomingWeightKg.doubleValue()), flight.getAircraftReg(), limit)
            );
        }
    }
}
