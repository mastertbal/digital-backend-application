package com.groupa.digitalbackendapplication.domain.repository;

import com.groupa.digitalbackendapplication.domain.entities.Customer;
import com.groupa.digitalbackendapplication.domain.entities.User;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhoneNumber(String phoneNumber);
}
