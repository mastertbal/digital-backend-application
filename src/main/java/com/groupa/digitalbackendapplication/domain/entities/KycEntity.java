package com.groupa.digitalbackendapplication.domain.entities;

import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.KycDocumentType;
import com.groupa.digitalbackendapplication.domain.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc_entities")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class KycEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "account_id")
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 25, nullable = false)
    private KycDocumentType documentType;

    @Column(name = "submitted_value", length = 25)
    private String submittedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 25, nullable = false)
    private KycStatus status;

    @Column(name = "rejection_reason", length = 100)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "upgraded_to", length = 25)
    private AccountTier resultingTier;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "resolved_at", nullable = false)
    private LocalDateTime resolvedAt;

}
