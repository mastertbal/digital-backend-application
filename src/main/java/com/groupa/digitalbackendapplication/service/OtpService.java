package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.ResendOtpRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.VerifyOtpRequest;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.response.Response;

import java.util.UUID;

public interface OtpService {
    Response<String> generateAndSendOtp(UUID customerId, Account account);

    Response<String> verifyOtp(VerifyOtpRequest request);

    Response<String> resendOtp(ResendOtpRequest request);
}
