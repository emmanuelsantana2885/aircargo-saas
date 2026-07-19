package com.aircargo.service;

import com.aircargo.dto.FlightDTO;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.entity.FlightStatus;
import com.aircargo.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    private FlightServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new FlightServiceImpl(flightRepository);
    }

    private Flight makeFlight(UUID id, UUID airlineId, String number, LocalDate date) {
        Flight f = new Flight();
        f.setId(id);
        if (airlineId != null) {
            Airline a = new Airline();
            a.setId(airlineId);
            f.setAirline(a);
        }
        f.setFlightNumber(number);
        f.setFlightDate(date);
        f.setStatus(FlightStatus.SCHEDULED);
        return f;
    }

    @Test
    void getAll_filtersByAirlineAndDate() {
        UUID airlineId = UUID.randomUUID();
        LocalDate d = LocalDate.of(2026,1,1);
        Flight f1 = makeFlight(UUID.randomUUID(), airlineId, "100", d);
        when(flightRepository.findByAirlineIdAndFlightDate(airlineId, d)).thenReturn(List.of(f1));

        List<FlightDTO> res = service.getAll(airlineId, d, null, null);

        assertEquals(1, res.size());
        assertEquals("100", res.get(0).getFlightNumber());
        verify(flightRepository).findByAirlineIdAndFlightDate(airlineId, d);
    }

    @Test
    void getById_found() {
        UUID id = UUID.randomUUID();
        Flight f = makeFlight(id, UUID.randomUUID(), "200", LocalDate.now());
        when(flightRepository.findById(id)).thenReturn(Optional.of(f));

        Optional<FlightDTO> dto = service.getById(id);

        assertTrue(dto.isPresent());
        assertEquals("200", dto.get().getFlightNumber());
    }

    @Test
    void create_and_persists() {
        FlightDTO dto = new FlightDTO();
        dto.setFlightNumber("300");
        dto.setFlightDate(LocalDate.now());

        Flight saved = makeFlight(UUID.randomUUID(), null, "300", dto.getFlightDate());
        when(flightRepository.save(any())).thenReturn(saved);

        FlightDTO res = service.create(dto);

        assertNotNull(res);
        assertEquals("300", res.getFlightNumber());
        ArgumentCaptor<Flight> cap = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(cap.capture());
        assertEquals("300", cap.getValue().getFlightNumber());
    }

    @Test
    void update_existing() {
        UUID id = UUID.randomUUID();
        Flight existing = makeFlight(id, null, "400", LocalDate.now());
        when(flightRepository.findById(id)).thenReturn(Optional.of(existing));
        when(flightRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        FlightDTO dto = new FlightDTO();
        dto.setFlightNumber("401");

        Optional<FlightDTO> updated = service.update(id, dto);

        assertTrue(updated.isPresent());
        assertEquals("401", updated.get().getFlightNumber());
    }

    @Test
    void delete_existing() {
        UUID id = UUID.randomUUID();
        when(flightRepository.existsById(id)).thenReturn(true);

        boolean ok = service.delete(id);

        assertTrue(ok);
        verify(flightRepository).deleteById(id);
    }

    @Test
    void getByAirlineIdAndFlightNumber_returnsFirst() {
        UUID aid = UUID.randomUUID();
        Flight f = makeFlight(UUID.randomUUID(), aid, "500", LocalDate.now());
        when(flightRepository.findByAirlineIdAndFlightNumber(aid, "500")).thenReturn(List.of(f));

        Optional<FlightDTO> res = service.getByAirlineIdAndFlightNumber(aid, "500");

        assertTrue(res.isPresent());
        assertEquals("500", res.get().getFlightNumber());
    }

    @Test
    void updateStatus_changesStatus() {
        UUID id = UUID.randomUUID();
        Flight existing = makeFlight(id, null, "600", LocalDate.now());
        when(flightRepository.findById(id)).thenReturn(Optional.of(existing));
        when(flightRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<FlightDTO> out = service.updateStatus(id, FlightStatus.DEPARTED);

        assertTrue(out.isPresent());
        assertEquals(FlightStatus.DEPARTED, out.get().getStatus());
    }
}
