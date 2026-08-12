package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.DailyTransferTotal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyTransferTotalRepository extends JpaRepository<DailyTransferTotal, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DailyTransferTotal d " +
            "where d.accountNumber = :accountNumber and d.transferDate = :transferDate")
    Optional<DailyTransferTotal> findForUpdate(@Param("accountNumber") String accountNumber,
                                               @Param("transferDate") LocalDate transferDate);

}
