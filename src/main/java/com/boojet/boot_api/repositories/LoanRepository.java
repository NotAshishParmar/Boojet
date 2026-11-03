package com.boojet.boot_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boojet.boot_api.domain.LoanDetails;

public interface LoanRepository extends JpaRepository<LoanDetails, Long>{
    
}
