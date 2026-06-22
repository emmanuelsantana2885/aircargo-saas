package com.aircargo.service;

import com.aircargo.dto.UldAwbDTO;
import com.aircargo.dto.UldDTO;
import com.aircargo.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Uld;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.UldAwbRepository;
import com.aircargo.repository.UldRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UldServiceImpl implements UldService{

    private final UldRepository uldRepository;
    private final FlightRepository flightRepository;
    private final UldAwbRepository uldAwbRepository;

    public UldServiceImpl(UldRepository uldRepository, FlightRepository flightRepository, UldAwbRepository uldAwbRepository) {
        this.uldRepository = uldRepository;
        this.flightRepository = flightRepository;
        this.uldAwbRepository = uldAwbRepository;
    }

    private UldDTO enrichWithAwbs(UldDTO dto) {
        if (dto == null || dto.getId() == null) return dto;
        List<UldAwbDTO> awbs = uldAwbRepository.findByUldId(dto.getId())
                .stream().map(UldAwbDTO::fromEntity).collect(Collectors.toList());
        dto.setAwbs(awbs);
        return dto;
    }

    @Override
    public List<UldDTO> getAll(UUID airlineId, UUID flightId) {
        List<Uld> results;
        if (flightId != null) results = uldRepository.findByFlightId(flightId);
        else if (airlineId != null) results = uldRepository.findByAirlineId(airlineId);
        else results = uldRepository.findAll();
        return results.stream()
                .map(UldDTO::fromEntity)
                .map(this::enrichWithAwbs)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UldDTO> getById(UUID id) {
        return uldRepository.findById(id)
                .map(UldDTO::fromEntity)
                .map(this::enrichWithAwbs);
    }

    @Override
    public UldDTO create(UldDTO dto) {
        Uld e = UldDTO.toEntity(dto);
        Uld saved = uldRepository.save(e);
        return enrichWithAwbs(UldDTO.fromEntity(saved));
    }

    @Override
    public Optional<UldDTO> update(UUID id, UldDTO dto) {
        return uldRepository.findById(id)
                .map(existing -> {
                    if (dto.getAirlineId() != null) {
                        Airline airline = new Airline();
                        airline.setId(dto.getAirlineId());
                        existing.setAirline(airline);
                    }
                    if (dto.getFlightId() != null) {
                        Flight flight = new Flight();
                        flight.setId(dto.getFlightId());
                        existing.setFlight(flight);
                    }
                    if (dto.getUldNumber() != null) existing.setUldNumber(dto.getUldNumber());
                    if (dto.getUldType() != null) existing.setUldType(dto.getUldType());
                    if (dto.getPosition() != null) existing.setPosition(dto.getPosition());
                    if (dto.getConfig() != null) existing.setConfig(dto.getConfig());
                    if (dto.getSealNumber() != null) existing.setSealNumber(dto.getSealNumber());
                    if (dto.getTareLbs() != null) existing.setTareLbs(dto.getTareLbs());
                    if (dto.getGrossWeightLbs() != null) existing.setGrossWeightLbs(dto.getGrossWeightLbs());
                    if (dto.getNetWeightLbs() != null) existing.setNetWeightLbs(dto.getNetWeightLbs());
                    if (dto.getTareKg() != null) existing.setTareKg(dto.getTareKg());
                    if (dto.getGrossWeightKg() != null) existing.setGrossWeightKg(dto.getGrossWeightKg());
                    if (dto.getNetWeightKg() != null) existing.setNetWeightKg(dto.getNetWeightKg());
                    if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
                    if (dto.getBuiltAt() != null) existing.setBuiltAt(dto.getBuiltAt());
                    if (dto.getLoadedAt() != null) existing.setLoadedAt(dto.getLoadedAt());
                    if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
                    return uldRepository.save(existing);
                })
                .map(UldDTO::fromEntity)
                .map(this::enrichWithAwbs);
    }

    @Override
    public UldDTO assignFlight(UUID id, UUID flightId) {
        Uld uld = uldRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ULD not found: " + id));
        if (flightId != null) {
            Flight flight = flightRepository.getReferenceById(flightId);
            uld.setFlight(flight);
        } else {
            uld.setFlight(null);
        }
        Uld saved = uldRepository.save(uld);
        return enrichWithAwbs(UldDTO.fromEntity(saved));
    }

    @Override
    public boolean delete(UUID id) {
        if (!uldRepository.existsById(id)) return false;
        uldRepository.deleteById(id);
        return true;
    }
}
