package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.entities.LoginSession;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginSessionUtil {

    private final LoginSessionService loginSessionService;

    public void verify(UUID userId) {
        Optional<LoginSession> loginSessionOptional = loginSessionService.getLoginSession(userId);
        if (loginSessionOptional.isEmpty() || loginSessionOptional.get().getLoggedIn().equals(Boolean.FALSE)) {
            throw new BadRequestException("Please login");
        }
    }
}
