package com.boojet.boot_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boojet.boot_api.domain.IncomePlan;

@Repository
public interface IncomePlanRepository extends JpaRepository<IncomePlan, Long>{
    
}
