package com.aircargo.service;

import com.aircargo.dto.FlightDTO;
import com.aircargo.entity.Flight;
import com.aircargo.entity.FlightStatus;
import com.aircargo.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlightServiceImpl implements FlightService{

    private final FlightRepository flightRepository;

    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public List<FlightDTO> getAll(UUID airlineId, LocalDate flightDate, FlightStatus status, String flightNumber) {
        List<Flight> results;
        if (flightNumber != null && airlineId != null) {
            results = flightRepository.findByAirlineIdAndFlightNumber(airlineId, flightNumber);
        } else if (flightDate != null && airlineId != null) {
            results = flightRepository.findByAirlineIdAndFlightDate(airlineId, flightDate);
        } else if (status != null && airlineId != null) {
            results = flightRepository.findByAirlineIdAndStatus(airlineId, status);
        } else if (airlineId != null) {
            results = flightRepository.findByAirlineId(airlineId);
        } else {
            results = flightRepository.findAll();
        }
        return results.stream()
                .map(FlightDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FlightDTO> getById(UUID id) {
        return flightRepository.findById(id).map(FlightDTO::fromEntity);
    }

    @Override
    public Optional<FlightDTO> getByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber) {
        List<Flight> list = flightRepository.findByAirlineIdAndFlightNumber(airlineId, flightNumber);
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.ofNullable(FlightDTO.fromEntity(list.get(0)));
    }

    @Override
    public Optional<FlightDTO> updateStatus(UUID id, FlightStatus status) {
        return flightRepository.findById(id)
                .map(f -> {
                    f.setStatus(status);
                    return flightRepository.save(f);
                })
                .map(FlightDTO::fromEntity);
    }

    @Override
    public FlightDTO create(FlightDTO dto) {
        Flight entity = FlightDTO.toEntity(dto);
        Flight saved = flightRepository.save(entity);
        return FlightDTO.fromEntity(saved);
    }

    @Override
    public Optional<FlightDTO> update(UUID id, FlightDTO dto) {
        return flightRepository.findById(id)
                .map(existing -> {
                    if (dto.getAirlineId() != null) {
                        com.aircargo.entity.Airline a = new com.aircargo.entity.Airline();
                        a.setId(dto.getAirlineId());
                        existing.setAirline(a);
                    }
                    existing.setFlightNumber(dto.getFlightNumber());
                    existing.setOrigin(dto.getOrigin());
                    existing.setDestination(dto.getDestination());
                    existing.setAircraftReg(dto.getAircraftReg());
                    existing.setAircraftType(dto.getAircraftType());
                    existing.setFlightDate(dto.getFlightDate());
                    existing.setStatus(dto.getStatus());
                    existing.setMaxPayloadKg(dto.getMaxPayloadKg());
                    existing.setTotalPositions(dto.getTotalPositions());
                    existing.setNotes(dto.getNotes());
                    return flightRepository.save(existing);
                })
                .map(FlightDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!flightRepository.existsById(id)) return false;
        flightRepository.deleteById(id);
        return true;
    }
}
