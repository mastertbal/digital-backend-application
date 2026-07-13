package com.groupa.digitalbackendapplication.controller;

import tools.jackson.databind.ObjectMapper;
import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController Integration Tests (MockMvc)")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    // ─────────────────────────────────────────────────────────────
    // POST /api/transaction/transfer
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /transfer: should return 201 CREATED on successful fund transfer")
    void transferFunds_ValidRequest_Returns201() throws Exception {
        TransferFundsRequest request = new TransferFundsRequest(
                UUID.randomUUID(), BigDecimal.valueOf(1_000), "2026222222", "Rent payment");

        ResponseWrapper<TransactionResponse> serviceResponse = ResponseWrapper.<TransactionResponse>builder()
                .data(new TransactionResponse(TransactionStatus.SUCCESSFUL))
                .message("Transaction successful")
                .statusCode(HttpStatus.CREATED)
                .build();

        when(transactionService.transferFunds(any())).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Transaction successful"))
                .andExpect(jsonPath("$.data.status").value("SUCCESSFUL"));
    }

    @Test
    @DisplayName("POST /transfer: should return 400 when accountId is missing")
    void transferFunds_MissingAccountId_Returns400() throws Exception {
        String requestJson = """
                {
                    "amount": 1000,
                    "destinationAccount": "2026222222",
                    "description": "Test"
                }
                """;

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfer: should return 400 when description is blank")
    void transferFunds_BlankDescription_Returns400() throws Exception {
        String requestJson = """
                {
                    "accountId": "%s",
                    "amount": 1000,
                    "destinationAccount": "2026222222",
                    "description": ""
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /transfer: should return 404 when source account does not exist")
    void transferFunds_SourceAccountNotFound_Returns404() throws Exception {
        TransferFundsRequest request = new TransferFundsRequest(
                UUID.randomUUID(), BigDecimal.valueOf(500), "2026222222", "Transfer");

        when(transactionService.transferFunds(any()))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found"));
    }

    @Test
    @DisplayName("POST /transfer: should return 400 when funds are insufficient")
    void transferFunds_InsufficientFunds_Returns400() throws Exception {
        TransferFundsRequest request = new TransferFundsRequest(
                UUID.randomUUID(), BigDecimal.valueOf(999_999), "2026222222", "Big transfer");

        when(transactionService.transferFunds(any()))
                .thenThrow(new BadRequestException("Insufficient funds available in account"));

        mockMvc.perform(post("/api/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds available in account"));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/transaction/deposit
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /deposit: should return 201 CREATED when card produces a SUCCESSFUL transaction")
    void depositFunds_SuccessfulCard_Returns201() throws Exception {
        CardDetailsRequest request = new CardDetailsRequest(
                UUID.randomUUID(), "7893234572819472", "SOLOMON GRUNDY",
                YearMonth.of(2029, 1), 324, BigDecimal.valueOf(5_000), "Top-up");

        ResponseWrapper<TransactionResponse> serviceResponse = ResponseWrapper.<TransactionResponse>builder()
                .data(new TransactionResponse(TransactionStatus.SUCCESSFUL))
                .message("Deposit Successful")
                .statusCode(HttpStatus.CREATED)
                .build();

        when(transactionService.depositFunds(any())).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Deposit Successful"))
                .andExpect(jsonPath("$.data.status").value("SUCCESSFUL"));
    }

    @Test
    @DisplayName("POST /deposit: should return 202 ACCEPTED when card produces a PENDING transaction")
    void depositFunds_PendingCard_Returns202() throws Exception {
        CardDetailsRequest request = new CardDetailsRequest(
                UUID.randomUUID(), "1234567893824913", "CHIOMA PRECIOUS",
                YearMonth.of(2027, 8), 372, BigDecimal.valueOf(2_000), "Top-up");

        ResponseWrapper<TransactionResponse> serviceResponse = ResponseWrapper.<TransactionResponse>builder()
                .data(new TransactionResponse(TransactionStatus.PENDING))
                .message("Deposit Successful")
                .statusCode(HttpStatus.ACCEPTED)
                .build();

        when(transactionService.depositFunds(any())).thenReturn(serviceResponse);

        mockMvc.perform(post("/api/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /deposit: should return 400 when deposit amount is below minimum")
    void depositFunds_AmountBelowMinimum_Returns400() throws Exception {
        CardDetailsRequest request = new CardDetailsRequest(
                UUID.randomUUID(), "7893234572819472", "SOLOMON GRUNDY",
                YearMonth.of(2029, 1), 324, BigDecimal.valueOf(50), "Low amount");

        when(transactionService.depositFunds(any()))
                .thenThrow(new BadRequestException("Deposit amount cannot be less than 100"));

        mockMvc.perform(post("/api/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Deposit amount cannot be less than 100"));
    }

    @Test
    @DisplayName("POST /deposit: should return 404 when card number does not exist")
    void depositFunds_CardNotFound_Returns404() throws Exception {
        CardDetailsRequest request = new CardDetailsRequest(
                UUID.randomUUID(), "0000000000000000", "GHOST PERSON",
                YearMonth.of(2029, 1), 999, BigDecimal.valueOf(1_000), "Deposit");

        when(transactionService.depositFunds(any()))
                .thenThrow(new ResourceNotFoundException("Invalid card. Please use a valid card"));

        mockMvc.perform(post("/api/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Invalid card. Please use a valid card"));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/transaction/requery/{transaction-id}
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /requery/{id}: should return 200 OK on successful requery")
    void requeryTransaction_ValidId_Returns200() throws Exception {
        UUID txId = UUID.randomUUID();

        ResponseWrapper<TransactionResponse> serviceResponse = ResponseWrapper.<TransactionResponse>builder()
                .data(new TransactionResponse(TransactionStatus.SUCCESSFUL))
                .message("Transaction Successful")
                .statusCode(HttpStatus.CREATED)
                .build();

        when(transactionService.requeryTransaction(txId)).thenReturn(serviceResponse);

        mockMvc.perform(put("/api/transaction/requery/{transaction-id}", txId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction Successful"))
                .andExpect(jsonPath("$.data.status").value("SUCCESSFUL"));
    }

    @Test
    @DisplayName("PUT /requery/{id}: should return 404 when transaction does not exist")
    void requeryTransaction_NotFound_Returns404() throws Exception {
        UUID unknownId = UUID.randomUUID();

        when(transactionService.requeryTransaction(unknownId))
                .thenThrow(new ResourceNotFoundException("Transaction does not exist"));

        mockMvc.perform(put("/api/transaction/requery/{transaction-id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transaction does not exist"));
    }

    @Test
    @DisplayName("PUT /requery/{id}: should return 400 when transaction is not PENDING")
    void requeryTransaction_NotPending_Returns400() throws Exception {
        UUID txId = UUID.randomUUID();

        when(transactionService.requeryTransaction(txId))
                .thenThrow(new BadRequestException("Cannot requery a transaction that is not pending"));

        mockMvc.perform(put("/api/transaction/requery/{transaction-id}", txId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot requery a transaction that is not pending"));
    }
}
