package com.aircargo.flightservice.repository;

import com.aircargo.flightservice.entity.Flight;
import com.aircargo.flightservice.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FlightRepository extends JpaRepository<Flight, UUID> {

    List<Flight> findByAirlineId(UUID airlineId);

    List<Flight> findByAirlineIdAndFlightDate(UUID airlineId, LocalDate flightDate);

    List<Flight> findByAirlineIdAndStatus(UUID airlineId, FlightStatus status);

    List<Flight> findByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber);
}
