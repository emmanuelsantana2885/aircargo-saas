package com.aircargo.service;

import com.aircargo.dto.DuaRecordDTO;
import com.aircargo.entity.DuaRecord;
import com.aircargo.entity.Mawb;
import com.aircargo.repository.DuaRecordRepository;
import com.aircargo.repository.MawbRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DuaRecordService {

    private final DuaRecordRepository repository;
    private final MawbRepository mawbRepository;

    public DuaRecordService(DuaRecordRepository repository, MawbRepository mawbRepository) {
        this.repository = repository;
        this.mawbRepository = mawbRepository;
    }

    public List<DuaRecordDTO> getAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(DuaRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DuaRecordDTO> getByMawb(UUID mawbId) {
        return repository.findByMawbIdOrderByCreatedAtDesc(mawbId).stream()
                .map(DuaRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public DuaRecordDTO getById(UUID id) {
        return repository.findById(id)
                .map(DuaRecordDTO::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("DUA no encontrado: " + id));
    }

    @Transactional
    public DuaRecordDTO create(DuaRecordDTO dto) {
        Mawb mawb = mawbRepository.findById(dto.getMawbId())
                .orElseThrow(() -> new IllegalArgumentException("MAWB no encontrado: " + dto.getMawbId()));
        DuaRecord entity = new DuaRecord();
        entity.setMawb(mawb);
        entity.setDuaNumber(dto.getDuaNumber());
        entity.setDocumentUrl(dto.getDocumentUrl());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : com.aircargo.entity.DuaStatus.PENDING);
        entity.setDuaDate(dto.getDuaDate());
        entity.setNotes(dto.getNotes());
        entity.setCustomsBroker(dto.getCustomsBroker());
        DuaRecord saved = repository.save(entity);
        return DuaRecordDTO.fromEntity(saved);
    }

    @Transactional
    public DuaRecordDTO update(UUID id, DuaRecordDTO dto) {
        return repository.findById(id).map(entity -> {
            entity.setDuaNumber(dto.getDuaNumber());
            if (dto.getDocumentUrl() != null) entity.setDocumentUrl(dto.getDocumentUrl());
            entity.setStatus(dto.getStatus());
            entity.setDuaDate(dto.getDuaDate());
            entity.setNotes(dto.getNotes());
            entity.setCustomsBroker(dto.getCustomsBroker());
            DuaRecord saved = repository.save(entity);
            return DuaRecordDTO.fromEntity(saved);
        }).orElseThrow(() -> new IllegalArgumentException("DUA no encontrado: " + id));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("DUA no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
