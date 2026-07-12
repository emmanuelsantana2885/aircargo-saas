package com.aircargo.service;

import com.aircargo.dto.LoadPlanningDTO;

import java.util.Optional;
import java.util.UUID;

public interface LoadPlanningService {
    Optional<LoadPlanningDTO> getByFlightId(UUID flightId);
    LoadPlanningDTO closeLoadPlan(UUID flightId);
}
