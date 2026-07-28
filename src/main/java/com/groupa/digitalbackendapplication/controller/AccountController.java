package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AccountCreatedResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final CustomerService customerService;

    @PostMapping("/create-personal-account")
    public ResponseEntity<ResponseWrapper<AccountCreatedResponse>> createPersonalAccount(@RequestBody @Valid CustomerRegistrationRequest payload){
        ResponseWrapper<AccountCreatedResponse> response = customerService.createPersonalAccount(payload);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
