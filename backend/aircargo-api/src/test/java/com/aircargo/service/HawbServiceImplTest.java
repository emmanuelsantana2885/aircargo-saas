package com.aircargo.service;

import com.aircargo.dto.HawbDTO;
import com.aircargo.entity.Airline;
import com.aircargo.entity.Hawb;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.repository.HawbRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HawbServiceImplTest {

    @Mock
    private HawbRepository repository;

    private HawbServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new HawbServiceImpl(repository);
    }

    private Hawb makeHawb(UUID id, UUID airlineId, UUID mawbId) {
        Hawb hawb = new Hawb();
        hawb.setId(id);
        if (airlineId != null) {
            Airline airline = new Airline();
            airline.setId(airlineId);
            hawb.setAirline(airline);
        }
        if (mawbId != null) {
            Mawb mawb = new Mawb();
            mawb.setId(mawbId);
            hawb.setMawb(mawb);
        }
        hawb.setHawbNumber("HAWB123");
        hawb.setDestination("JFK");
        hawb.setPieces(10);
        hawb.setWeightKg(new BigDecimal("75.0"));
        hawb.setStatus(MawbStatus.BOOKED);
        return hawb;
    }

    @Test
    void getAll_filtersByMawbId() {
        UUID mawbId = UUID.randomUUID();
        Hawb hawb = makeHawb(UUID.randomUUID(), UUID.randomUUID(), mawbId);
        when(repository.findByMawbId(mawbId)).thenReturn(List.of(hawb));

        List<HawbDTO> result = service.getAll(null, mawbId);

        assertEquals(1, result.size());
        assertEquals(mawbId, result.get(0).getMawbId());
        verify(repository).findByMawbId(mawbId);
    }

    @Test
    void getAll_filtersByAirlineId() {
        UUID airlineId = UUID.randomUUID();
        Hawb hawb = makeHawb(UUID.randomUUID(), airlineId, null);
        when(repository.findByAirlineId(airlineId)).thenReturn(List.of(hawb));

        List<HawbDTO> result = service.getAll(airlineId, null);

        assertEquals(1, result.size());
        assertEquals(airlineId, result.get(0).getAirlineId());
        verify(repository).findByAirlineId(airlineId);
    }

    @Test
    void getById_returnsHawb() {
        UUID id = UUID.randomUUID();
        Hawb hawb = makeHawb(id, UUID.randomUUID(), UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(hawb));

        Optional<HawbDTO> result = service.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("HAWB123", result.get().getHawbNumber());
    }

    @Test
    void create_savesHawb() {
        HawbDTO dto = new HawbDTO();
        dto.setHawbNumber("HAWB456");
        dto.setDestination("LAX");
        dto.setPieces(5);
        dto.setWeightKg(new BigDecimal("20.0"));
        dto.setStatus(MawbStatus.RECEIVED);

        Hawb saved = makeHawb(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        saved.setHawbNumber("HAWB456");
        when(repository.save(any())).thenReturn(saved);

        HawbDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("HAWB456", result.getHawbNumber());
        verify(repository).save(any(Hawb.class));
    }

    @Test
    void update_existingHawb() {
        UUID id = UUID.randomUUID();
        Hawb existing = makeHawb(id, UUID.randomUUID(), UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        HawbDTO dto = new HawbDTO();
        dto.setHawbNumber("HAWB789");

        Optional<HawbDTO> result = service.update(id, dto);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("HAWB789", result.get().getHawbNumber());
        verify(repository).save(any(Hawb.class));
    }

    @Test
    void delete_existingHawb() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        boolean deleted = service.delete(id);

        assertTrue(deleted);
        verify(repository).deleteById(id);
    }
}
