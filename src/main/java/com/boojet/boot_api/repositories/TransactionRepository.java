package com.boojet.boot_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boojet.boot_api.Money;
import com.boojet.boot_api.domain.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
}
