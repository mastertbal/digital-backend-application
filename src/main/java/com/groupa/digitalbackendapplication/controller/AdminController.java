package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.AccountSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.KycRejectionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;


    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/admin-profile")
    public ResponseWrapper<AdminDto> getAdminProfile(){
        return adminService.getAdminProfile();
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/fetch-pending-kyc")
    public ResponseWrapper<Page<KycDto>> fetchAllPendingKyc(
            @RequestParam(defaultValue = "0")  int     page,
            @RequestParam(defaultValue = "10") int     size)
    {
        Pageable pageable = PageRequest.of(page, size);
        return adminService.fetchAllPendingKyc(pageable);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/fetch-pending-kyc-by-id/{account-id}")
    public ResponseWrapper<KycDto> fetchPendingKycById(@PathVariable("account-id") UUID payload){
        return adminService.fetchPendingKycById(payload);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/approvekyc/{kyc-id}")
    public ResponseWrapper<KycResolveResponse> approveKyc(@PathVariable("kyc-id") UUID kycId){
        return adminService.approveKyc(kycId);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/reject-kyc")
    public ResponseWrapper<KycResolveResponse> rejectKyc(@Valid @RequestBody KycRejectionRequest payload){
        return adminService.rejectKyc(payload);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/suspend-account")
    public ResponseWrapper<String> suspendAccount(@Valid @RequestBody AccountSuspensionRequest payload){
        return adminService.suspendAccount(payload);
    }
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/reactivate-account/{account-id}")
    public ResponseWrapper<String> reactivateAccount(@PathVariable("account-id") UUID id){
        return adminService.reactivateAccount(id);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/customer-profile/{account-number}")
    public Response<CustomerDto> getCustomerProfileById(@PathVariable("account-number") String accountNumber){
        return adminService.getCustomerProfile(accountNumber);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/transaction/{transaction-id}")
    public ResponseWrapper<TransactionHistoryResponseDto> getTransactionById(@PathVariable("transaction-id") UUID id){
        return adminService.getTransactionById(id);
    }
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/stats")
    public ResponseWrapper<BankOverviewDto> getBankStat(){
        return adminService.getOverview();
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/get-all-audit-logs")
    public ResponseWrapper<Page<AuditLog>> getAllAuditLogs(
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "20") int pageSize
    ) {
        return adminService.getAuditLogs(pageNumber, pageSize);
    }
}
