package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;

public interface AuthService {
    Response<LoginResponse> loginUser(LoginRequest loginRequest);
}
