package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.AccountSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.KycRejectionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.response.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    ResponseWrapper<AdminCreationResponse> createAdmin(AdminCreationRequest payload);
    ResponseWrapper<AdminDto> getAdminProfile();
    Response<CustomerDto> getCustomerProfile(String accountNumber);
    ResponseWrapper<Page<CustomerDto>> getAllCustomer(Pageable pageable);
    ResponseWrapper<TransactionHistoryResponseDto> getTransactionById(UUID transactionId);
    ResponseWrapper<List<TransactionHistoryResponseDto>> getCustomerTransactions(String accountNumber);
    ResponseWrapper<Page<KycDto>> fetchAllPendingKyc(Pageable pageable);
    ResponseWrapper<KycDto> fetchPendingKycByAccountNumber(String payload);
    ResponseWrapper<KycResolveResponse> approveKyc(UUID kycId);
    ResponseWrapper<KycResolveResponse> rejectKyc(KycRejectionRequest payload);
    ResponseWrapper<String> suspendAccount(AccountSuspensionRequest payload);
    ResponseWrapper<String> reactivateAccount(String accountNumber);
    ResponseWrapper<BankOverviewDto> getOverview();
    ResponseWrapper<Page<AuditLog>> getAuditLogs(int pageNumber, int pageSize);
    ResponseWrapper<Page<TransactionHistoryResponseDto>> getAllTransactions(Pageable pageable);
}
