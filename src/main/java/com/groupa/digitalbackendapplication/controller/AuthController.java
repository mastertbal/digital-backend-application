package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.notification.EmailDetails;

import com.groupa.digitalbackendapplication.domain.dto.request.ResendOtpRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.VerifyOtpRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.ForgetPasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminCreationResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.service.AuthService;
import com.groupa.digitalbackendapplication.service.OtpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final OtpService otpService;

    @PostMapping("/create-Admin")
    public ResponseWrapper<AdminCreationResponse> createAdmin(@Valid @RequestBody AdminCreationRequest payload){
        return authService.createAdmin(payload);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<Response<LoginResponse>> loginUser(
            @Valid @RequestBody LoginRequest loginRequest
    ){
        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }
    @PostMapping(path = "/verify-otp")
    public ResponseEntity<Response<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request){
        return ResponseEntity.ok(otpService.verifyOtp(request));
    }

    @PostMapping(path = "/resend-otp")
    public ResponseEntity<Response<String>> resendOtp(@Valid @RequestBody ResendOtpRequest request){
        return ResponseEntity.ok(otpService.resendOtp(request));
    }

    @Operation(security =@SecurityRequirement(name = "X-ADMIN_ID"))
    @PostMapping(path = "/login-admin")
    public ResponseEntity<Response<LoginResponse>> loginAdmin(@Valid @RequestBody LoginRequest payload,
                                                              @RequestHeader("X-ADMIN_ID") String adminId){
        return ResponseEntity.ok(authService.loginAdmin(payload, adminId));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<Response<LogoutResponse>> logoutUser(){
        return ResponseEntity.ok(authService.logout());
    }

    @PostMapping("/new-access-token")
    public ResponseEntity<Response<LoginResponse>> getNewAccessToken(HttpServletRequest request, HttpServletResponse response){
        return ResponseEntity.ok( authService.getNewAccessToken(request, response) );
    }

    @PatchMapping("/forget-password/customer")
    public ResponseWrapper<String> forgetCustomerPassword(@Valid @RequestBody ForgetPasswordRequest payload){
        return authService.forgetCustomerPassword(payload);
    }

    @PatchMapping("/forget-password/admin/{admin-id}")
    public ResponseWrapper<String> forgetAdminPassword(@Valid @RequestBody ForgetPasswordRequest payload, @PathVariable("admin-id") String adminId){
        return authService.forgetAdminPassword(payload, adminId);
    }
}
