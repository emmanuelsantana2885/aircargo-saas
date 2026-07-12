package com.aircargo.repository;

import com.aircargo.entity.Uld;
import com.aircargo.entity.UldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UldRepository extends JpaRepository<Uld, UUID> {

    List<Uld> findByFlightId(UUID flightId);

    List<Uld> findByAirlineId(UUID airlineId);

    List<Uld> findByFlightIdAndStatus(UUID flightId, UldStatus status);

    Optional<Uld> findByFlightIdAndUldNumber(UUID flightId, String uldNumber);
}
