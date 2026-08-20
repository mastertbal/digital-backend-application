package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.response.DailyTransactionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.entities.DailyTransactions;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.ActionType;
import com.groupa.digitalbackendapplication.repository.AuditLogRepository;
import com.groupa.digitalbackendapplication.repository.DailyTransactionsRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyTransactionServiceImpl {
    private final DailyTransactionsRepository dailyTransactionsRepository;
    private final AuditLogRepository auditLogRepository;
    private final SecurityUtil securityUtil;

    public ResponseWrapper<DailyTransactionResponse> getDailyTransactionSummary() {
        LocalDate date = LocalDate.now();
        DailyTransactions dailyTransactions = dailyTransactionsRepository.getDailyTransactionsByDate(date)
                .orElse(null);

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.TRANSACTION_FETCHED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("transactions")
                        .build());

        return ResponseWrapper.<DailyTransactionResponse>builder()
                .data(buildDailyTransactionResponse(dailyTransactions))
                .statusCode(HttpStatus.OK)
                .message("Daily transaction fetched")
                .build();
    }


    private DailyTransactionResponse buildDailyTransactionResponse(DailyTransactions dailyTransactions){
        if(dailyTransactions != null){
            return new DailyTransactionResponse(dailyTransactions.getTotalCredit(), dailyTransactions.getTotalDebit(), dailyTransactions.getDate());
        }
        return new DailyTransactionResponse(BigDecimal.valueOf(0),BigDecimal.valueOf(0), LocalDate.now());
    }
}
