package com.aircargo.service;

import com.aircargo.dto.MawbDTO;
import com.aircargo.entity.MawbStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MawbService {
    List<MawbDTO> getAll(UUID airlineId, UUID flightId, MawbStatus status);
    Optional<MawbDTO> getById(UUID id);
    Optional<MawbDTO> getByAirlineIdAndAwbNumber(UUID airlineId, String awbNumber);
    Optional<MawbDTO> updateStatus(UUID id, MawbStatus status);
    MawbDTO create(MawbDTO dto);
    Optional<MawbDTO> update(UUID id, MawbDTO dto);
    boolean delete(UUID id);
}
