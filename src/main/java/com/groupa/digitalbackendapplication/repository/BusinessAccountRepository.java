package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

    @Repository
    public interface BusinessAccountRepository extends JpaRepository<Business, UUID> {

        Optional<Business> findByBusinessEmail(String businessEmail);
        Optional<Business> findByCacNumber(String CacNumber);
    }