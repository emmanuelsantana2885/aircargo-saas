package com.aircargo.repository;

import com.aircargo.entity.Flight;
import com.aircargo.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FlightRepository extends JpaRepository<Flight, UUID> {

    //Todos los vuelos de una aerolinea
    List<Flight> findByAirlineId(UUID airlineid);

    //Vuelos por aerolinea y fecha
    List<Flight> findByAirlineIdAndFlightDate(UUID airlineId, LocalDate flightDate);

    //Vuelos por estado
    List<Flight> findByAirlineIdAndStatus(UUID airlineId, FlightStatus status);

    //Buscar por numero de vuelo
    List<Flight> findByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber);

}
