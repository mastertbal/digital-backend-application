package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.notification.EmailDetails;

import com.groupa.digitalbackendapplication.domain.dto.request.ResendOtpRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.VerifyOtpRequest;
import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.service.AuthService;
import com.groupa.digitalbackendapplication.service.OtpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final OtpService otpService;

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

    @PostMapping(path = "/logout")
    public ResponseEntity<Response<LogoutResponse>> logoutUser(){
        return ResponseEntity.ok(authService.logout());
    }

    @PostMapping("/new-access-token")
    public ResponseEntity<Response<LoginResponse>> getNewAccessToken(HttpServletRequest request, HttpServletResponse response){
        return ResponseEntity.ok( authService.getNewAccessToken(request, response) );
    }

    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(){
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient("codenairy@gamil.com")
                .subject("Notification Test")
                .messageBody("This is a test email from PAYEDGE")
                .build();
        emailService.sendEmail(emailDetails);
        return ResponseEntity.ok("Email sent successfully");
    }

}
