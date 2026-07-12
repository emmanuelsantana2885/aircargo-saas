package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDTO {
    private String role;
    private List<ViewAccessDTO> views;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewAccessDTO {
        private String viewCode;
        private String viewName;
        private String viewDescription;
        private String category;
        private boolean canAccess;
    }
}
