package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public AuthUser getSecurityPrincipal(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new BadRequestException("User profile not available");

        return (AuthUser) authentication.getPrincipal();
    }
}
