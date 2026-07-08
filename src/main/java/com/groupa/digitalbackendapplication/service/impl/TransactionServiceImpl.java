package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.LedgerEntry;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.EntryType;
import com.groupa.digitalbackendapplication.domain.enums.LedgerEntryStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionStatus;
import com.groupa.digitalbackendapplication.domain.enums.TransactionType;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.domain.entities.CardDetails;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CardDetailsRepository;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.service.DepositService;
import com.groupa.digitalbackendapplication.service.TransactionService;
import com.groupa.digitalbackendapplication.utils.TransactionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Validated
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardDetailsRepository cardDetailsRepository;
    private final AccountRepository accountRepository;
    private final DepositService depositService;

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

        Transaction transaction = TransactionUtil.buildTransactionEntity(TransactionType.TRANSFER, TransactionStatus.SUCCESSFUL,sourceAccount,
                destinationAccount, payload.amount(), payload.description().trim());

        //Deduct from sender, credit receiver
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(payload.amount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(payload.amount()));

        //Ledger entry for debit
        LedgerEntry debit = new LedgerEntry();
        debit.setEntryType(EntryType.DEBIT);
        debit.setStatus(LedgerEntryStatus.SETTLED);
        debit.setAccount(sourceAccount);
        debit.setTransaction(transaction);
        debit.setAmount(payload.amount());
        debit.setCreatedAt(LocalDateTime.now());

        //Ledger entry for credit
        LedgerEntry credit = new LedgerEntry();
        credit.setEntryType(EntryType.CREDIT);
        credit.setStatus(LedgerEntryStatus.SETTLED);
        credit.setAccount(destinationAccount);
        credit.setTransaction(transaction);
        credit.setAmount(payload.amount());
        credit.setCreatedAt(LocalDateTime.now());

        //Save transaction to db
        transaction.getLedgerEntries().add(debit);
        transaction.getLedgerEntries().add(credit);
        transaction = transactionRepository.save(transaction);

        return ResponseWrapper.<TransactionResponse>builder()
                .data(buildTransactionResponse(transaction.getTransactionStatus()))
                .message("Transaction successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    @Transactional
    public ResponseWrapper<TransactionResponse> depositFunds(@Valid CardDetailsRequest payload) {
        Account destinationAccount = accountRepository.findById(payload.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String payloadCardNumber = payload.cardNumber().trim();
        String payloadCardName = payload.cardName().trim();

        CardDetails cardDetails = cardDetailsRepository.findByCardNumber(payloadCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid card. Please use a valid card"));

        if(!cardDetails.cardName().equalsIgnoreCase(payloadCardName) || !cardDetails.dateOfExpiry().equals(payload.dateOfExpiry()) || !cardDetails.cvc().equals(payload.cvc())){
            throw new RuntimeException("Problem occurred. Kindly reconfirm your card details and try again");
        }

        //Transaction will either be successful or pending - based on Card used in CardDetailsRepository
        if(cardDetails.transactionStatus() == TransactionStatus.SUCCESSFUL){
            Transaction savedTransaction = depositService.buildSuccessfulDeposit(destinationAccount, payload);

            return ResponseWrapper.<TransactionResponse>builder()
                    .data(buildTransactionResponse(savedTransaction.getTransactionStatus()))
                    .message("Deposit Successful")
                    .statusCode(HttpStatus.CREATED)
                    .build();
        }
        else{
            Transaction savedTransaction = depositService.buildPendingDeposit(destinationAccount, payload);

            return ResponseWrapper.<TransactionResponse>builder()
                    .data(buildTransactionResponse(savedTransaction.getTransactionStatus()))
                    .message("Deposit Successful")
                    .statusCode(HttpStatus.ACCEPTED)
                    .build();
        }
    }

    private TransactionResponse buildTransactionResponse(TransactionStatus transactionStatus){
        return  new TransactionResponse(transactionStatus);
    }
}
