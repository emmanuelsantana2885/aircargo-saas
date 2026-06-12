package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirlineDTO {

    private UUID id;
    private String code;
    private String name;
    private String iataCode;
    private String country;
    private Boolean isActive;
    private OffsetDateTime createdAt;

}
