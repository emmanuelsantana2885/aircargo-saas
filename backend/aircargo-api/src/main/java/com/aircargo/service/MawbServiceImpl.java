package com.aircargo.service;

import com.aircargo.dto.MawbDTO;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.repository.AirlineRepository;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.MawbRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MawbServiceImpl implements MawbService {

    private final MawbRepository mawbRepository;
    private final AirlineRepository airlineRepository;
    private final FlightRepository flightRepository;

    public MawbServiceImpl(MawbRepository mawbRepository,
                            AirlineRepository airlineRepository,
                            FlightRepository flightRepository) {
        this.mawbRepository = mawbRepository;
        this.airlineRepository = airlineRepository;
        this.flightRepository = flightRepository;
    }

    @Override
    public List<MawbDTO> getAll(UUID airlineId, UUID flightId, MawbStatus status) {
        List<Mawb> results;
        if (flightId != null) {
            results = mawbRepository.findByFlightId(flightId);
        } else if (airlineId != null && status != null) {
            results = mawbRepository.findByAirlineIdAndStatus(airlineId, status);
        } else if (airlineId != null) {
            results = mawbRepository.findByAirlineId(airlineId);
        } else {
            results = mawbRepository.findAll();
        }
        return results.stream()
                .map(MawbDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MawbDTO> getById(UUID id) {
        return mawbRepository.findById(id)
                .map(MawbDTO::fromEntity);
    }

    @Override
    public Optional<MawbDTO> getByAirlineIdAndAwbNumber(UUID airlineId, String awbNumber) {
        return mawbRepository.findByAirlineIdAndAwbNumber(airlineId, awbNumber)
                .map(MawbDTO::fromEntity);
    }

    @Override
    public Optional<MawbDTO> updateStatus(UUID id, MawbStatus status) {
        return mawbRepository.findById(id)
                .map(mawb -> {
                    mawb.setStatus(status);
                    return mawbRepository.save(mawb);
                })
                .map(MawbDTO::fromEntity);
    }

    @Override
    public MawbDTO create(MawbDTO dto) {
        Airline airline = airlineRepository.findById(dto.getAirlineId())
                .orElseThrow(() -> new IllegalArgumentException("Airline not found: " + dto.getAirlineId()));

        Flight flight = null;
        if (dto.getFlightId() != null) {
            flight = flightRepository.findById(dto.getFlightId())
                    .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + dto.getFlightId()));
        }

        Mawb entity = MawbDTO.toEntity(dto, airline, flight);
        entity.setId(null);
        Mawb saved = mawbRepository.save(entity);
        return MawbDTO.fromEntity(saved);
    }

    @Override
    public Optional<MawbDTO> update(UUID id, MawbDTO dto) {
        return mawbRepository.findById(id)
                .map(existing -> {
                    Airline airline = airlineRepository.findById(dto.getAirlineId())
                            .orElseThrow(() -> new IllegalArgumentException("Airline not found: " + dto.getAirlineId()));

                    Flight flight = null;
                    if (dto.getFlightId() != null) {
                        flight = flightRepository.findById(dto.getFlightId())
                                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + dto.getFlightId()));
                    }

                    Mawb updated = MawbDTO.toEntity(dto, airline, flight);
                    updated.setId(existing.getId());
                    return mawbRepository.save(updated);
                })
                .map(MawbDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!mawbRepository.existsById(id)) return false;
        mawbRepository.deleteById(id);
        return true;
    }
}
