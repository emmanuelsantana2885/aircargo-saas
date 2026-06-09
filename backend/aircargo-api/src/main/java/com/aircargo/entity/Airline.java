package com.aircargo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data                           //lombok: Genera getters, setters, toString, equals, hashCode
@Builder                        //Lombok: patron builder para crear objetos
@NoArgsConstructor              //Lombok: constructor vacio -- requerido por JPA
@AllArgsConstructor             //Lombok: constructor con todos los campos
@Entity                         //JPA: esta clase es una entidad de base de datos
@Table(name = "airline")        //JPA:mapea a la tabla "airline"
@Getter
@Setter

public class Airline {

    @Id // esta es la llave primaria
    @GeneratedValue(strategy = GenerationType.UUID) // UUID generado automaticamente

    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;        //"UPS", "AA", "DL"

    @Column(name = "name", nullable = false, length= 100)
    private String name;

    @Column(name = "iata_code", length = 3)
    private String iataCode;    //"5X"

    @Column(name = "country", length = 60)
    private String country;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp              //Hibernate: auto-set al crear
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    //Constructor adicional util
    public Airline(String code, String name, String iataCode, String country){
        this.code = code;
        this.name = name;
        this.iataCode = iataCode;
        this.country = country;
        this.isActive = true;

    }
}
