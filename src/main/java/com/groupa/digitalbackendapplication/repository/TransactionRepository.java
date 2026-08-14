package com.groupa.digitalbackendapplication.repository;

import com.groupa.digitalbackendapplication.domain.entities.Account;
import com.groupa.digitalbackendapplication.domain.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("select t from Transaction t where t.destinationAccount = :account")
    List<Transaction> findAllByDestinationAccount(Account account);
}
