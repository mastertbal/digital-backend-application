package com.groupa.digitalbackendapplication.domain.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatusCode;
@Builder
@Data
public class ResponseWrapper <T>{
    private T data;
    private String message;
    private HttpStatusCode statusCode;
}
