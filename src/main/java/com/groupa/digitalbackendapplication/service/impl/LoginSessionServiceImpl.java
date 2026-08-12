package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.entities.LoginSession;
import com.groupa.digitalbackendapplication.repository.LoginSessionRepository;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginSessionServiceImpl implements LoginSessionService {

    private final LoginSessionRepository loginSessionRepository;

    @Override
    public void saveLoginSession(UUID userId) {
        Optional<LoginSession> loginSessionOptional = loginSessionRepository.findByUserId(userId);
        if (loginSessionOptional.isPresent()) {
            LoginSession loginSession = loginSessionOptional.get();
            loginSession.setLoggedIn(Boolean.TRUE);
            loginSession.setTimeOfLogin(LocalDateTime.now());
            loginSessionRepository.save(loginSession);
        } else {
            LoginSession newLoginSession = LoginSession.builder()
                    .loggedIn(Boolean.TRUE)
                    .userId(userId)
                    .timeOfLogin(LocalDateTime.now())
                    .build();
            loginSessionRepository.save(newLoginSession);
        }
    }

    @Override
    public void invalidateLoginSession(UUID userId) {
        Optional<LoginSession> loginSessionOptional = loginSessionRepository.findByUserId(userId);
        if (loginSessionOptional.isPresent()) {
            LoginSession loginSession = loginSessionOptional.get();
            loginSession.setLoggedIn(Boolean.FALSE);
            loginSession.setTimeOfLogout(LocalDateTime.now());
            loginSessionRepository.save(loginSession);
            System.out.println("Logout invalidated");
        }
    }

    @Override
    public Optional<LoginSession> getLoginSession(UUID userId) {
        return loginSessionRepository.findByUserId(userId);
    }
}
