package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class VerifyOtpRequest {
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;

    @NotBlank(message = "OTP cannot be empty")
    @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit number")
    private String otp;
}
