package com.aircargo.service;

import com.aircargo.dto.LoadPlanningDTO;
import com.aircargo.dto.LoadPlanningUldDTO;
import com.aircargo.dto.UldAwbDTO;
import com.aircargo.entity.Flight;
import com.aircargo.entity.FlightStatus;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.entity.Uld;
import com.aircargo.entity.UldAwb;
import com.aircargo.entity.UldStatus;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.UldAwbRepository;
import com.aircargo.repository.UldRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoadPlanningServiceImpl implements LoadPlanningService {

    private final FlightRepository flightRepository;
    private final UldRepository uldRepository;
    private final UldAwbRepository uldAwbRepository;
    private final MawbRepository mawbRepository;

    public LoadPlanningServiceImpl(FlightRepository flightRepository,
                                    UldRepository uldRepository,
                                    UldAwbRepository uldAwbRepository,
                                    MawbRepository mawbRepository) {
        this.flightRepository = flightRepository;
        this.uldRepository = uldRepository;
        this.uldAwbRepository = uldAwbRepository;
        this.mawbRepository = mawbRepository;
    }

    @Override
    @Transactional
    public LoadPlanningDTO closeLoadPlan(UUID flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found: " + flightId));
        flight.setStatus(FlightStatus.DEPARTED);
        flightRepository.save(flight);

        List<Uld> ulds = uldRepository.findByFlightId(flightId);
        for (Uld uld : ulds) {
            if (uld.getStatus() != UldStatus.OFFLOADED && uld.getStatus() != UldStatus.LEFT_BEHIND) {
                uld.setStatus(UldStatus.LOADED);
                uldRepository.save(uld);
            }
            // Actualizar MAWBs a DEPARTED
            List<UldAwb> links = uldAwbRepository.findByUldId(uld.getId());
            for (UldAwb link : links) {
                if (link.getMawb() != null) {
                    Mawb mawb = link.getMawb();
                    if (mawb.getStatus() != MawbStatus.DEPARTED) {
                        mawb.setStatus(MawbStatus.DEPARTED);
                        mawbRepository.save(mawb);
                    }
                }
            }
        }

        return getByFlightId(flightId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Load plan not found after close"));
    }

    @Override
    public Optional<LoadPlanningDTO> getByFlightId(UUID flightId) {
        return flightRepository.findById(flightId)
                .map(flight -> {
                    List<Uld> ulds = uldRepository.findByFlightId(flightId);

                    List<LoadPlanningUldDTO> uldDtos = ulds.stream()
                            .map(uld -> {
                                List<UldAwbDTO> awbs = uldAwbRepository.findByUldId(uld.getId())
                                        .stream()
                                        .map(UldAwbDTO::fromEntity)
                                        .collect(Collectors.toList());

                                return LoadPlanningUldDTO.builder()
                                        .id(uld.getId())
                                        .uldNumber(uld.getUldNumber())
                                        .uldType(uld.getUldType())
                                        .position(uld.getPosition())
                                        .config(uld.getConfig())
                                        .sealNumber(uld.getSealNumber())
                                        .tareLbs(uld.getTareLbs())
                                        .grossWeightLbs(uld.getGrossWeightLbs())
                                        .netWeightLbs(uld.getNetWeightLbs())
                                        .status(uld.getStatus())
                                        .awbs(awbs)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    return LoadPlanningDTO.builder()
                            .flightId(flight.getId())
                            .flightNumber(flight.getFlightNumber())
                            .origin(flight.getOrigin())
                            .destination(flight.getDestination())
                            .aircraftReg(flight.getAircraftReg())
                            .flightDate(flight.getFlightDate())
                            .totalPositions(flight.getTotalPositions())
                            .maxPayloadKg(flight.getMaxPayloadKg())
                            .ulds(uldDtos)
                            .build();
                });
    }
}
