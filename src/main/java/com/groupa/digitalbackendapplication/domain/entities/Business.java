package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business")
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

    @Column(name = "business_name", unique = true)
    private String businessName;

    @Column(name = "business_address", unique = true)
    private String businessAddress;

    @Column(name = "cac_number", unique = true)
    private String cacNumber;

    @Column(name = "password")
    private String password;

    @Column(name = "business_email", unique = true)
    private String businessEmail;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
