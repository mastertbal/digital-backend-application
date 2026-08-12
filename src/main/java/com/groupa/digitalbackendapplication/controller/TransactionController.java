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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
@PreAuthorize("hasAuthority('CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> transferFunds(@Valid @RequestBody TransferFundsRequest payload){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transferFunds(payload));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> depositFunds(@Valid @RequestBody CardDetailsRequest payload){
        System.out.println("Depositing funds");
        ResponseWrapper<TransactionResponse> response = transactionService.depositFunds(payload);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/requery/{transaction-id}")
    public ResponseEntity<ResponseWrapper<TransactionResponse>> requery(@PathVariable("transaction-id") UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.requeryTransaction(id));
    }
}
