package com.groupa.digitalbackendapplication.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class BusinessAccountCreated {
    private String accountNumber;
    private String businessName;
}
