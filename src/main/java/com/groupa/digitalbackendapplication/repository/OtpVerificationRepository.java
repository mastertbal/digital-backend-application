package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
    @Query("""
            SELECT o FROM OtpVerification o 
            WHERE o.customerId = :customerId 
            AND o.verifiedAt is NULL 
            ORDER BY o.createdAt DESC""")
    Optional<OtpVerification> findLatestByCustomerId(UUID customerId);
}
