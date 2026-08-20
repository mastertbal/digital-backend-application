package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.response.CustomerDto;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.ForgetPasswordRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.AdminCreationResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.Admin;
import com.groupa.digitalbackendapplication.domain.entities.AuditLog;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.enums.AccountStatus;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.ActionType;
import com.groupa.digitalbackendapplication.domain.request.LoginRequest;
import com.groupa.digitalbackendapplication.domain.response.LoginResponse;
import com.groupa.digitalbackendapplication.domain.response.LogoutResponse;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.AdminRepository;
import com.groupa.digitalbackendapplication.repository.AuditLogRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.repository.UserRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.security.CustomUserDetailsService;
import com.groupa.digitalbackendapplication.security.TokenService;
import com.groupa.digitalbackendapplication.service.AdminService;
import com.groupa.digitalbackendapplication.service.AuthService;
import com.groupa.digitalbackendapplication.service.LoginSessionService;
import com.groupa.digitalbackendapplication.service.RefreshSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshSessionService refreshSessionService;
    private final LoginSessionService loginSessionService;
    private final AdminRepository adminRepository;
    private final AdminService adminService;
    private final AuditLogRepository auditLogRepository;

    @Override
    public ResponseWrapper<AdminCreationResponse> createAdmin(AdminCreationRequest payload) {
        return adminService.createAdmin(payload);
    }

    @Override
    public Response<LoginResponse> loginUser(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        AuthUser authUser = (AuthUser) customUserDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(password, authUser.getPassword())) {
            throw new BadRequestException("Password does not match");
        }

        String role = authUser.getUser().getRole().name();
        UUID userId = authUser.getUser().getId();

        String token = tokenService.generateToken(authUser.getUsername());
        String refreshToken = tokenService.generateRefreshToken(authUser.getUsername());

        String sessionId = LocalDateTime.now().toString();

        refreshSessionService.createLoginSession(sessionId, userId);

        loginSessionService.saveLoginSession(userId);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        LoginResponse loginResponse = LoginResponse.builder()
                .role(role)
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_LOGIN)
                        .userId(userId)
                        .userEmail(email)
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("customer")
                        .build());

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();
    }

    @Override
    public Response<LoginResponse> loginAdmin(LoginRequest payload, String adminId) {
        Admin admin = adminRepository.findByAdminIdAndEmail(adminId, payload.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        AuthUser authUser = (AuthUser) customUserDetailsService.loadUserByUsername(payload.getEmail());

        if (!passwordEncoder.matches(payload.getPassword(), authUser.getPassword())) {
            throw new BadRequestException("Password does not match");
        }

        String role = authUser.getUser().getRole().name();
        UUID userId = authUser.getUser().getId();

        String token = tokenService.generateToken(authUser.getUsername());
        String refreshToken = tokenService.generateRefreshToken(authUser.getUsername());

        String sessionId = LocalDateTime.now().toString();

        refreshSessionService.createLoginSession(sessionId, userId);

        loginSessionService.saveLoginSession(userId);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        LoginResponse loginResponse = LoginResponse.builder()
                .role(role)
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ADMIN_REGISTRATION)
                        .userId(userId)
                        .userEmail(admin.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successful")
                .data(loginResponse)
                .build();
    }

    public Response<LoginResponse> getNewAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // get the refresh token
        String refreshHeader = request.getHeader("Authorization");
        if (refreshHeader == null || !refreshHeader.startsWith("Bearer ")) {
            throw new ResourceNotFoundException("Authorization header missing");
        }

        // get the refresh token
        String refreshToken = refreshHeader.substring(7);

        // extract the username
        String username = tokenService.getUsernameFromToken(refreshToken);

        // check if the user exist
        AuthUser userDetails = (AuthUser) customUserDetailsService.loadUserByUsername(username);

        // get customer from repository
        Customer customer = customerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found") );

        // if logout, refresh session should be null, thus a new refresh token should be gotten by login
        String refreshSession = refreshSessionService.getUserSession(customer.getId());

        if (refreshSession == null) throw new BadRequestException("Refresh token invalid. Please login to get a new one");

        // check if refresh token is valid
        if (tokenService.validateRefreshToken(refreshToken)) {
            // invalidate previous session
            refreshSessionService.invalidateLoginSession(customer.getId());

            // generate new session id
            String sessionId = LocalDateTime.now().toString();

            // generate new access token
            String newAccessToken = tokenService.generateToken(customer.getEmail());
            // generate new refresh token
            String newRefreshToken = tokenService.generateRefreshToken(customer.getEmail());

            // save new session id into refresh session table
            refreshSessionService.createLoginSession(sessionId, customer.getId());

            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .role(customer.getRole().name())
                    .build();

            // save audit log
            auditLogRepository.save(
                    AuditLog.builder()
                            .actionType(ActionType.ANOTHER_ACCESS_TOKEN)
                            .userId(customer.getId())
                            .userEmail(customer.getEmail())
                            .timeOfCreation(LocalDateTime.now())
                            .entityType("user")
                            .build());

            System.out.println("Audit log saved");

            return Response.<LoginResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Login successful")
                    .data(loginResponse)
                    .build();
        } else {
            throw new BadRequestException("Something went wrong");
        }

    }

    @Override
    public Response<LogoutResponse> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        UUID userId = authUser.getUser().getId();

        loginSessionService.invalidateLoginSession(userId);

        refreshSessionService.invalidateLoginSession(userId);

        LogoutResponse logoutResponse = new LogoutResponse("Logout Successful");

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_LOGOUT)
                        .userId(userId)
                        .userEmail(authUser.getUser().getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("user")
                        .build());

        return Response.<LogoutResponse>builder()
                .message("Success")
                .data(logoutResponse)
                .statusCode(HttpStatus.OK.value())
                .build();
    }

    @Override
    public ResponseWrapper<String> forgetCustomerPassword(ForgetPasswordRequest payload) {
        Customer customer = customerRepository.findByEmail(payload.email())
                .orElseThrow(()-> new ResourceNotFoundException("user not found"));

        if(!payload.newPassword().equals(payload.confirmPassword()))
            throw new BadRequestException("Confirm password must be same as new password");

        customer.setPassword(passwordEncoder.encode(payload.confirmPassword()));
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.PASSWORD_CHANGED)
                        .userId(customer.getId())
                        .userEmail(customer.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("customer")
                        .build());

        return ResponseWrapper.<String>builder()
                .message("Password reset successful")
                .statusCode(HttpStatus.ACCEPTED)
                .build();
    }

    @Override
    public ResponseWrapper<String> forgetAdminPassword(ForgetPasswordRequest payload, String adminId) {
        Admin admin = adminRepository.findByAdminIdAndEmail(adminId, payload.email())
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        if(!payload.newPassword().equals(payload.confirmPassword()))
            throw new BadRequestException("Confirm password must be same as new password");

        admin.setPassword(passwordEncoder.encode(payload.confirmPassword()));
        admin.setUpdatedAt(LocalDateTime.now());
        adminRepository.save(admin);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.PASSWORD_CHANGED)
                        .userId(admin.getId())
                        .userEmail(admin.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return ResponseWrapper.<String>builder()
                .message("Password reset successful")
                .statusCode(HttpStatus.ACCEPTED)
                .build();
    }
}
