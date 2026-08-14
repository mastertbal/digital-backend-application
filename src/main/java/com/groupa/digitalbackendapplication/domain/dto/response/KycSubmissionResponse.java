package com.groupa.digitalbackendapplication.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class KycSubmissionResponse {
    private String message;
}
