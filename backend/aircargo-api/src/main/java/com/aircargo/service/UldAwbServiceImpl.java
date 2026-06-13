package com.aircargo.service;

import com.aircargo.dto.UldAwbDTO;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.Uld;
import com.aircargo.entity.UldAwb;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.UldAwbRepository;
import com.aircargo.repository.UldRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UldAwbServiceImpl implements UldAwbService {

    private final UldAwbRepository uldAwbRepository;
    private final UldRepository uldRepository;
    private final MawbRepository mawbRepository;

    public UldAwbServiceImpl(UldAwbRepository uldAwbRepository,
                              UldRepository uldRepository,
                              MawbRepository mawbRepository) {
        this.uldAwbRepository = uldAwbRepository;
        this.uldRepository = uldRepository;
        this.mawbRepository = mawbRepository;
    }

    @Override
    public List<UldAwbDTO> getAll(UUID uldId, UUID mawbId) {
        List<UldAwb> results;
        if (uldId != null) {
            results = uldAwbRepository.findByUldId(uldId);
        } else if (mawbId != null) {
            results = uldAwbRepository.findByMawbId(mawbId);
        } else {
            results = uldAwbRepository.findAll();
        }
        return results.stream()
                .map(UldAwbDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UldAwbDTO> getById(UUID id) {
        return uldAwbRepository.findById(id)
                .map(UldAwbDTO::fromEntity);
    }

    @Override
    public UldAwbDTO create(UldAwbDTO dto) {
        UldAwb entity = buildEntity(dto, new UldAwb());
        entity.setId(null);
        UldAwb saved = uldAwbRepository.save(entity);
        return UldAwbDTO.fromEntity(saved);
    }

    @Override
    public Optional<UldAwbDTO> update(UUID id, UldAwbDTO dto) {
        return uldAwbRepository.findById(id)
                .map(existing -> {
                    UldAwb updated = buildEntity(dto, existing);
                    updated.setId(existing.getId());
                    return uldAwbRepository.save(updated);
                })
                .map(UldAwbDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!uldAwbRepository.existsById(id)) return false;
        uldAwbRepository.deleteById(id);
        return true;
    }

    /**
     * Resuelve uld y mawb (si vienen) contra la BD y mapea el resto de campos del DTO.
     * Requisito de negocio: un uld_awb necesita uld_id siempre; mawb_id es opcional
     * (puede ser mawb_label para casos como EMPTY ULD, RED TAG, WWEF, etc.)
     */
    private UldAwb buildEntity(UldAwbDTO dto, UldAwb target) {
        Uld uld = uldRepository.findById(dto.getUldId())
                .orElseThrow(() -> new IllegalArgumentException("Uld not found: " + dto.getUldId()));
        target.setUld(uld);

        if (dto.getMawbId() != null) {
            Mawb mawb = mawbRepository.findById(dto.getMawbId())
                    .orElseThrow(() -> new IllegalArgumentException("Mawb not found: " + dto.getMawbId()));
            target.setMawb(mawb);
        } else {
            target.setMawb(null);
        }

        target.setMawblabel(dto.getMawbLabel());
        target.setDescription(dto.getDescription());
        target.setDestination(dto.getDestination());
        target.setPieces(dto.getPieces());
        target.setPiecesPct(dto.getPiecesPct());
        target.setTempInbound(dto.getTempInbound());
        target.setTempOutbound(dto.getTempOutbound());
        target.setHc(dto.getHc());
        target.setComments(dto.getComments());
        target.setConsumptionPallets(dto.getConsumptionPallets());
        target.setStartTime(dto.getStartTime());
        target.setEndTime(dto.getEndTime());
        target.setAvgTimePerPieceSec(dto.getAvgTimePerPieceSec());
        return target;
    }
}
