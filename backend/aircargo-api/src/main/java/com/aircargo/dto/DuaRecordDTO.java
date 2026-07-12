package com.aircargo.dto;

import com.aircargo.entity.DuaRecord;
import com.aircargo.entity.DuaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuaRecordDTO {

    private UUID id;
    private UUID mawbId;
    private String awbNumber;
    private String duaNumber;
    private String documentUrl;
    private DuaStatus status;
    private LocalDate duaDate;
    private String notes;
    private String customsBroker;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static DuaRecordDTO fromEntity(DuaRecord entity) {
        if (entity == null) return null;
        return DuaRecordDTO.builder()
                .id(entity.getId())
                .mawbId(entity.getMawb() != null ? entity.getMawb().getId() : null)
                .awbNumber(entity.getMawb() != null ? entity.getMawb().getAwbNumber() : null)
                .duaNumber(entity.getDuaNumber())
                .documentUrl(entity.getDocumentUrl())
                .status(entity.getStatus())
                .duaDate(entity.getDuaDate())
                .notes(entity.getNotes())
                .customsBroker(entity.getCustomsBroker())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
