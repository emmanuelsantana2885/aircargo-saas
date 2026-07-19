package com.aircargo.service;

import com.aircargo.dto.UldDTO;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Uld;
import com.aircargo.entity.UldStatus;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.UldAwbRepository;
import com.aircargo.repository.UldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UldServiceImplTest {

    @Mock
    private UldRepository repository;

    @Mock
    private FlightRepository flightRepo;

    @Mock
    private UldAwbRepository uldAwbRepo;

    private UldServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UldServiceImpl(repository, flightRepo, uldAwbRepo);
        when(uldAwbRepo.findByUldId(any())).thenReturn(Collections.emptyList());
    }

    private Uld makeUld(UUID id, UUID airlineId, UUID flightId) {
        Uld uld = new Uld();
        uld.setId(id);
        if (airlineId != null) {
            Airline airline = new Airline();
            airline.setId(airlineId);
            uld.setAirline(airline);
        }
        if (flightId != null) {
            Flight flight = new Flight();
            flight.setId(flightId);
            uld.setFlight(flight);
        }
        uld.setUldNumber("ULD123");
        uld.setUldType(null);
        uld.setStatus(UldStatus.OPEN);
        uld.setGrossWeightKg(new BigDecimal("100.0"));
        return uld;
    }

    @Test
    void getAll_filtersByFlightId() {
        UUID flightId = UUID.randomUUID();
        Uld uld = makeUld(UUID.randomUUID(), UUID.randomUUID(), flightId);
        when(repository.findByFlightId(flightId)).thenReturn(List.of(uld));

        List<UldDTO> result = service.getAll(null, flightId);

        assertEquals(1, result.size());
        assertEquals(flightId, result.get(0).getFlightId());
        verify(repository).findByFlightId(flightId);
    }

    @Test
    void getAll_filtersByAirlineId() {
        UUID airlineId = UUID.randomUUID();
        Uld uld = makeUld(UUID.randomUUID(), airlineId, null);
        when(repository.findByAirlineId(airlineId)).thenReturn(List.of(uld));

        List<UldDTO> result = service.getAll(airlineId, null);

        assertEquals(1, result.size());
        assertEquals(airlineId, result.get(0).getAirlineId());
        verify(repository).findByAirlineId(airlineId);
    }

    @Test
    void getById_returnsUld() {
        UUID id = UUID.randomUUID();
        Uld uld = makeUld(id, UUID.randomUUID(), UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(uld));

        Optional<UldDTO> result = service.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("ULD123", result.get().getUldNumber());
    }

    @Test
    void create_savesUld() {
        UldDTO dto = new UldDTO();
        dto.setUldNumber("ULD456");
        dto.setGrossWeightKg(new BigDecimal("120.0"));
        dto.setStatus(UldStatus.LOADED);

        Uld saved = makeUld(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        saved.setUldNumber("ULD456");
        when(repository.save(any())).thenReturn(saved);

        UldDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("ULD456", result.getUldNumber());
        verify(repository).save(any(Uld.class));
    }

    @Test
    void update_existingUld() {
        UUID id = UUID.randomUUID();
        Uld existing = makeUld(id, UUID.randomUUID(), UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UldDTO dto = new UldDTO();
        dto.setUldNumber("ULD789");

        Optional<UldDTO> result = service.update(id, dto);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("ULD789", result.get().getUldNumber());
        verify(repository).save(any(Uld.class));
    }

    @Test
    void delete_existingUld() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        boolean deleted = service.delete(id);

        assertTrue(deleted);
        verify(repository).deleteById(id);
    }
}
