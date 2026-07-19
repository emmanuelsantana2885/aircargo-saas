package com.aircargo.authservice.dto;

import com.aircargo.authservice.entity.ViewPermission;
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
public class ViewPermissionDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String category;
    private Boolean isActive;

    public static ViewPermissionDTO fromEntity(ViewPermission entity) {
        if (entity == null) return null;
        return ViewPermissionDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .isActive(entity.getIsActive())
                .build();
    }
}
