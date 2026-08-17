package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "businesses")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_name", unique = true, nullable = false, length = 100)
    private String businessName;

    @Column(name = "business_address", unique = true, nullable = false, length = 100)
    private String businessAddress;

    @Column(name = "cac_number", unique = true, nullable = false, length = 50)
    private String cacNumber;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @Column(name = "business_email", unique = true, nullable = false, length = 50)
    private String businessEmail;

    @Column(name = "account_number", unique = true, nullable = false, length = 25)
    private String accountNumber;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
