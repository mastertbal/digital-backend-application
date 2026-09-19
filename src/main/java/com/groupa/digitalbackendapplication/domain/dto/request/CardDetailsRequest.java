package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CardDetailsRequest(
        @NotBlank(message = "cardNumber is a required field")
        String cardNumber,

        @NotBlank(message = "cardName ia a required field")
        String cardName,

        @FutureOrPresent
        @NotNull(message = "dateOfExpiry is a required field")
        YearMonth dateOfExpiry,

        @NotNull(message = "cvc is a required field")
        @Digits(integer = 3, fraction = 0)
        Integer cvc,

        @NotNull(message = "amount is a required field")
        BigDecimal depositAmount,

        @NotBlank(message = "description is a required field")
        String description
) {
}
