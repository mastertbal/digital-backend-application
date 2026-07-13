package com.groupa.digitalbackendapplication.exceptions;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private String message;
    private int statusCode;
}
