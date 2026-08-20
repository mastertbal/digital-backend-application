package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.KycSubmissionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.KycSubmissionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.service.KycService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CUSTOMER')")
@SecurityRequirement(name = "bearerAuth")
public class KycController {

    private final KycService kycService;


    @PostMapping("/submit")
    public ResponseWrapper<KycSubmissionResponse> submitKyc(@Valid @RequestBody KycSubmissionRequest payload){
        return kycService.submitKyc(payload);
    }
}
