package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.response.DailyTransactionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.DailyTransactions;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.repository.DailyTransactionsRepository;
import com.groupa.digitalbackendapplication.repository.UserRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyTransactionServiceImpl {
    private final DailyTransactionsRepository dailyTransactionsRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public ResponseWrapper<DailyTransactionResponse> getDailyTransactionSummary() {
        LocalDate date = LocalDate.now();
        DailyTransactions dailyTransactions = dailyTransactionsRepository.getDailyTransactionsByDate(date)
                .orElse(null);

        return ResponseWrapper.<DailyTransactionResponse>builder()
                .data(buildDailyTransactionResponse(dailyTransactions))
                .statusCode(HttpStatus.OK)
                .message("Daily transaction fetched")
                .build();
    }


    private DailyTransactionResponse buildDailyTransactionResponse(DailyTransactions dailyTransactions){
        if(dailyTransactions != null){
            return new DailyTransactionResponse(dailyTransactions.getTotalDebit(), dailyTransactions.getTotalDebit(), dailyTransactions.getDate());
        }
        return new DailyTransactionResponse(BigDecimal.valueOf(0),BigDecimal.valueOf(0), LocalDate.now());
    }
}
