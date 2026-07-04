package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferFundsRequest(
        @NotNull(message = "accountId cannot be empty")
        UUID accountId,

        @NotNull(message = "amount is required")
        BigDecimal amount,

        @NotNull(message = "destinationAccount is required")
        String destinationAccount,

        String description
) {
}
