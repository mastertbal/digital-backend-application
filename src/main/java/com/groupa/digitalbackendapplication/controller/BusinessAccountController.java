package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.BusinessRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.BusinessAccountCreated;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.service.BusinessAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessAccountController {
    private final BusinessAccountService businessAccountService;

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/createaccount")
    public ResponseWrapper<BusinessAccountCreated> createBusinessAccount(@Valid @RequestBody
                                                                                BusinessRegistrationRequest payload){
        return businessAccountService.createBusinessAccount(payload);
    }

}
