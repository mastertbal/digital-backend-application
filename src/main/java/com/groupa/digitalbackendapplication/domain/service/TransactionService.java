package com.groupa.digitalbackendapplication.domain.service;

import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import jakarta.validation.Valid;

public interface TransactionService {
    ResponseWrapper<TransactionResponse> transferFunds(@Valid TransferFundsRequest payload);
}
