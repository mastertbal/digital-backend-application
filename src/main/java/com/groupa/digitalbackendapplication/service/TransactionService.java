package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionHistoryResponseDto;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionStatusResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    ResponseWrapper<TransactionStatusResponse> transferFunds(@Valid TransferFundsRequest payload);

    ResponseWrapper<TransactionStatusResponse> depositFunds(@Valid CardDetailsRequest payload);

    ResponseWrapper<TransactionStatusResponse> requeryTransaction(UUID id);

    ResponseWrapper<List<TransactionHistoryResponseDto>> getAllTransactionHistory();

    ResponseWrapper<TransactionHistoryResponseDto> getTransactionById(UUID transactionId);
}
