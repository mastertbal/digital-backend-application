package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionHistoryResponseDto;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionStatusResponse;
import com.groupa.digitalbackendapplication.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction")
@PreAuthorize("hasAuthority('CUSTOMER')")
@Tag(name = "Transaction Controller", description = "APIs for handling transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", method = "POST")
    public ResponseEntity<ResponseWrapper<TransactionStatusResponse>> transferFunds(@Valid @RequestBody TransferFundsRequest payload){
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transferFunds(payload));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit Funds", method = "POST")
    public ResponseEntity<ResponseWrapper<TransactionStatusResponse>> depositFunds(@Valid @RequestBody CardDetailsRequest payload){
        SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        .forEach(a -> System.out.println(a.getAuthority()));
        System.out.println("Depositing funds");
        ResponseWrapper<TransactionStatusResponse> response = transactionService.depositFunds(payload);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/requery/{transaction-id}")
    @Operation(summary = "Requery Pending Transactions", method = "PUT")
    public ResponseEntity<ResponseWrapper<TransactionStatusResponse>> requery(@PathVariable("transaction-id") UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(transactionService.requeryTransaction(id));
    }

    @GetMapping("/transaction-history")
    @Operation(summary = "Get Transaction history of customer", method = "GET")
    public ResponseEntity<ResponseWrapper<List<TransactionHistoryResponseDto>>> getAllTransactionHistory(){
        return ResponseEntity.ok(transactionService.getAllTransactionHistory());
    }
}
