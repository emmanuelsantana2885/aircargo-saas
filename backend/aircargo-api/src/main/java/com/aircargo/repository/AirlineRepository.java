package com.aircargo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aircargo.entity.Airline;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


public interface AirlineRepository extends JpaRepository<Airline, UUID> {

    //Spring genera el SQL automaticamente desde el nombre del metodo
    Optional<Airline> findByCode(String code);

    boolean existsByCode(String code);
}
