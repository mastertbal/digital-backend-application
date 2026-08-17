package com.groupa.digitalbackendapplication.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "daily_transfer_totals")
public class DailyTransferTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, length = 25)
    private String accountNumber;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    public DailyTransferTotal(String accountNumber, LocalDate transferDate, BigDecimal totalAmount) {
        this.accountNumber = accountNumber;
        this.transferDate = transferDate;
        this.totalAmount = totalAmount;
    }
}
