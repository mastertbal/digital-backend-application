package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    Response<LoginResponse> loginUser(LoginRequest loginRequest);
    Response<LoginResponse> loginAdmin(LoginRequest payload, String adminId);
    Response<LoginResponse> getNewAccessToken(HttpServletRequest request, HttpServletResponse response);
    Response<LogoutResponse> logout();
}
