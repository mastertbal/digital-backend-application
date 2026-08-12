package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.entities.LoginSession;
import com.groupa.digitalbackendapplication.service.impl.LoginSessionServiceImpl;

import java.util.Optional;
import java.util.UUID;

public interface LoginSessionService {
    void saveLoginSession(UUID userId);
    void invalidateLoginSession(UUID userId);
    Optional<LoginSession> getLoginSession(UUID userId);
}
