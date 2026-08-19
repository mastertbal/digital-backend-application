package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.response.DailyTransactionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.service.impl.DailyTransactionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/daily-transactions")
public class DailyTransactionController {

    private final DailyTransactionServiceImpl dailyTransactionService;

    @GetMapping("/daily")
    @Operation(summary = "Get Daily Transactions summary", method = "GET")
    public ResponseEntity<ResponseWrapper<DailyTransactionResponse>> getDailyTransactionsSummary(){
        return ResponseEntity.ok(dailyTransactionService.getDailyTransactionSummary());
    }
}
