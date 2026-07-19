package com.aircargo.service;

import com.aircargo.dto.AirlineDTO;
import com.aircargo.common.entity.Airline;
import com.aircargo.repository.AirlineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AirlineServiceImplTest {

    @Mock
    private AirlineRepository repository;

    private AirlineServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AirlineServiceImpl(repository);
    }

    private Airline makeAirline(UUID id) {
        Airline airline = new Airline();
        airline.setId(id);
        airline.setCode("TEST");
        airline.setName("Test Airline");
        airline.setIataCode("TA");
        airline.setCountry("US");
        airline.setIsActive(true);
        airline.setCreatedAt(OffsetDateTime.now());
        return airline;
    }

    @Test
    void getAll_returnsList() {
        Airline airline = makeAirline(UUID.randomUUID());
        when(repository.findAll()).thenReturn(List.of(airline));

        List<AirlineDTO> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("TEST", result.get(0).getCode());
        verify(repository).findAll();
    }

    @Test
    void getById_returnsAirline() {
        UUID id = UUID.randomUUID();
        Airline airline = makeAirline(id);
        when(repository.findById(id)).thenReturn(Optional.of(airline));

        Optional<AirlineDTO> result = service.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Test Airline", result.get().getName());
    }

    @Test
    void create_savesAirline() {
        AirlineDTO dto = new AirlineDTO();
        dto.setCode("NEW");
        dto.setName("New Airline");
        dto.setIataCode("NA");

        Airline saved = makeAirline(UUID.randomUUID());
        saved.setCode("NEW");
        saved.setName("New Airline");
        when(repository.save(any())).thenReturn(saved);

        AirlineDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("NEW", result.getCode());
        verify(repository).save(any(Airline.class));
    }

    @Test
    void update_existingAirline() {
        UUID id = UUID.randomUUID();
        Airline existing = makeAirline(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AirlineDTO dto = new AirlineDTO();
        dto.setCode("UPD");
        dto.setName("Updated Airline");

        Optional<AirlineDTO> result = service.update(id, dto);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("UPD", result.get().getCode());
        verify(repository).save(any(Airline.class));
    }

    @Test
    void delete_existingAirline() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        boolean deleted = service.delete(id);

        assertTrue(deleted);
        verify(repository).deleteById(id);
    }
}
