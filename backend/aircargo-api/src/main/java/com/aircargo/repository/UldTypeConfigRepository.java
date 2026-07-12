package com.aircargo.repository;

import com.aircargo.entity.UldType;
import com.aircargo.entity.UldTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UldTypeConfigRepository extends JpaRepository<UldTypeConfig, UUID> {
    List<UldTypeConfig> findByAirlineId(UUID airlineId);
    Optional<UldTypeConfig> findByAirlineIdAndUldType(UUID airlineId, UldType uldType);
}
