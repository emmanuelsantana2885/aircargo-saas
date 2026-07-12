package com.aircargo.repository;

import com.aircargo.entity.DuaRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DuaRecordRepository extends JpaRepository<DuaRecord, UUID> {
    List<DuaRecord> findByMawbIdOrderByCreatedAtDesc(UUID mawbId);
    List<DuaRecord> findAllByOrderByCreatedAtDesc();
    Optional<DuaRecord> findByMawbIdAndDuaNumber(UUID mawbId, String duaNumber);
    boolean existsByMawbId(UUID mawbId);
}
