package com.groupa.digitalbackendapplication.service;

import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminCreationResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    ResponseWrapper<AdminCreationResponse> createAdmin(AdminCreationRequest payload);
    Response<LoginResponse> loginUser(LoginRequest loginRequest);
    Response<LoginResponse> loginAdmin(LoginRequest payload, String adminId);
    Response<LoginResponse> getNewAccessToken(HttpServletRequest request, HttpServletResponse response);
    Response<LogoutResponse> logout();
}
