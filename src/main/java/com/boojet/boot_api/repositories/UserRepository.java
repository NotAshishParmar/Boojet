package com.boojet.boot_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boojet.boot_api.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    
}
