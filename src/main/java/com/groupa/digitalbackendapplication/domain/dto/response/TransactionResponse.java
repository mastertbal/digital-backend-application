package com.groupa.digitalbackendapplication.domain.dto.response;

import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;

public record TransactionResponse(
        TransactionStatus status
) {
}
