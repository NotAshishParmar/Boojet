package com.boojet.boot_api.repositories;

import org.springframework.stereotype.Repository;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountType;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{

    List<Account> findByTypeIn(Collection<AccountType> types);

}
