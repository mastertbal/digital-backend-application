package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import com.groupa.digitalbackendapplication.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> transferFunds(@Valid @RequestBody TransferFundsRequest payload){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transferFunds(payload));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> depositFunds(@Valid @RequestBody CardDetailsRequest payload){
        ResponseWrapper<TransactionResponse> response = transactionService.depositFunds(payload);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/requery")
    public void requery(){

    }
}
