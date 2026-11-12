package com.boojet.boot_api.mappers.Impl;

import org.springframework.stereotype.Component;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.dto.account.AccountResponse;
import com.boojet.boot_api.mappers.Mapper;

@Component 
public class AccountMapper implements Mapper<Account, AccountResponse> {

    @Override
    public AccountResponse mapTo(Account account) {
        return new AccountResponse(account.getId(),
                                    account.getName(),
                                    account.getType(),
                                    account.getOpeningBalance(),
                                    account.getCreatedAt(),
                                    account.getClosedAt());
    }

    @Override
    public Account mapFrom(AccountResponse b) {
        //Account being updated/created via Patch/Create DTOs handled by the service so no need to convert account response to entity
        throw new UnsupportedOperationException("Unimplemented method 'mapFrom'");
    }
    
    
}
