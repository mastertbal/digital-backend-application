package com.groupa.digitalbackendapplication.domain.exceptions;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private String message;
    private int statusCode;
}
