package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import jakarta.validation.Valid;

public interface DepositService {

    Transaction buildSuccessfulDeposit(Account account, @Valid CardDetailsRequest payload);

    Transaction buildPendingDeposit(Account destinationAccount, @Valid CardDetailsRequest payload);
}
