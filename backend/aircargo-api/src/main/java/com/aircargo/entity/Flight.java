package com.aircargo.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name= "flight")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor



public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name= "id", updatable = false, nullable = false)
    private UUID id;

    // FK a airline
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Airline airline;

    //Numero de vuelo: "335","767"
    @Column(name = "flight_number", nullable = false, length = 20)
    private String flightNumber;

    //Ruta: SDQ -> MIA
    @Column(name = "origin", nullable = false, columnDefinition = "bpchar(3)")
    private String origin;

    @Column(name = "destination", nullable = false, columnDefinition = "bpchar(3)")
    private String destination;

    //Matricula: "N-349-UP"
    @Column(name = "aircraft_reg", length = 20)
    private String aircraftReg;

    //Tipo de aeronave - ENUM de PostgreSQL
    @Column(name = "aircraft_type", columnDefinition = "aircraft_type")
    @Enumerated(EnumType.STRING)
    private AircraftType aircraftType;

    //Fecha del vuelo
    @Column(name = "flight_date", nullable = false)
    private LocalDate flightDate;

    //Estado del vuelo -- ENUM de PostgreSQL
    @Column(name = "Status", nullable = false, columnDefinition = "flight_status")
    @Enumerated(EnumType.STRING)
    private FlightStatus status = FlightStatus.SCHEDULED;

    //Capacidad maxima en kg
    @Column(name = "max_payload_kg", precision = 10, scale =2)
    private BigDecimal maxPayloadkg;

    //Total de posiciones ULD
    @Column(name = "total_positions")
    private Integer totalPositions;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //Constructor util
    public Flight(Airline airline, String flightNumber, String origin,
                  String destination, String aircraftReg, LocalDate flightDate){
        this.airline = airline;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.aircraftReg = aircraftReg;
        this.flightDate = flightDate;
        this.status = FlightStatus.SCHEDULED;
    }
}
