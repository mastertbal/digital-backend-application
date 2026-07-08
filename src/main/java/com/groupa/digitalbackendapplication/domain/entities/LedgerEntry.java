package com.groupa.digitalbackendapplication.domain.entities;

import com.groupa.digitalbackendapplication.domain.enums.EntryType;
import com.groupa.digitalbackendapplication.domain.enums.LedgerEntryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction; //Meant to link back to the transaction table that generated this entry

    @Column(name = "entry_type")
    @Enumerated(value = EnumType.STRING)
    private EntryType entryType;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private LedgerEntryStatus status;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
