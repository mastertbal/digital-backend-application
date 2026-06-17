package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "account")
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

    @Column(name = "bvn")
    private String bvn;

    @Column(name = "nin")
    private String nin;

    @Column(name = "submission_status")
    @Enumerated(value = EnumType.STRING)
    private SubmissionStatus submissionStatus;
}
