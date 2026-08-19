package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class KycRejectionRequest {

    @NotNull(message = "please provide a valid id")
    private UUID kycId;
    @NotBlank(message = "reason cannot be blank")
    private String reason;
}
