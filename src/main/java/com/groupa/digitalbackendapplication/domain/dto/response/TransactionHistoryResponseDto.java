package com.groupa.digitalbackendapplication.domain.dto.response;

import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionHistoryResponseDto(
        UUID transactionId,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        String sourceAccount,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {
}
