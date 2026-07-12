package com.aircargo.dto;

import com.aircargo.entity.Site;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDTO {

    private UUID id;
    private String code;
    private String name;
    private String country;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static SiteDTO fromEntity(Site entity) {
        if (entity == null) return null;
        return SiteDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .country(entity.getCountry())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Site toEntity(SiteDTO dto) {
        if (dto == null) return null;
        Site entity = new Site();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setCountry(dto.getCountry());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }
}
