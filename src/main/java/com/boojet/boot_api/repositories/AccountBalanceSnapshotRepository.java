package com.boojet.boot_api.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boojet.boot_api.domain.AccountBalanceSnapshot;

@Repository
public interface AccountBalanceSnapshotRepository extends JpaRepository<AccountBalanceSnapshot, Long>{
    
    Optional<AccountBalanceSnapshot> findTopByAccount_IdOrderByAsOfDateDesc(Long accountId);

    Optional<AccountBalanceSnapshot> findTopByAccount_IdAndAsOfDateLessThanEqualOrderByAsOfDateDesc(Long accountId, LocalDate asOfDate);

    Optional<AccountBalanceSnapshot> findByAccount_IdAndAsOfDate(Long accountId, LocalDate asOfDate);

    List<AccountBalanceSnapshot> findAllByAccount_IdOrderByAsOfDateDesc(Long accountId);
}
