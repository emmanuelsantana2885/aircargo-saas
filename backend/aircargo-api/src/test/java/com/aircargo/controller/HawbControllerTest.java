package com.aircargo.controller;

import com.aircargo.entity.Airline;
import com.aircargo.entity.Hawb;
import com.aircargo.entity.Mawb;
import com.aircargo.repository.HawbRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HawbControllerTest {

    @Mock
    private HawbRepository repository;

    private HawbController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new HawbController(repository);
    }

    private Hawb makeHawb(UUID id, UUID airlineId, UUID mawbId) {
        Hawb h = new Hawb();
        h.setId(id);
        if (airlineId != null) {
            Airline a = new Airline();
            a.setId(airlineId);
            h.setAirline(a);
        }
        if (mawbId != null) {
            Mawb m = new Mawb();
            m.setId(mawbId);
            h.setMawb(m);
        }
        h.setHawbNumber("H123");
        h.setWeightKg(new BigDecimal("10"));
        return h;
    }

    @Test
    void getAll_filtersByMawbId() {
        UUID mawbId = UUID.randomUUID();
        Hawb h = makeHawb(UUID.randomUUID(), UUID.randomUUID(), mawbId);
        when(repository.findByMawbId(mawbId)).thenReturn(List.of(h));

        List<Hawb> res = controller.getAll(null, mawbId);

        assertEquals(1, res.size());
        assertEquals(mawbId, res.get(0).getMawb().getId());
    }

    @Test
    void getById_returnsHawb() {
        UUID id = UUID.randomUUID();
        Hawb h = makeHawb(id, UUID.randomUUID(), UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(h));

        ResponseEntity<Hawb> out = controller.getById(id);

        assertEquals(200, out.getStatusCodeValue());
        assertEquals(id, out.getBody().getId());
    }

    @Test
    void create_savesHawb() {
        Hawb in = makeHawb(null, UUID.randomUUID(), UUID.randomUUID());
        Hawb saved = makeHawb(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(repository.save(in)).thenReturn(saved);

        ResponseEntity<Hawb> out = controller.create(in);

        assertEquals(200, out.getStatusCodeValue());
        assertNotNull(out.getBody());
    }

}
