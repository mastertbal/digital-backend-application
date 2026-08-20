package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotNull;

public record ForgetPasswordRequest (
        @NotNull(message = "provide a valid email address")
        String email,
        @NotNull(message = "provide a valid password")
        String newPassword,
        @NotNull(message = "Reenter your new password")
        String confirmPassword) {
}
