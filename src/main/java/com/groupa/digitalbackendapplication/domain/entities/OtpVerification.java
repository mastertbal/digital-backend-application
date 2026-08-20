package com.groupa.digitalbackendapplication.domain.entities;

import com.groupa.digitalbackendapplication.domain.enums.OtpChannel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private  UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private OtpChannel channel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();

        if (attemptCount == null){
            attemptCount = 0;
        }
    }

}
