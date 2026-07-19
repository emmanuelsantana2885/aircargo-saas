package com.aircargo.service;

import com.aircargo.dto.AppUserDTO;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.AppUser;
import com.aircargo.entity.UserRole;
import com.aircargo.repository.AppUserRepository;
import com.aircargo.repository.SiteRepository;
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

class AppUserServiceImplTest {

    @Mock
    private AppUserRepository repository;

    @Mock
    private SiteRepository siteRepository;

    private AppUserServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AppUserServiceImpl(repository, siteRepository);
    }

    private AppUser makeUser(UUID id, UUID airlineId) {
        AppUser user = new AppUser();
        user.setId(id);
        if (airlineId != null) {
            Airline airline = new Airline();
            airline.setId(airlineId);
            user.setAirline(airline);
        }
        user.setSupabaseUid(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setFullName("Test User");
        user.setRole(UserRole.WAREHOUSE_ASSISTANT);
        user.setIsActive(true);
        user.setLastLogin(OffsetDateTime.now());
        return user;
    }

    @Test
    void getAll_filtersByAirlineId() {
        UUID airlineId = UUID.randomUUID();
        AppUser user = makeUser(UUID.randomUUID(), airlineId);
        when(repository.findByAirlineId(airlineId)).thenReturn(List.of(user));

        List<AppUserDTO> result = service.getAll(airlineId);

        assertEquals(1, result.size());
        assertEquals(airlineId, result.get(0).getAirlineId());
        verify(repository).findByAirlineId(airlineId);
    }

    @Test
    void getById_returnsUser() {
        UUID id = UUID.randomUUID();
        AppUser user = makeUser(id, UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(user));

        Optional<AppUserDTO> result = service.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("user@example.com", result.get().getEmail());
    }

    @Test
    void create_savesUser() {
        UUID supabaseUid = UUID.randomUUID();
        AppUserDTO dto = new AppUserDTO();
        dto.setSupabaseUid(supabaseUid);
        dto.setEmail("new@example.com");
        dto.setFullName("New User");
        dto.setRole(UserRole.WAREHOUSE_ASSISTANT);

        AppUser saved = makeUser(UUID.randomUUID(), UUID.randomUUID());
        saved.setSupabaseUid(supabaseUid);
        saved.setEmail("new@example.com");
        saved.setFullName("New User");
        when(repository.save(any())).thenReturn(saved);

        AppUserDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        verify(repository).save(any(AppUser.class));
    }

    @Test
    void update_existingUser() {
        UUID id = UUID.randomUUID();
        AppUser existing = makeUser(id, UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UUID supabaseUid = UUID.randomUUID();
        AppUserDTO dto = new AppUserDTO();
        dto.setEmail("updated@example.com");
        dto.setFullName("Updated User");
        dto.setSupabaseUid(supabaseUid);
        dto.setRole(UserRole.WAREHOUSE_ASSISTANT);

        Optional<AppUserDTO> result = service.update(id, dto);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("updated@example.com", result.get().getEmail());
        verify(repository).save(any(AppUser.class));
    }

    @Test
    void delete_existingUser() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        boolean deleted = service.delete(id);

        assertTrue(deleted);
        verify(repository).deleteById(id);
    }
}
