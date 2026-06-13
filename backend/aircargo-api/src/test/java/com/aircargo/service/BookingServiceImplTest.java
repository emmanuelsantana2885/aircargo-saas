package com.aircargo.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.aircargo.dto.BookingDTO;
import com.aircargo.entity.Airline;
import com.aircargo.entity.Booking;
import com.aircargo.entity.CommodityType;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Mawb;
import com.aircargo.repository.BookingRepository;

class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new BookingServiceImpl(bookingRepository);
    }

    private Booking makeBooking(UUID id, UUID airlineId, UUID flightId, UUID mawbId) {
        Booking booking = new Booking();
        booking.setId(id);
        if (airlineId != null) {
            Airline airline = new Airline();
            airline.setId(airlineId);
            booking.setAirline(airline);
        }
        if (flightId != null) {
            Flight flight = new Flight();
            flight.setId(flightId);
            booking.setFlight(flight);
        }
        if (mawbId != null) {
            Mawb mawb = new Mawb();
            mawb.setId(mawbId);
            booking.setMawb(mawb);
        }
        booking.setClientName("Client One");
        booking.setContactName("Contact One");
        booking.setCnee("CNEE");
        booking.setShipperName("Shipper One");
        booking.setAwbNumber("AWB123");
        booking.setSkids(2);
        booking.setUnits(4);
        booking.setReservedKg(new BigDecimal("150.0"));
        booking.setConfirmedKg(new BigDecimal("140.0"));
        booking.setReceivedKg(new BigDecimal("130.0"));
        booking.setFulfillmentPct(new BigDecimal("0.8667"));
        booking.setDestination("MIA");
        booking.setPriority(1);
        booking.setCommodityType(CommodityType.DRY_CARGO);
        booking.setDayReceived("12:00");
        booking.setTimeHours(new BigDecimal("1.25"));
        booking.setPositions(new BigDecimal("2.50"));
        booking.setRealPositions(new BigDecimal("2.35"));
        booking.setLastWeekKg(new BigDecimal("100.0"));
        booking.setLastWeekPositions(new BigDecimal("2.00"));
        booking.setIsConfirmed(true);
        booking.setNotes("Test booking");
        booking.setCreatedAt(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());
        return booking;
    }

    @Test
    void getAll_filtersByFlightId() {
        UUID flightId = UUID.randomUUID();
        Booking booking = makeBooking(UUID.randomUUID(), UUID.randomUUID(), flightId, null);
        when(bookingRepository.findByFlightId(flightId)).thenReturn(List.of(booking));

        List<BookingDTO> result = service.getAll(null, flightId);

        assertEquals(1, result.size());
        assertEquals("AWB123", result.get(0).getAwbNumber());
        verify(bookingRepository).findByFlightId(flightId);
    }

    @Test
    void getAll_filtersByAirlineId() {
        UUID airlineId = UUID.randomUUID();
        Booking booking = makeBooking(UUID.randomUUID(), airlineId, null, null);
        when(bookingRepository.findByAirlineId(airlineId)).thenReturn(List.of(booking));

        List<BookingDTO> result = service.getAll(airlineId, null);

        assertEquals(1, result.size());
        assertEquals(airlineId, result.get(0).getAirlineId());
        verify(bookingRepository).findByAirlineId(airlineId);
    }

    @Test
    void getById_returnsBooking() {
        UUID id = UUID.randomUUID();
        Booking booking = makeBooking(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

        Optional<BookingDTO> result = service.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Client One", result.get().getClientName());
    }

    @Test
    void create_savesBooking() {
        BookingDTO dto = new BookingDTO();
        dto.setClientName("Client Two");
        dto.setShipperName("Shipper Two");
        dto.setAwbNumber("AWB456");
        dto.setReservedKg(new BigDecimal("50.0"));
        dto.setCommodityType(CommodityType.DRY_CARGO);

        Booking saved = makeBooking(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null);
        saved.setClientName("Client Two");
        when(bookingRepository.save(any())).thenReturn(saved);

        BookingDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("Client Two", result.getClientName());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void update_existingBooking() {
        UUID id = UUID.randomUUID();
        Booking existing = makeBooking(id, UUID.randomUUID(), UUID.randomUUID(), null);
        when(bookingRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDTO dto = new BookingDTO();
        dto.setClientName("Client Updated");
        dto.setShipperName("Shipper Updated");
        dto.setAwbNumber("AWB789");

        Optional<BookingDTO> result = service.update(id, dto);

        assertTrue(result.isPresent());
        assertEquals("Client Updated", result.get().getClientName());
        assertEquals(id, result.get().getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateAwb_existingBooking() {
        UUID id = UUID.randomUUID();
        Booking existing = makeBooking(id, UUID.randomUUID(), UUID.randomUUID(), null);
        when(bookingRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<BookingDTO> result = service.updateAwb(id, "AWB-UPDATED");

        assertTrue(result.isPresent());
        assertEquals("AWB-UPDATED", result.get().getAwbNumber());
        verify(bookingRepository).save(existing);
    }

    @Test
    void delete_existingBooking() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.existsById(id)).thenReturn(true);

        boolean deleted = service.delete(id);

        assertTrue(deleted);
        verify(bookingRepository).deleteById(id);
    }
}
