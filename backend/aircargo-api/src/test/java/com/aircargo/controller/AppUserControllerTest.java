package com.aircargo.controller;

import com.aircargo.entity.Airline;
import com.aircargo.entity.AppUser;
import com.aircargo.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserControllerTest {

    @Mock
    private AppUserRepository repository;

    private AppUserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AppUserController(repository);
    }

    private AppUser makeUser(UUID id, UUID airlineId) {
        AppUser u = new AppUser();
        u.setId(id);
        if (airlineId != null) {
            Airline a = new Airline();
            a.setId(airlineId);
            u.setAirline(a);
        }
        u.setFullName("Test User");
        u.setEmail("t@example.com");
        return u;
    }

    @Test
    void getAll_filtersByAirline() {
        UUID airlineId = UUID.randomUUID();
        AppUser u = makeUser(UUID.randomUUID(), airlineId);
        when(repository.findByAirlineId(airlineId)).thenReturn(List.of(u));

        List<AppUser> res = controller.getAll(airlineId);

        assertEquals(1, res.size());
        assertEquals(airlineId, res.get(0).getAirline().getId());
    }

    @Test
    void getById_returnsUser() {
        UUID id = UUID.randomUUID();
        AppUser u = makeUser(id, null);
        when(repository.findById(id)).thenReturn(Optional.of(u));

        ResponseEntity<AppUser> out = controller.getById(id);

        assertEquals(200, out.getStatusCodeValue());
        assertEquals(id, out.getBody().getId());
    }

}
