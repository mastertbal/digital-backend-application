package com.groupa.digitalbackendapplication.domain.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private UUID userId;
    //private String token;
}
