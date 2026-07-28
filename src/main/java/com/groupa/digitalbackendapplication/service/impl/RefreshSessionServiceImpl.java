package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.entities.RefreshSession;
import com.groupa.digitalbackendapplication.repository.RefreshSessionRepository;
import com.groupa.digitalbackendapplication.service.RefreshSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshSessionServiceImpl implements RefreshSessionService {

    private final RefreshSessionRepository refreshSessionRepository;

    @Transactional
    @Override
    public void createLoginSession(String sessionId, UUID userId) {
        Optional<RefreshSession> activeSessionOptional = refreshSessionRepository.findByUserId(userId);
        if (activeSessionOptional.isEmpty()){
            RefreshSession newUserSession = RefreshSession.builder()
                    .activeSessionId(sessionId)
                    .userId(userId)
                    .build();
            refreshSessionRepository.save(newUserSession);
        }else{
            RefreshSession activeSession = activeSessionOptional.get();
            activeSession.setActiveSessionId(sessionId);
            refreshSessionRepository.save(activeSession);
        }
    }

    @Transactional
    @Override
    public void invalidateLoginSession(UUID userId) {
        Optional<RefreshSession> activeSessionOptional = refreshSessionRepository.findByUserId(userId);
        if (activeSessionOptional.isPresent()){
            refreshSessionRepository.deleteByUserId(userId);
        }
    }

    @Override
    public String getUserSession(UUID userId) {
        Optional<RefreshSession> activeSessionOptional = refreshSessionRepository.findByUserId(userId);
        if (activeSessionOptional.isEmpty()) throw  new AccessDeniedException("Something went wrong!");
        return activeSessionOptional.get().getActiveSessionId();
    }
}
