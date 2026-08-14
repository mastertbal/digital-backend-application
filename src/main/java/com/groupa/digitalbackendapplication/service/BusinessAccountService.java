package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.BusinessRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.BusinessAccountCreated;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;

public interface BusinessAccountService {

    ResponseWrapper<BusinessAccountCreated> createBusinessAccount(BusinessRegistrationRequest payload);
}
