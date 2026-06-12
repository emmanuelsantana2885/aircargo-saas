package com.aircargo.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.aircargo.entity.Airline;
import com.aircargo.entity.Booking;
import com.aircargo.entity.CommodityType;
import com.aircargo.entity.Flight;
import com.aircargo.repository.AirlineRepository;
import com.aircargo.repository.BookingRepository;
import com.aircargo.repository.FlightRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AirlineRepository airlineRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void updateAwb_persistsAwbNumberThroughController() throws Exception {
        Airline airline = airlineRepository.save(new Airline("TST", "Test Airline", "TS", "Testland"));
        Flight flight = new Flight(airline, "TST123", "AAA", "BBB", "N-TEST", LocalDate.now().plusDays(1));
        flight = flightRepository.save(flight);

        Booking booking = new Booking();
        booking.setAirline(airline);
        booking.setFlight(flight);
        booking.setClientName("Client A");
        booking.setContactName("Contact A");
        booking.setReservedKg(new BigDecimal("1000"));
        booking.setCommodityType(CommodityType.DRY_CARGO);
        booking = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/{id}/awb", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"awbNumber\":\"AWB-INT-123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.awbNumber").value("AWB-INT-123"));

        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(updated.getAwbNumber()).isEqualTo("AWB-INT-123");
    }
}
