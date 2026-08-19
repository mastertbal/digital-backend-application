package com.groupa.digitalbackendapplication.service.impl;

import com.groupa.digitalbackendapplication.domain.dto.request.KycSubmissionRequest;
import com.groupa.digitalbackendapplication.domain.dto.response.KycSubmissionResponse;
import com.groupa.digitalbackendapplication.domain.dto.response.ResponseWrapper;
import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.entities.KycEntity;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.AccountTier;
import com.groupa.digitalbackendapplication.domain.enums.KycDocumentType;
import com.groupa.digitalbackendapplication.domain.enums.KycStatus;
import com.groupa.digitalbackendapplication.exceptions.BadRequestException;
import com.groupa.digitalbackendapplication.exceptions.ResourceNotFoundException;
import com.groupa.digitalbackendapplication.repository.AccountRepository;
import com.groupa.digitalbackendapplication.repository.CustomerRepository;
import com.groupa.digitalbackendapplication.repository.UserRepository;
import com.groupa.digitalbackendapplication.repository.KycEntityRepository;
import com.groupa.digitalbackendapplication.security.AuthUser;
import com.groupa.digitalbackendapplication.service.KycService;
import com.groupa.digitalbackendapplication.utils.EncryptionUtil;
import com.groupa.digitalbackendapplication.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final SecurityUtil securityUtil;
    private final KycEntityRepository kycEntityRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final EncryptionUtil encryptionUtil;

    private static final Pattern ELEVEN_DIGITS = Pattern.compile("\\d{11}");

    @Override
    public ResponseWrapper<KycSubmissionResponse> submitKyc(KycSubmissionRequest payload) {
        AccountTier resultingTier;
        AuthUser loggedInUser = securityUtil.getSecurityPrincipal();
        User user = loggedInUser.getUser();
        Account account = accountRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Optional<KycEntity> kycEntity = kycEntityRepository.findByAccountIdAndStatus(account.getId(), KycStatus.PENDING);
        if(kycEntity.isPresent())
            throw new BadRequestException("Wait until previous kyc is resolved");

        Optional<String> validationError = validate(payload.getDocumentType(), payload.getSubmittedValue());
        if (validationError.isPresent()){
            KycSubmissionResponse message = buildKycEntity(user.getId(),account.getId(),payload.getDocumentType(),
                    payload.getSubmittedValue(),KycStatus.REJECTED, validationError.get(),
                    null);

            return ResponseWrapper.<KycSubmissionResponse>builder()
                    .data(message)
                    .message(validationError.get())
                    .statusCode(HttpStatus.NOT_ACCEPTABLE)
                    .build();
        }

        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new BadCredentialsException("Customer not found"));

        AccountTier tier = recomputeEligibleTier(customer);

        if(payload.getDocumentType().equals(KycDocumentType.NIN) && tier.equals(AccountTier.TIER_1))
            resultingTier = AccountTier.TIER_2;
        else if(payload.getDocumentType().equals(KycDocumentType.BVN) && tier.equals(AccountTier.TIER_2))
            resultingTier = AccountTier.TIER_3;
        else throw new BadRequestException("Invalid document to upgrade tier");

        KycSubmissionResponse message = buildKycEntity(customer.getId(),account.getId(),payload.getDocumentType(),
                payload.getSubmittedValue(),KycStatus.PENDING, null, resultingTier);


        return ResponseWrapper.<KycSubmissionResponse>builder()
                .data(message)
                .message("KYC successfully submitted, an email will be sent once approved or rejected")
                .statusCode(HttpStatus.OK)
                .build();
    }

    private Optional<String> validate(KycDocumentType documentType, String submittedValue) {
        if (submittedValue == null || submittedValue.trim().isEmpty()) {
            return Optional.of("Submitted value must not be empty");
        }

        String trimmed = submittedValue.trim();

        return switch (documentType) {
            case NIN -> {
                if (!ELEVEN_DIGITS.matcher(trimmed).matches()) {
                    yield Optional.of("Invalid NIN format: expected 11 digits");
                }
                yield Optional.empty();
            }
            case BVN -> {
                if (!ELEVEN_DIGITS.matcher(trimmed).matches()) {
                    yield Optional.of("Invalid BVN format: expected 11 digits");
                }
                yield Optional.empty();
            }
            default -> Optional.of("Unsupported document type: " + documentType);
        };
    }

    private KycSubmissionResponse buildKycEntity(UUID customerId, UUID accountId, KycDocumentType documentType, String submittedValue, KycStatus status, String rejectionReason, AccountTier resultingTier){
        KycEntity save = KycEntity.builder()
                .customerId(customerId)
                .accountId(accountId)
                .documentType(documentType)
                .submittedValue(submittedValue)
                .status(status)
                .rejectionReason(rejectionReason)
                .resultingTier(resultingTier)
                .submittedAt(LocalDateTime.now())
                .resolvedAt(null)
                .build();
        kycEntityRepository.save(save);

        return new KycSubmissionResponse("Kyc " + status.toString());
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
        userRepository.save(customer);
    }

    private AccountTier recomputeEligibleTier(Customer customer) {
        boolean hasNin = kycEntityRepository.existsByCustomerIdAndDocumentTypeAndStatus(
                customer.getId(), KycDocumentType.NIN, KycStatus.APPROVED);
        boolean hasBvn = kycEntityRepository.existsByCustomerIdAndDocumentTypeAndStatus(
                customer.getId(), KycDocumentType.BVN, KycStatus.APPROVED);

        if (hasNin && hasBvn) {
            return AccountTier.TIER_3;
        }
        if (hasNin) {
            return AccountTier.TIER_2;
        }
        return AccountTier.TIER_1;
    }
}
