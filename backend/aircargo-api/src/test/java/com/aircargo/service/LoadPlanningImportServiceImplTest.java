package com.aircargo.service;

import com.aircargo.entity.UldType;
import com.aircargo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class LoadPlanningImportServiceImplTest {

    private LoadPlanningImportServiceImpl service;

    @BeforeEach
    void setUp() {
        FlightRepository flightRepository = mock(FlightRepository.class);
        UldRepository uldRepository = mock(UldRepository.class);
        UldAwbRepository uldAwbRepository = mock(UldAwbRepository.class);
        MawbRepository mawbRepository = mock(MawbRepository.class);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        AirlineRepository airlineRepository = mock(AirlineRepository.class);

        service = new LoadPlanningImportServiceImpl(flightRepository, uldRepository, uldAwbRepository,
                mawbRepository, bookingRepository, airlineRepository);
    }

    private UldType invokeDetect(String uldNumber) throws Exception {
        Method m = LoadPlanningImportServiceImpl.class.getDeclaredMethod("detectUldType", String.class);
        m.setAccessible(true);
        return (UldType) m.invoke(service, uldNumber);
    }

    @Test
    void detect_known_prefix_AAD() throws Exception {
        assertEquals(UldType.AAD, invokeDetect("AAD-1234"));
    }

    @Test
    void detect_lowercase_prefix() throws Exception {
        assertEquals(UldType.PMC, invokeDetect("pmc-9999"));
    }

    @Test
    void detect_unknown_prefix_returns_bulk() throws Exception {
        assertEquals(UldType.BULK, invokeDetect("XYZ-000"));
    }

    @Test
    void detect_null_returns_bulk() throws Exception {
        assertEquals(UldType.BULK, invokeDetect(null));
    }
}
