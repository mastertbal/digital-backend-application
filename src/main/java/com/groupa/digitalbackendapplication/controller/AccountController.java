package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.ChangePasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AccountCreatedResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.CustomerDto;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final CustomerService customerService;

    @PostMapping("/create-personal-account")
    public ResponseWrapper<AccountCreatedResponse> createPersonalAccount(@RequestBody @Valid CustomerRegistrationRequest payload){
        return customerService.createPersonalAccount(payload);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(path = "/user-profile")
    public Response<CustomerDto> getUserProfile() {
        return customerService.getUserProfile();
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/password-reset")
    public ResponseWrapper<String> changePassword(@Valid ChangePasswordRequest payload){
        return customerService.changePassword(payload);
    }
}
