package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.response.DailyTransactionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.DailyTransactions;
import com.groupa.digitalbackendapplication.repository.DailyTransactionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailyTransactionServiceImpl {
    private final DailyTransactionsRepository dailyTransactionsRepository;

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
            return new DailyTransactionResponse(dailyTransactions.getTotalCredit(), dailyTransactions.getTotalDebit(), dailyTransactions.getDate());
        }
        return new DailyTransactionResponse(BigDecimal.valueOf(0),BigDecimal.valueOf(0), LocalDate.now());
    }
}
