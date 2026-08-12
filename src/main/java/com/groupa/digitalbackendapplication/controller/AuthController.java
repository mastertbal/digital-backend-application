package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.service.AuthService;
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

    @PostMapping(path = "/login")
    public ResponseEntity<Response<LoginResponse>> loginUser(
            @Valid @RequestBody LoginRequest loginRequest
    ){
        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<Response<LogoutResponse>> logoutUser(){
        return ResponseEntity.ok(authService.logout());
    }

    @PostMapping("/new-access-token")
    public ResponseEntity<Response<LoginResponse>> getNewAccessToken(HttpServletRequest request, HttpServletResponse response){
        return ResponseEntity.ok( authService.getNewAccessToken(request, response) );
    }
}
