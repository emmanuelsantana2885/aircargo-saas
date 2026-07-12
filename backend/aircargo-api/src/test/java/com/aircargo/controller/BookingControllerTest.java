package com.aircargo.controller;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.aircargo.dto.BookingAwbUpdateRequest;
import com.aircargo.dto.BookingDTO;
import com.aircargo.repository.AuditLogRepository;
import com.aircargo.service.AuditService;
import com.aircargo.service.BookingService;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditService auditService;
    private BookingController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditService = new AuditService(auditLogRepository);
        controller = new BookingController(bookingService, auditService);
    }

    @Test
    void updateAwb_returnsUpdated_whenFound() {
        UUID id = UUID.randomUUID();
        BookingDTO dto = new BookingDTO();
        dto.setId(id);
        dto.setAwbNumber("AWB123");
        when(bookingService.updateAwb(id, "AWB123")).thenReturn(Optional.of(dto));

        BookingAwbUpdateRequest request = new BookingAwbUpdateRequest();
        request.setAwbNumber("AWB123");
        ResponseEntity<BookingDTO> res = controller.updateAwb(id, request, null, null);

        assertEquals(200, res.getStatusCodeValue());
        assertNotNull(res.getBody());
        assertEquals("AWB123", res.getBody().getAwbNumber());
    }

    @Test
    void updateAwb_returnsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(bookingService.updateAwb(id, "AWB123")).thenReturn(Optional.empty());

        BookingAwbUpdateRequest request = new BookingAwbUpdateRequest();
        request.setAwbNumber("AWB123");
        ResponseEntity<BookingDTO> res = controller.updateAwb(id, request, null, null);

        assertEquals(404, res.getStatusCodeValue());
    }

    @Test
    void updateAwb_returnsBadRequest_whenNoAwbProvided() {
        UUID id = UUID.randomUUID();

        BookingAwbUpdateRequest request = new BookingAwbUpdateRequest();
        request.setAwbNumber("");
        ResponseEntity<BookingDTO> res = controller.updateAwb(id, request, null, null);

        assertEquals(400, res.getStatusCodeValue());
    }
}
