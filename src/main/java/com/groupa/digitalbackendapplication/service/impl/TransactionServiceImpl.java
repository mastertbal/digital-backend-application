package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.CardDetailsRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.TransferFundsRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.DailyTransactionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionHistoryResponseDto;
import com.groupa.digitalbackendapplication.domain.dto.response.TransactionStatusResponse;
import com.groupa.digitalbackendapplication.domain.entities.*;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CardDetailsRepository;
import com.groupa.digitalbackendapplication.repository.DailyTransactionsRepository;
import com.groupa.digitalbackendapplication.repository.TransactionRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.DepositService;
import com.groupa.digitalbackendapplication.service.TransactionService;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
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
import java.time.LocalDate;
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
    private final DailyTransactionsRepository dailyTransactionsRepository;
    private final DepositService depositService;
    private final TierLimiterUtil tierLimiterUtil;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public ResponseWrapper<TransactionStatusResponse> transferFunds(@Valid TransferFundsRequest payload) {
        Account sourceAccount = getAuthenticatedUser();

        //Does destination Account exists
        Account destinationAccount = accountRepository.findByAccountNumber(payload.destinationAccount().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));

        //Check if account balance isn't above tier maximum balance
        tierLimiterUtil.validateNotAlreadyOverTierMaxBalance(sourceAccount);
        //Check if daily transfer limit hasn't been exceeded
        tierLimiterUtil.validateDailyTransferLimit(sourceAccount, payload.amount());

        if (payload.amount().compareTo(sourceAccount.getBalance()) > 0)
            throw new BadRequestException("Insufficient funds available in account");

        Transaction senderTransaction = TransactionUtil.buildTransactionEntity(TransactionType.TRANSFER, TransactionStatus.SUCCESSFUL, sourceAccount,
                destinationAccount, payload.amount(), payload.description().trim());

        Transaction receiverTransaction = TransactionUtil.buildTransactionEntity(TransactionType.TRANSFER, TransactionStatus.SUCCESSFUL, destinationAccount,
                sourceAccount, payload.amount(), payload.description().trim());

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
        senderTransaction.addLedger(debit);
        senderTransaction.addLedger(credit);

        senderTransaction = transactionRepository.save(senderTransaction);
        transactionRepository.save(receiverTransaction);
        //fixme: At this point, we also save the daily debit and daily credit to db
        return ResponseWrapper.<TransactionStatusResponse>builder()
                .data(buildTransactionResponse(senderTransaction.getTransactionStatus()))
                .message("Transaction successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    @Transactional
    public ResponseWrapper<TransactionStatusResponse> depositFunds(@Valid CardDetailsRequest payload) {
        Account destinationAccount = getAuthenticatedUser();

        if (payload.depositAmount().compareTo(BigDecimal.valueOf(100)) < 0)
            throw new BadRequestException("Deposit amount cannot be less than 100");

        String payloadCardNumber = payload.cardNumber().trim();
        String payloadCardName = payload.cardName().trim();

        CardDetails cardDetails = cardDetailsRepository.findByCardNumber(payloadCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid card. Please use a valid card"));

        if (!cardDetails.cardName().equalsIgnoreCase(payloadCardName) || !cardDetails.dateOfExpiry().equals(payload.dateOfExpiry()) || !cardDetails.cvc().equals(payload.cvc())) {
            throw new BadRequestException("Problem occurred. Kindly reconfirm your card details and try again");
        }

        //Transaction will either be successful or pending - based on Card used in CardDetailsRepository
        if (cardDetails.transactionStatus() == TransactionStatus.SUCCESSFUL) {
            Transaction savedTransaction = depositService.buildSuccessfulDeposit(destinationAccount, payload);

            return ResponseWrapper.<TransactionStatusResponse>builder()
                    .data(buildTransactionResponse(savedTransaction.getTransactionStatus()))
                    .message("Deposit Successful")
                    .statusCode(HttpStatus.CREATED)
                    .build();
        } else {
            Transaction savedTransaction = depositService.buildPendingDeposit(destinationAccount, payload);

            return ResponseWrapper.<TransactionStatusResponse>builder()
                    .data(buildTransactionResponse(savedTransaction.getTransactionStatus()))
                    .message("Deposit Successful")
                    .statusCode(HttpStatus.ACCEPTED)
                    .build();
        }
    }

    @Override
    @Transactional
    public ResponseWrapper<TransactionStatusResponse> requeryTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction does not exist"));

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING)
            throw new BadRequestException("Cannot requery a transaction that is not pending");

        //SIMULATING A REQUERY, WHERE TRANSACTION CAN EITHER BE SUCCESSFUL OR DECLINED.
        transaction.setTransactionStatus(TransactionStatus.valueOf(TransactionRequeryUtil.requeryTransactionStatus()));

        TransactionStatus updatedTransactionStatus = transaction.getTransactionStatus();

        //IF UPDATED TRANSACTION STATUS IS NOW SUCCESSFUL, WE CAN ADD THIS FUNDS TO CUSTOMER'S WALLET
        if (updatedTransactionStatus == TransactionStatus.SUCCESSFUL) {
            Account account = transaction.getDestinationAccount();
            account.setBalance(account.getBalance().add(transaction.getAmountTransferred()));
        }

        List<LedgerEntry> existingLedgerEntries = new ArrayList<>(transaction.getLedgerEntries());
        existingLedgerEntries.forEach(transaction::removeLedger);

        LocalDateTime updatedLedgerTime = LocalDateTime.now();

        //IT IS EITHER GOING TO BE SUCCESSFUL OR DECLINED NOW
        for (LedgerEntry ledgerEntry : existingLedgerEntries) {
            if (updatedTransactionStatus == TransactionStatus.SUCCESSFUL) {
                ledgerEntry.setStatus(LedgerEntryStatus.SETTLED);
                ledgerEntry.setSettledAt(updatedLedgerTime);
            } else {
                ledgerEntry.setStatus(LedgerEntryStatus.VOID);
                ledgerEntry.setVoidedAt(updatedLedgerTime);
            }

            transaction.addLedger(ledgerEntry);
        }

        transactionRepository.save(transaction);

        return ResponseWrapper.<TransactionStatusResponse>builder()
                .message(updatedTransactionStatus == TransactionStatus.SUCCESSFUL ? "Transaction Successful" : "Transaction Failed")
                .data(buildTransactionResponse(updatedTransactionStatus))
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    public ResponseWrapper<List<TransactionHistoryResponseDto>> getAllTransactionHistory() {
        Account account = getAuthenticatedUser();

        List<TransactionHistoryResponseDto> transactions = transactionRepository.findAllByDestinationAccount(account)
                .stream().map(tran -> new TransactionHistoryResponseDto(tran.getId(),
                        tran.getTransactionType(), tran.getTransactionStatus(), tran.getSourceAccount() != null ? tran.getSourceAccount().getAccountNumber() : null,
                        tran.getAmountTransferred(), tran.getDescription(), tran.getCreatedAt()))
                .toList();

        System.out.println(transactions);

        return ResponseWrapper.<List<TransactionHistoryResponseDto>>builder()
                .data(transactions)
                .message("Transactions fetched")
                .statusCode(HttpStatus.OK)
                .build();
    }

    private TransactionStatusResponse buildTransactionResponse(TransactionStatus transactionStatus) {
        return new TransactionStatusResponse(transactionStatus);
    }


    private Account getAuthenticatedUser() {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        Customer customer = loggedInUser.getCustomer();

        return accountRepository.findByOwnerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}