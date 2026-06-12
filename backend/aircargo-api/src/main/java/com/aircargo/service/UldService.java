package com.aircargo.service;

import com.aircargo.dto.UldDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UldService {
    List<UldDTO> getAll(UUID airlineId, UUID flightId);
    Optional<UldDTO> getById(UUID id);
    UldDTO create(UldDTO dto);
    Optional<UldDTO> update(UUID id, UldDTO dto);
    boolean delete(UUID id);
}
