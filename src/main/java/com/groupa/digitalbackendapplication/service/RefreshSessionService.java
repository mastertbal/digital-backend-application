package com.groupa.digitalbackendapplication.service;


import java.util.UUID;

public interface RefreshSessionService {
    void createLoginSession(String sessionId, UUID userId);
    void invalidateLoginSession(UUID userId);
    String getUserSession(UUID userId);
}
