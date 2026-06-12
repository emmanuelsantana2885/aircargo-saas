package com.aircargo.service;

import com.aircargo.dto.MawbDTO;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.repository.MawbRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MawbServiceImpl implements MawbService {

    private final MawbRepository mawbRepository;

    public MawbServiceImpl(MawbRepository mawbRepository) {
        this.mawbRepository = mawbRepository;
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
}
