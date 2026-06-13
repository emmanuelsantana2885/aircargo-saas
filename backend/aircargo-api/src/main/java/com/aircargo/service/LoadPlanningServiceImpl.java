package com.aircargo.service;

import com.aircargo.dto.LoadPlanningDTO;
import com.aircargo.dto.LoadPlanningUldDTO;
import com.aircargo.dto.UldAwbDTO;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Uld;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.UldAwbRepository;
import com.aircargo.repository.UldRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoadPlanningServiceImpl implements LoadPlanningService {

    private final FlightRepository flightRepository;
    private final UldRepository uldRepository;
    private final UldAwbRepository uldAwbRepository;

    public LoadPlanningServiceImpl(FlightRepository flightRepository,
                                    UldRepository uldRepository,
                                    UldAwbRepository uldAwbRepository) {
        this.flightRepository = flightRepository;
        this.uldRepository = uldRepository;
        this.uldAwbRepository = uldAwbRepository;
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
