package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionResponse;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.LedgerEntry;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.domain.entities.CardDetails;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CardDetailsRepository;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.service.DepositService;
import com.groupa.digitalbackendapplication.service.TransactionService;
import com.groupa.digitalbackendapplication.utils.TierLimiterUtil;
import com.groupa.digitalbackendapplication.utils.TransactionRequeryUtil;
import com.groupa.digitalbackendapplication.utils.TransactionUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardDetailsRepository cardDetailsRepository;
    private final AccountRepository accountRepository;
    private final DepositService depositService;
    private final TierLimiterUtil tierLimiterUtil;

    @Override
    @Transactional
    public ResponseWrapper<TransactionResponse> transferFunds(@Valid TransferFundsRequest payload) {
        Account sourceAccount= accountRepository.findById(payload.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        //Does destination Account exists
        Account destinationAccount = accountRepository.findByAccountNumber(payload.destinationAccount().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));

        //Check if account balance isn't above tier maximum balance
        tierLimiterUtil.validateNotAlreadyOverTierMaxBalance(sourceAccount);
        //Check if daily transfer limit hasn't been exceeded
        tierLimiterUtil.validateDailyTransferLimit(sourceAccount, payload.amount());

        if(payload.amount().compareTo(sourceAccount.getBalance()) > 0)
            throw new BadRequestException("Insufficient funds available in account");

        Transaction transaction = TransactionUtil.buildTransactionEntity(TransactionType.TRANSFER, TransactionStatus.SUCCESSFUL,sourceAccount,
                destinationAccount, payload.amount(), payload.description().trim());

        //Deduct from sender, credit receiver
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(payload.amount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(payload.amount()));

        //Set total daily transfer
        tierLimiterUtil.recordDailyTransferTotal(sourceAccount.getAccountNumber(), payload.amount());

        //Ledger entry for debit
        LocalDateTime now = LocalDateTime.now();

        LedgerEntry debit = new LedgerEntry();
        debit.setEntryType(EntryType.DEBIT);
        debit.setStatus(LedgerEntryStatus.SETTLED);
        debit.setAccount(sourceAccount);
        debit.setAmount(payload.amount());
        debit.setSettledAt(now);

        //Ledger entry for credit
        LedgerEntry credit = new LedgerEntry();
        credit.setEntryType(EntryType.CREDIT);
        credit.setStatus(LedgerEntryStatus.SETTLED);
        credit.setAccount(destinationAccount);
        credit.setAmount(payload.amount());
        credit.setSettledAt(now);

        //Save transaction to db
        transaction.addLedger(debit);
        transaction.addLedger(credit);
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
        if(payload.depositAmount().compareTo(BigDecimal.valueOf(100)) < 0)
            throw new BadRequestException("Deposit amount cannot be less than 100");

        Account destinationAccount = accountRepository.findById(payload.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        String payloadCardNumber = payload.cardNumber().trim();
        String payloadCardName = payload.cardName().trim();

        CardDetails cardDetails = cardDetailsRepository.findByCardNumber(payloadCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid card. Please use a valid card"));

        if(!cardDetails.cardName().equalsIgnoreCase(payloadCardName) || !cardDetails.dateOfExpiry().equals(payload.dateOfExpiry()) || !cardDetails.cvc().equals(payload.cvc())){
            throw new BadRequestException("Problem occurred. Kindly reconfirm your card details and try again");
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

    @Override
    @Transactional
    public ResponseWrapper<TransactionResponse> requeryTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction does not exist"));

        if(transaction.getTransactionStatus() != TransactionStatus.PENDING)
            throw new BadRequestException("Cannot requery a transaction that is not pending");

        //SIMULATING A REQUERY, WHERE TRANSACTION CAN EITHER BE SUCCESSFUL OR DECLINED.
        transaction.setTransactionStatus(TransactionStatus.valueOf(TransactionRequeryUtil.requeryTransactionStatus()));

        TransactionStatus updatedTransactionStatus = transaction.getTransactionStatus();

        //IF UPDATED TRANSACTION STATUS IS NOW SUCCESSFUL, WE CAN ADD THIS FUNDS TO CUSTOMER'S WALLET
        if(updatedTransactionStatus == TransactionStatus.SUCCESSFUL){
            Account account = transaction.getDestinationAccount();
            account.setBalance(account.getBalance().add(transaction.getAmountTransferred()));
        }

        List<LedgerEntry> existingLedgerEntries = new ArrayList<>(transaction.getLedgerEntries());
        existingLedgerEntries.forEach(transaction::removeLedger);

        LocalDateTime updatedLedgerTime = LocalDateTime.now();

        //IT IS EITHER GOING TO BE SUCCESSFUL OR DECLINED NOW
        for(LedgerEntry ledgerEntry : existingLedgerEntries){
            if(updatedTransactionStatus == TransactionStatus.SUCCESSFUL){
                ledgerEntry.setStatus(LedgerEntryStatus.SETTLED);
                ledgerEntry.setSettledAt(updatedLedgerTime);
            }else{
                ledgerEntry.setStatus(LedgerEntryStatus.VOID);
                ledgerEntry.setVoidedAt(updatedLedgerTime);
            }

            transaction.addLedger(ledgerEntry);
        }

        transactionRepository.save(transaction);

        return ResponseWrapper.<TransactionResponse>builder()
                .message(updatedTransactionStatus == TransactionStatus.SUCCESSFUL ? "Transaction Successful" : "Transaction Failed")
                .data(buildTransactionResponse(updatedTransactionStatus))
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    private TransactionResponse buildTransactionResponse(TransactionStatus transactionStatus){
        return new TransactionResponse(transactionStatus);
    }
}
