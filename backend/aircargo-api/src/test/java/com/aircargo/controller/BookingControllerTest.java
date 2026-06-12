package com.aircargo.controller;

import com.aircargo.dto.BookingDTO;
import com.aircargo.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    private BookingController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new BookingController(bookingService);
    }

    @Test
    void updateAwb_returnsUpdated_whenFound() {
        UUID id = UUID.randomUUID();
        BookingDTO dto = new BookingDTO();
        dto.setId(id);
        dto.setAwbNumber("AWB123");
        when(bookingService.updateAwb(id, "AWB123")).thenReturn(Optional.of(dto));

        ResponseEntity<BookingDTO> res = controller.updateAwb(id, Map.of("awbNumber", "AWB123"));

        assertEquals(200, res.getStatusCodeValue());
        assertNotNull(res.getBody());
        assertEquals("AWB123", res.getBody().getAwbNumber());
    }

    @Test
    void updateAwb_returnsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(bookingService.updateAwb(id, "AWB123")).thenReturn(Optional.empty());

        ResponseEntity<BookingDTO> res = controller.updateAwb(id, Map.of("awbNumber", "AWB123"));

        assertEquals(404, res.getStatusCodeValue());
    }

    @Test
    void updateAwb_returnsBadRequest_whenNoAwbProvided() {
        UUID id = UUID.randomUUID();

        ResponseEntity<BookingDTO> res = controller.updateAwb(id, Map.of());

        assertEquals(400, res.getStatusCodeValue());
    }
}
