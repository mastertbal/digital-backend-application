package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.CustomerRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AccountCreatedResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import jakarta.validation.Valid;

public interface CustomerService {

    ResponseWrapper<AccountCreatedResponse> createPersonalAccount(CustomerRegistrationRequest payload);

    ResponseWrapper<AccountCreatedResponse> createAdminAccount(@Valid CustomerRegistrationRequest payload);
}
