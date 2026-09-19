package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChangeTransactionPinRequest(
        @NotNull(message = "Set a four digit pin")
        @Min(value = 1000, message = "pin must be four digits and cannot start with 0")
        @Max(value = 9999, message = "pin must be four digits")
        Integer pin,

        @NotNull(message = "Set a four digit pin")
        @Min(value = 1000, message = "pin must be four digits and cannot start with 0")
        @Max(value = 9999, message = "pin must be four digits")
        Integer confirmPin) {
}
