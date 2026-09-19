package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferFundsRequest(

        @NotNull(message = "Source account required")
        String sourceAccount,

        @NotNull(message = "amount is required")
        BigDecimal amount,

        @NotNull(message = "destinationAccount is required")
        String destinationAccount,

        @NotBlank(message = "description is a required field")
        String description,

        @NotNull(message = "Provide your transaction pin")
        Integer transactionPin
) {
}
