package com.groupa.digitalbackendapplication.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AccountSuspensionRequest(
    @NotNull(message = "account id cannot be blank")
    UUID accountId,

    @NotBlank(message = "reason cannot be blank")
    String suspensionReason)
{
}
