package com.groupa.digitalbackendapplication.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyTransactionResponse(
        BigDecimal totalCredit,
        BigDecimal totalDebit,
        LocalDate date
) {
}
