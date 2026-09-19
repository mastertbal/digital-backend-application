package com.groupa.digitalbackendapplication.domain.entities;

import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.PersonalAccountType;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Customer customer;

    @Column(name = "account_status", nullable = false, length = 25)
    @Enumerated(value = EnumType.STRING)
    private AccountStatus accountStatus;

    @Column(name = "account_number", unique = true, nullable = false, length = 25)
    private String accountNumber;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "account_tier", nullable = false, length = 25)
    @Enumerated(value = EnumType.STRING)
    private AccountTier accountTier;

    @Column(name = "account_type", length = 25)
    @Enumerated(value = EnumType.STRING)
    private PersonalAccountType personalAccountType;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
