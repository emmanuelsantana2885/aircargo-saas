package com.aircargo.repository;

import com.aircargo.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByFlightId(UUID flightId);
    List<Booking>findByAirlineId(UUID airlineId);
    List<Booking>findByFlightIdAndCommodityType(
            UUID flightId,
            com.aircargo.entity.CommodityType commodityType);

}
