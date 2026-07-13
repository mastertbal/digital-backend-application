package com.groupa.digitalbackendapplication.domain.entities;

import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;

import java.time.YearMonth;

public record CardDetails(
        String cardNumber,
        String cardName,
        YearMonth dateOfExpiry,
        Integer cvc,
        TransactionStatus transactionStatus
) {
}
