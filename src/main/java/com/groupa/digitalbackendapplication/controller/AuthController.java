package com.groupa.digitalbackendapplication.controller;

import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.service.AuthService;
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

    @PostMapping(path = "/login")
    public ResponseEntity<Response<LoginResponse>> loginUser(
            @Valid @RequestBody LoginRequest loginRequest
    ){
        return ResponseEntity.ok(authService.loginUser(loginRequest));
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
}
