package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.AccountSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.KycRejectionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {

    ResponseWrapper<AdminCreationResponse> createAdmin(AdminCreationRequest payload);
    ResponseWrapper<AdminDto> getAdminProfile();
    ResponseWrapper<Page<KycDto>> fetchAllPendingKyc(Pageable pageable);
    ResponseWrapper<KycDto> fetchPendingKycById(UUID id);
    ResponseWrapper<KycResolveResponse> approveKyc(UUID kycId);
    ResponseWrapper<KycResolveResponse> rejectKyc(KycRejectionRequest payload);
    ResponseWrapper<String> suspendAccount(AccountSuspensionRequest payload);
}
