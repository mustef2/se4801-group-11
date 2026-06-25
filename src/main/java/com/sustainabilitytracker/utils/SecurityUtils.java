package com.sustainabilitytracker.utils;

import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.Role;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public static boolean hasAccessToCompany(
            User user, Long companyId) {
        if (user == null) return false;
        if (user.getRole() == Role.ADMIN ||
                user.getRole() == Role.AUDITOR) return true;
        return user.getCompany() != null
                && user.getCompany().getId().equals(companyId);
    }
}
