package com.aircargo.service;

import com.aircargo.dto.HawbDTO;
import com.aircargo.entity.Hawb;
import com.aircargo.repository.HawbRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HawbServiceImpl implements HawbService{

    private final HawbRepository hawbRepository;

    public HawbServiceImpl(HawbRepository hawbRepository) {
        this.hawbRepository = hawbRepository;
    }

    @Override
    public List<HawbDTO> getAll(UUID airlineId, UUID mawbId) {
        List<Hawb> results;
        if (mawbId != null) results = hawbRepository.findByMawbId(mawbId);
        else if (airlineId != null) results = hawbRepository.findByAirlineId(airlineId);
        else results = hawbRepository.findAll();
        return results.stream().map(HawbDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<HawbDTO> getById(UUID id) {
        return hawbRepository.findById(id).map(HawbDTO::fromEntity);
    }

    @Override
    public HawbDTO create(HawbDTO dto) {
        Hawb e = HawbDTO.toEntity(dto);
        Hawb saved = hawbRepository.save(e);
        return HawbDTO.fromEntity(saved);
    }

    @Override
    public Optional<HawbDTO> update(UUID id, HawbDTO dto) {
        return hawbRepository.findById(id)
                .map(existing -> {
                    Hawb updated = HawbDTO.toEntity(dto);
                    updated.setId(existing.getId());
                    return hawbRepository.save(updated);
                })
                .map(HawbDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!hawbRepository.existsById(id)) return false;
        hawbRepository.deleteById(id);
        return true;
    }
}
