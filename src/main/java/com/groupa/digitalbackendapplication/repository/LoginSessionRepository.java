package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.LoginSession;
import com.groupa.digitalbackendapplication.domain.entities.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginSessionRepository extends JpaRepository<LoginSession, UUID> {
    Optional<LoginSession> findByUserId(UUID userId);
}
