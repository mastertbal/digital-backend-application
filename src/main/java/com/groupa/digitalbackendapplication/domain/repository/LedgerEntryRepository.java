package com.groupa.digitalbackendapplication.domain.repository;

import com.groupa.digitalbackendapplication.domain.entities.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
}
