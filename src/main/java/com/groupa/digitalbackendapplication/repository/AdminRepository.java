package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.Admin;
import com.groupa.digitalbackendapplication.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findAdminByEmail(String email);
    Optional<Admin> findByAdminIdAndEmail(String adminId, String email);
    Optional<Admin> findAdminByEmailAndRole(String email, Role role);
    Optional<Admin> findAdminByPhoneNumber(String phoneNumber);
    Optional<Admin> findTopByOrderByAdminIdDesc();

}
