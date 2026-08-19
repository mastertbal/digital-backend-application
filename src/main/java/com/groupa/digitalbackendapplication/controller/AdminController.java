package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.AccountSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.KycRejectionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminCreationResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.KycDto;
import com.groupa.digitalbackendapplication.domain.dto.response.KycResolveResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/create-Admin")
    public ResponseWrapper<AdminCreationResponse> createAdmin(@Valid @RequestBody AdminCreationRequest payload){
        return adminService.createAdmin(payload);
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
}
