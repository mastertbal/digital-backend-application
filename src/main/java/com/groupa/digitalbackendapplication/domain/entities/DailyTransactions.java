package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_transactions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyTransactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_credit", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCredit;

    @Column(name = "total_debit", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDebit;

    @Column(name = "transaction_date", nullable = false, unique = true)
    private LocalDate date;

    public DailyTransactions(BigDecimal totalCredit, BigDecimal totalDebit){
        this.totalCredit = totalCredit;
        this.totalDebit = totalDebit;
    }

    @PrePersist
    private void onCreate(){
        this.date = LocalDate.now();
    }
}
