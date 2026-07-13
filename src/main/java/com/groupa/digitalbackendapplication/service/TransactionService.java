package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface TransactionService {
    ResponseWrapper<TransactionResponse> transferFunds(@Valid TransferFundsRequest payload);

    ResponseWrapper<TransactionResponse> depositFunds(@Valid CardDetailsRequest payload);

    ResponseWrapper<TransactionResponse> requeryTransaction(UUID id);
}
