package com.aircargo.service;

import com.aircargo.entity.Flight;
import com.aircargo.entity.Uld;
import com.aircargo.entity.FlightStatus;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.UldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FlightManifestService {

    private final FlightRepository flightRepository;
    private final UldRepository uldRepository;

    public FlightManifestService(FlightRepository flightRepository, UldRepository uldRepository) {
        this.flightRepository = flightRepository;
        this.uldRepository = uldRepository;
    }

    /**
     * Cierra operativamente la carga del vuelo pasándolo a estado BOARDING
     * y congelando los pesos definitivos de la rampa.
     */
    @Transactional
    public Map<String, Object> finalizeFlight(UUID flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("EL VUELO ESPECIFICADO NO EXISTE."));

        // Regla de Negocio Acoplada: Bloquear si el avión ya se fue, llegó o se canceló
        if (flight.getStatus() == FlightStatus.DEPARTED || 
            flight.getStatus() == FlightStatus.ARRIVED || 
            flight.getStatus() == FlightStatus.CANCELLED) {
            throw new IllegalStateException("INMUTABILIDAD DE CARGA: El vuelo ya se encuentra en estado " + flight.getStatus() + ".");
        }

        List<Uld> assignedUlds = uldRepository.findByFlightId(flightId);

        // Calcular la masa total real estibada (Libras de Rampa)
        double totalGrossWeightLbs = assignedUlds.stream()
                .mapToDouble(u -> u.getGrossWeightLbs() != null ? u.getGrossWeightLbs().doubleValue() : 0.0)
                .sum();

        // Transición de estado: El vuelo entra en proceso de estiba/embarque físico de contenedores
        flight.setStatus(FlightStatus.BOARDING);
        flightRepository.save(flight);

        return Map.of(
            "flightNumber", flight.getFlightNumber(),
            "aircraftReg", flight.getAircraftReg(),
            "status", flight.getStatus().name(),
            "totalUldsManifested", assignedUlds.size(),
            "finalGrossWeightLbs", BigDecimal.valueOf(totalGrossWeightLbs).setScale(2, java.math.RoundingMode.HALF_UP),
            "message", "FLIGHT CARGO MANIFEST CLOSED SUCCESSFULLY - STATUS SET TO BOARDING"
        );
    }
}
