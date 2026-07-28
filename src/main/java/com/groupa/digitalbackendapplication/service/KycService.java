package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.KycSubmissionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.KycSubmissionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;

public interface KycService {
    ResponseWrapper<KycSubmissionResponse> submitKyc(KycSubmissionRequest payload);
}
