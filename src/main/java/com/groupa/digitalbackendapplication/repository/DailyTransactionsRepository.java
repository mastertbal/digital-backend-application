package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.DailyTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyTransactionsRepository extends JpaRepository<DailyTransactions, Long> {

    Optional<DailyTransactions> getDailyTransactionsByDate(LocalDate date);
}
