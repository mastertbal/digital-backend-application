package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.KycEntity;
import com.groupa.digitalbackendapplication.domain.enums.KycDocumentType;
import com.groupa.digitalbackendapplication.domain.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface KycEntityRepository extends JpaRepository<KycEntity, UUID> {

    boolean existsByCustomerIdAndDocumentTypeAndStatus(UUID customerId, KycDocumentType documentType, KycStatus status);
}
