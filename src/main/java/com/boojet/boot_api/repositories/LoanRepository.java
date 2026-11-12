package com.boojet.boot_api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boojet.boot_api.domain.LoanDetails;
import com.boojet.boot_api.domain.Account;


public interface LoanRepository extends JpaRepository<LoanDetails, Long>{

    public Optional<LoanDetails> findByLinkedAccount(Account linkedAccount);

}
