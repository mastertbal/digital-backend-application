package com.groupa.digitalbackendapplication.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SavedCustomerResponse {
    private UUID customerId;
}
