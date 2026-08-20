package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z]).{10,15}$",
                message = "Password should contain at least one number, one letter and must be 10 to 15 characters long")
        @NotNull(message = "provide a valid password")
        String newPassword,
        @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z]).{10,15}$",
                message = "Password should contain at least one number, one letter and must be 10 to 15 characters long")
        @NotNull(message = "Reenter your new password")
        String confirmPassword) {
}
