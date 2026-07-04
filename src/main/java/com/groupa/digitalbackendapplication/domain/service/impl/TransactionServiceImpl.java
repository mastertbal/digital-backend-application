package com.groupa.digitalbackendapplication.domain.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.LedgerEntry;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.EntryType;
import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;
import com.groupa.digitalbackendapplication.domain.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.domain.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.domain.repository.AccountRepository;
import com.groupa.digitalbackendapplication.domain.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.domain.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Validated
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public ResponseWrapper<TransactionResponse> transferFunds(@Valid TransferFundsRequest payload) {
        Account sourceAccount= accountRepository.findById(payload.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        //Does destination Account exists
        Account destinationAccount = accountRepository.findByAccountNumber(payload.destinationAccount().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));


        if(payload.amount().compareTo(sourceAccount.getBalance()) > 0)
            throw new BadRequestException("Insufficient funds available in account");

        Transaction transaction = buildTransactionEntity(TransactionType.TRANSFER, TransactionStatus.SUCCESSFUL,sourceAccount,
                destinationAccount, payload.amount(), payload.description().trim());

        //Deduct from sender, credit receiver
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(payload.amount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(payload.amount()));

        //Ledger entry for debit
        LedgerEntry debit = new LedgerEntry();
        debit.setEntryType(EntryType.DEBIT);
        debit.setAccountId(sourceAccount.getId());
        debit.setTransaction(transaction);
        debit.setAmount(payload.amount());
        debit.setCreatedAt(LocalDateTime.now());

        //Ledger entry for credit
        LedgerEntry credit = new LedgerEntry();
        credit.setEntryType(EntryType.CREDIT);
        credit.setAccountId(destinationAccount.getId());
        credit.setTransaction(transaction);
        credit.setAmount(payload.amount());
        credit.setCreatedAt(LocalDateTime.now());

        //Save transaction to db
        transaction.getLedgerEntries().add(debit);
        transaction.getLedgerEntries().add(credit);
        transactionRepository.save(transaction);

        return ResponseWrapper.<TransactionResponse>builder()
                .data(buildTransactionResponse())
                .message("Transaction successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    private Transaction buildTransactionEntity(TransactionType transactionType, TransactionStatus transactionStatus,
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

    private TransactionResponse buildTransactionResponse(){
        return  new TransactionResponse(TransactionStatus.SUCCESSFUL);
    }
}
