package com.aircargo.service;

import com.aircargo.dto.HawbDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HawbService {
    List<HawbDTO> getAll(UUID airlineId, UUID mawbId);
    Optional<HawbDTO> getById(UUID id);
    HawbDTO create(HawbDTO dto);
    Optional<HawbDTO> update(UUID id, HawbDTO dto);
    boolean delete(UUID id);
}
