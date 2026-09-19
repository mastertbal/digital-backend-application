package com.groupa.digitalbackendapplication.domain.dto.response;

import com.groupa.digitalbackendapplication.domain.entities.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SavedCustomerResponse {
    private Customer customer;
}
