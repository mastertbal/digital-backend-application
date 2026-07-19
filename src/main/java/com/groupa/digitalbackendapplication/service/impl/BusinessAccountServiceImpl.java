package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.BusinessRegistrationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.BusinessAccountCreated;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.Business;
import com.groupa.digitalbackendapplication.repository.BusinessAccountRepository;
import com.groupa.digitalbackendapplication.service.BusinessAccountService;
import com.groupa.digitalbackendapplication.utils.AccountUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessAccountServiceImpl implements BusinessAccountService {
    private final BusinessAccountRepository businessAccountRepository;

    @Override
    @Transactional
    public ResponseWrapper<BusinessAccountCreated> createBusinessAccount(BusinessRegistrationRequest payload) {

        Optional<Business> exists = businessAccountRepository.findByCacNumber(payload.getCacNumber());
        if (exists.isPresent()){
            throw new IllegalArgumentException("Business with this CAC number already exist");
        }

        AccountUtil accountUtil = new AccountUtil();
        String accountNo = accountUtil.generateAccountNumber();

        Business business = Business.builder()
                .businessName(payload.getBusinessName())
                .businessAddress(payload.getBusinessAddress())
                .cacNumber(payload.getCacNumber())
                .password(payload.getPassword())
                .businessEmail(payload.getBusinessEmail())
                .accountNumber(accountNo)
                .build();
        businessAccountRepository.save(business);

        BusinessAccountCreated businessAccountCreated = new BusinessAccountCreated(accountNo, payload.getBusinessName());

        return ResponseWrapper.<BusinessAccountCreated>builder()
                .data(businessAccountCreated)
                .message("Account Create Successfully")
                .statusCode(HttpStatus.OK)
                .build();
    }
}
