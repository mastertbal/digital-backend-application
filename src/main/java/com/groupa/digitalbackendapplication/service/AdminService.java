package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.AccountSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.KycRejectionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.response.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {

    ResponseWrapper<AdminCreationResponse> createAdmin(AdminCreationRequest payload);
    ResponseWrapper<AdminDto> getAdminProfile();
    Response<CustomerDto> getCustomerProfile(UUID customerId);
    ResponseWrapper<TransactionHistoryResponseDto> getTransactionById(UUID transactionId);
    ResponseWrapper<Page<KycDto>> fetchAllPendingKyc(Pageable pageable);
    ResponseWrapper<KycDto> fetchPendingKycById(UUID id);
    ResponseWrapper<KycResolveResponse> approveKyc(UUID kycId);
    ResponseWrapper<KycResolveResponse> rejectKyc(KycRejectionRequest payload);
    ResponseWrapper<String> suspendAccount(AccountSuspensionRequest payload);
    ResponseWrapper<String> reactivateAccount(UUID accountId);
    ResponseWrapper<BankOverviewDto> getOverview();
}
