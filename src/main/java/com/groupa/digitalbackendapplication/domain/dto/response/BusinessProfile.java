package com.groupa.digitalbackendapplication.domain.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BusinessProfile(
        String businessName,
        String businessAddress,
        String cacNumber,
        String password,
        String businessEmail,
        String accountNumber) {
}
