package com.groupa.digitalbackendapplication.utils;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TransactionUtil {

    public static Transaction buildTransactionEntity(TransactionType transactionType, TransactionStatus transactionStatus,
                                               Account sourceAccount, Account destinationAccount, BigDecimal amount,
                                               String description){
        return Transaction.builder()
                .transactionType(transactionType)
                .transactionStatus(transactionStatus)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amountTransferred(amount)
                .description(description)
                .createdAt(LocalDateTime.now())
                .ledgerEntries(new ArrayList<>())
                .build();
    }
}
