package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.AccountSuspensionRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.AdminCreationRequest;
import com.groupa.digitalbackendapplication.domain.dto.request.KycRejectionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.*;
import com.groupa.digitalbackendapplication.domain.entities.*;
import com.groupa.digitalbackendapplication.domain.enums.*;
import com.groupa.digitalbackendapplication.domain.response.Response;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.notification.EmailDetails;
import com.groupa.digitalbackendapplication.notification.EmailService;
import com.groupa.digitalbackendapplication.repository.*;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.AdminService;
import com.groupa.digitalbackendapplication.service.CustomerService;
import com.groupa.digitalbackendapplication.service.TransactionService;
import com.groupa.digitalbackendapplication.utils.EncryptionUtil;
import com.groupa.digitalbackendapplication.utils.LoginSessionUtil;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final KycEntityRepository kycEntityRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final EncryptionUtil encryptionUtil;
    private final LoginSessionUtil loginSessionUtil;
    private final SecurityUtil securityUtil;
    private final CustomerService customerService;
    private final TransactionService transactionService;
    private final AuditLogRepository auditLogRepository;


    @Override
    public ResponseWrapper<AdminCreationResponse> createAdmin(AdminCreationRequest payload) {
        Role role = Role.ADMIN;

        if(validatePhoneNumber(payload.phoneNumber()))
            throw new BadRequestException("Error occurred, provide another phone");

        if(checkEmail(payload.email()))
            throw new BadRequestException("Error occurred, provide another email");

        if(validateAge(payload.dateOfBirth()))
            throw new BadRequestException("Admin can not be less than 18 years of age");

        AdminCreationResponse response = buildUser(payload.firstName(), payload.lastName(), payload.email(),
                payload.password(), payload.phoneNumber(), role, payload.gender(), payload.dateOfBirth(),
                payload.address());

        try {
            String emailSubject = "Admin account Creation";
            String emailBody = "Dear " + payload.firstName() + " " + payload.lastName().toUpperCase(Locale.ROOT) + ", your admin account has successfully been created. Your Admin ID: " + response.getAdminId();
            sendMail(payload.email(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("Welcome email failed to send to {}: {}", payload.email(), e.getMessage());
        }

        return ResponseWrapper.<AdminCreationResponse>builder()
                .data(response)
                .message("Admin creation successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    public ResponseWrapper<AdminDto> getAdminProfile() {
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        loginSessionUtil.verify(loggedInUser.getUser().getId());

        Admin admin = adminRepository.findById(loggedInUser.getUser().getId())
                .orElseThrow(()-> new ResourceNotFoundException("Admin not found"));

        AdminDto dto = buildAdminDto(admin);

        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.USER_PROFILE_FETCHED)
                        .userId(admin.getId())
                        .userEmail(admin.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());

        return ResponseWrapper.<AdminDto>builder()
                .data(dto)
                .message("Fetch admin profile successfully")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public Response<CustomerDto> getCustomerProfile(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        return customerService.getUserProfileById(account.getOwnerId());
    }

    @Override
    public ResponseWrapper<TransactionHistoryResponseDto> getTransactionById(UUID transactionId) {
        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.TRANSACTION_FETCHED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("transactions")
                        .build());
        return transactionService.getTransactionById(transactionId);
    }

    @Override
    public ResponseWrapper<Page<KycDto>> fetchAllPendingKyc(Pageable pageable) {
        Page<KycEntity> kycEntityPage = kycEntityRepository.findAllByStatus(pageable, KycStatus.PENDING);

        List<KycDto> dtos =kycEntityPage
                .map(kyc -> buildKycDto(
                        kyc.getAccountId(),
                        kyc.getCustomerId(),
                        kyc.getId(),
                        kyc.getDocumentType(),
                        kyc.getSubmittedValue(),
                        kyc.getResultingTier(),
                        kyc.getStatus(),
                        kyc.getSubmittedAt()
                ))
                .toList();

        Page<KycDto> pagedKyc = new PageImpl<>(dtos, pageable,kycEntityPage.getTotalElements());

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.PENDING_KYC_FETCHED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("kyc_entities")
                        .build());

        return ResponseWrapper.<Page<KycDto>>builder()
                .data(pagedKyc)
                .message("successful")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<KycDto> fetchPendingKycById(UUID id) {
        KycEntity kyc = kycEntityRepository.findByAccountIdAndStatus(id, KycStatus.PENDING)
                .orElseThrow(()->new ResourceNotFoundException("No pending kyc with the provided id"));

        KycDto dto = buildKycDto(kyc.getAccountId(), kyc.getCustomerId(), kyc.getId(), kyc.getDocumentType(),
                kyc.getSubmittedValue(), kyc.getResultingTier(), kyc.getStatus(), kyc.getSubmittedAt());

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.PENDING_KYC_FETCHED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("kyc_entities")
                        .build());

        return ResponseWrapper.<KycDto>builder()
                .data(dto)
                .message("fetch successfully")
                .statusCode(HttpStatus.FOUND)
                .build();
    }

    @Override
    public ResponseWrapper<KycResolveResponse> approveKyc(UUID kycId) {

        KycEntity kyc = kycEntityRepository.findById(kycId)
                .orElseThrow(()-> new ResourceNotFoundException("Kyc not found"));

        Account account = accountRepository.findById(kyc.getAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        Customer customer = customerRepository.findById(kyc.getCustomerId())
                .orElseThrow(()-> new ResourceNotFoundException("Customer ot found"));

        setKycStatus(kyc, KycStatus.APPROVED, KycStatus.APPROVED.name(), account, customer);

        KycResolveResponse response = new KycResolveResponse(KycStatus.APPROVED.name());


        try {
            String emailSubject = "KYC APPROVED";
            String emailBody = "Congratulations, your KYC has been approved. Your account upgraded to tier " +
                    kyc.getResultingTier().name().toUpperCase(Locale.ROOT);

            sendMail(customer.getEmail(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("Kyc Approval email failed to send to {}: {}", customer.getEmail(), e.getMessage());
        }

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.KYC_APPROVED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("kyc_entities")
                        .build());

        return ResponseWrapper.<KycResolveResponse>builder()
                .data(response)
                .message("Kyc has Been approved")
                .statusCode(HttpStatus.ACCEPTED)
                .build();
    }

    @Override
    public ResponseWrapper<KycResolveResponse> rejectKyc(KycRejectionRequest payload) {
        KycEntity kyc = kycEntityRepository.findById(payload.getKycId())
                .orElseThrow(()-> new ResourceNotFoundException("Kyc not found"));

        Account account = accountRepository.findById(kyc.getAccountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        Customer customer = customerRepository.findById(kyc.getCustomerId())
                .orElseThrow(()-> new ResourceNotFoundException("Customer ot found"));

        setKycStatus(kyc, KycStatus.REJECTED, payload.getReason(), account, customer);

        KycResolveResponse response = new KycResolveResponse(payload.getReason());

        try {
            String emailSubject = "KYC REJECTED";
            String emailBody = "Sorry your KYC request has been rejected due to " + payload.getReason();

            sendMail(customer.getEmail(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("Kyc rejection email failed to send to {}: {}", customer.getEmail(), e.getMessage());
        }

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.KYC_REJECTED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("kyc_entities")
                        .build());

        return ResponseWrapper.<KycResolveResponse>builder()
                .data(response)
                .message("Kyc Rejected")
                .statusCode(HttpStatus.OK)
                .build();

    }

    @Override
    public ResponseWrapper<String> suspendAccount(AccountSuspensionRequest payload) {
        Account account = accountRepository.findById(payload.accountId())
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        if(account.getAccountStatus() == AccountStatus.FROZEN)
            throw new RuntimeException("Account already frozen");

        account.setAccountStatus(AccountStatus.FROZEN);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        Customer customer = customerRepository.findById(account.getOwnerId())
                .orElseThrow(()-> new ResourceNotFoundException("Error occurred"));

        try {
            String emailSubject = "Account Suspended";
            String emailBody = "Dear " + customer.getFirstName() + " " + customer.getLastName().toUpperCase(Locale.ROOT) + " we regret to inform you that your account with account number: "
                    + account.getAccountNumber() + " has been suspended due to " + payload.suspensionReason();
            sendMail(customer.getEmail(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("Suspension email failed to send to {}: {}", customer.getEmail(), e.getMessage());
        }

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ACCOUNT_SUSPENDED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("accounts")
                        .build());

        return ResponseWrapper.<String>builder()
                .data(AccountStatus.FROZEN.name())
                .message("Account successfully frozen")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<String> reactivateAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        if(account.getAccountStatus() == AccountStatus.ACTIVE)
            throw new RuntimeException("Account already activated");

        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        Customer customer = customerRepository.findById(account.getOwnerId())
                .orElseThrow(()-> new ResourceNotFoundException("Error occurred"));

        try {
            String emailSubject = "Account Reactivated";
            String emailBody = "Dear " + customer.getFirstName() + " " + customer.getLastName().toUpperCase(Locale.ROOT) + " Your account with account number: "
                    + account.getAccountNumber() + " has been REACTIVATED";
            sendMail(customer.getEmail(), emailSubject, emailBody);
        } catch (Exception e){
            log.error("Reactivation email failed to send to {}: {}", customer.getEmail(), e.getMessage());
        }

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ACCOUNT_UNSUSPENDED)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("accounts")
                        .build());

        return ResponseWrapper.<String>builder()
                .data(AccountStatus.ACTIVE.name())
                .message("Account reactivated successfully")
                .statusCode(HttpStatus.OK)
                .build();

    }

    @Override
    public ResponseWrapper<BankOverviewDto> getOverview() {
        BankOverviewDto dto = buildOverview();

        // save audit log
        User user = securityUtil.getSecurityPrincipal().getUser();
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.BANK_OVERVIEW)
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("accounts")
                        .build());

        return ResponseWrapper.<BankOverviewDto>builder()
                .data(dto)
                .message("Fetched bank stat")
                .statusCode(HttpStatus.OK)
                .build();
    }

    @Override
    public ResponseWrapper<Page<AuditLog>> getAuditLogs(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);

        Page<AuditLog> auditLogs = auditLogRepository.findAll(pageable);

        return ResponseWrapper.<Page<AuditLog>>builder()
                .data(auditLogs)
                .message("Success")
                .statusCode(HttpStatus.OK)
                .build();
    }


    private String buildAdminId(){
        String id = "AD0001";
        Optional<Admin> lastAdmin = adminRepository.findTopByOrderByAdminIdDesc();

        if (lastAdmin.isPresent()){
            String lastAdminId = lastAdmin.get().getAdminId();

            String prefix = lastAdminId.substring(0, 2);
            int number = Integer.parseInt(lastAdminId.substring(2));

            number++;

            id = prefix + String.format("%04d", number);
        }
        return  id;
    }

    private AdminCreationResponse buildUser(String firstName, String lastName, String email, String password,
                                            String phoneNumber, Role role, Gender gender, LocalDate dateOfBirth, String address){
        String adminId = buildAdminId();
        Admin admin = Admin.builder().firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber)
                .role(role)
                .gender(gender)
                .dateOfBirth(dateOfBirth)
                .adminId(adminId)
                .address(address)
                .build();
        Admin savedAdmin = adminRepository.save(admin);
        // save audit log
        auditLogRepository.save(
                AuditLog.builder()
                        .actionType(ActionType.ADMIN_REGISTRATION)
                        .userId(savedAdmin.getId())
                        .userEmail(savedAdmin.getEmail())
                        .timeOfCreation(LocalDateTime.now())
                        .entityType("admin")
                        .build());
        return new AdminCreationResponse(savedAdmin.getFirstName(), adminId);
    }

    private AdminDto buildAdminDto(Admin admin){
        return AdminDto.builder()
                .adminId(admin.getAdminId())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(admin.getEmail())
                .phoneNumber(admin.getPhoneNumber())
                .gender(admin.getGender())
                .dateOfBirth(admin.getDateOfBirth())
                .role(admin.getRole())
                .address(admin.getAddress())
                .build();
    }

    private boolean validatePhoneNumber(String phoneNumber){
        Optional<Admin> adminOptional = adminRepository.findAdminByPhoneNumber(phoneNumber);
        return adminOptional.isPresent();
    }

    private boolean validateAge(LocalDate dateOfBirth){
        return Period.between(dateOfBirth, LocalDate.now()).getYears() < 18;
    }
    private boolean checkEmail(String email){
        Optional<Admin> adminOptional = adminRepository.findAdminByEmailAndRole(email, Role.ADMIN);
        return adminOptional.isPresent();
    }

    private KycDto buildKycDto(UUID accountId, UUID customerId, UUID kycId, KycDocumentType documentType,
                               String submittedValue, AccountTier resultingTier,KycStatus status,LocalDateTime submittedAt){
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(()-> new ResourceNotFoundException("customer not found"));
        String customerName = customer.getFirstName() + " " + customer.getLastName();

        return KycDto.builder()
                .accountId(accountId)
                .customerName(customerName)
                .kycId(kycId)
                .documentType(documentType)
                .submittedValue(submittedValue)
                .resultingTier(resultingTier)
                .status(status)
                .submittedAt(submittedAt)
                .build();
    }

    private void sendMail(String email, String emailSubject, String emailBody){

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(email)
                .subject(emailSubject)
                .messageBody(emailBody)
                .build();
        emailService.sendEmail(emailDetails);
    }

    private void setKycStatus(KycEntity kyc, KycStatus status, String resolutionReason, Account account, Customer customer){
        kyc.setStatus(status);
        kyc.setResolvedAt(LocalDateTime.now());
        kyc.setRejectionReason(resolutionReason);

        kycEntityRepository.save(kyc);

        if(status == KycStatus.APPROVED){
            buildAccount(account, kyc.getResultingTier());
            buildCustomer(customer, kyc.getDocumentType(), kyc.getSubmittedValue());
        }

    }
    private void buildAccount(Account account, AccountTier resultingTier){
        account.setAccountTier(resultingTier);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

    }

    private void buildCustomer(Customer customer, KycDocumentType documentType, String submittedValue){
        if(documentType.equals(KycDocumentType.NIN)){
            customer.setNin(encryptionUtil.encrypt(submittedValue));
            customer.setUpdatedAt(LocalDateTime.now());
        }
        else if (documentType.equals(KycDocumentType.BVN)){
            customer.setBvn(encryptionUtil.encrypt(submittedValue));
            customer.setUpdatedAt(LocalDateTime.now());
        }
        customerRepository.save(customer);
    }

    private long totalTierAccount(AccountTier tier){
        return accountRepository.countAccountByAccountTier(tier);
    }

    private long totalAccountByStatus(AccountStatus status){
        return accountRepository.countByAccountStatus(status);
    }

    private BankOverviewDto buildOverview(){
        long totalAccount = accountRepository.count();

        return BankOverviewDto.builder()
                .totalAccount(totalAccount)
                .totalActiveAccount(totalAccountByStatus(AccountStatus.ACTIVE))
                .totalDormantAccount(totalAccountByStatus(AccountStatus.DORMANT))
                .totalSuspendedAccount(totalAccountByStatus(AccountStatus.FROZEN))
                .totalTier1Account(totalTierAccount(AccountTier.TIER_1))
                .totalTier2Account(totalTierAccount(AccountTier.TIER_2))
                .totalTier3Account(totalTierAccount(AccountTier.TIER_3))
                .build();

    }

}
