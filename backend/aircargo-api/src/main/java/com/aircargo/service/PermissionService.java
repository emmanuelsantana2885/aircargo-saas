package com.aircargo.service;

import com.aircargo.auth.UserPrincipal;
import com.aircargo.entity.UserRole;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionService {

    public boolean hasViewAccess(UserPrincipal user, String viewName) {
        if (user == null) return false;
        UserRole role = UserRole.valueOf(user.role());
        return switch (role) {
            case SUPER_USER -> true;
            case ADMIN -> !"SETTINGS".equals(viewName);
            case READ_ONLY -> true;
            case WAREHOUSE_ASSISTANT -> "RECEIPTS".equals(viewName) || "DASHBOARD".equals(viewName);
            case OPERATIONS -> Set.of("DASHBOARD", "FLIGHTS", "MAWBS", "LOAD_PLANNING", "ULDS").contains(viewName);
            case TRAFFIC -> Set.of("DASHBOARD", "BOOKINGS", "MAWBS", "LOAD_PLANNING", "ULDS").contains(viewName);
            case LOAD_PLANNER -> Set.of("DASHBOARD", "FLIGHTS", "LOAD_PLANNING", "ULDS").contains(viewName);
        };
    }
}