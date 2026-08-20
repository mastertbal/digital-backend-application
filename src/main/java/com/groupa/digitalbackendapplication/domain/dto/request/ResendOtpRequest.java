package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ResendOtpRequest {
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;
}
