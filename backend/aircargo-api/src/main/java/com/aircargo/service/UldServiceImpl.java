package com.aircargo.service;

import com.aircargo.dto.UldDTO;
import com.aircargo.entity.Uld;
import com.aircargo.repository.UldRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UldServiceImpl implements UldService{

    private final UldRepository uldRepository;

    public UldServiceImpl(UldRepository uldRepository) {
        this.uldRepository = uldRepository;
    }

    @Override
    public List<UldDTO> getAll(UUID airlineId, UUID flightId) {
        List<Uld> results;
        if (flightId != null) results = uldRepository.findByFlightId(flightId);
        else if (airlineId != null) results = uldRepository.findByAirlineId(airlineId);
        else results = uldRepository.findAll();
        return results.stream().map(UldDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<UldDTO> getById(UUID id) {
        return uldRepository.findById(id).map(UldDTO::fromEntity);
    }

    @Override
    public UldDTO create(UldDTO dto) {
        Uld e = UldDTO.toEntity(dto);
        Uld saved = uldRepository.save(e);
        return UldDTO.fromEntity(saved);
    }

    @Override
    public Optional<UldDTO> update(UUID id, UldDTO dto) {
        return uldRepository.findById(id)
                .map(existing -> {
                    Uld updated = UldDTO.toEntity(dto);
                    updated.setId(existing.getId());
                    return uldRepository.save(updated);
                })
                .map(UldDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!uldRepository.existsById(id)) return false;
        uldRepository.deleteById(id);
        return true;
    }
}
