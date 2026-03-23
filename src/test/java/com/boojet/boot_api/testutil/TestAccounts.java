package com.boojet.boot_api.testutil;

import java.time.LocalDate;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountType;
import com.boojet.boot_api.domain.Money;

public class TestAccounts {

    private TestAccounts(){
    }

    public static Account chequing() {
        return Account.builder()
                .id(1L)
                .user(TestUsers.user1())
                .name("Chequing")
                .type(AccountType.CHEQUING)
                .openingBalance(Money.of("1000.00"))
                .createdAt(LocalDate.of(2026, 1, 1))
                .closedAt(null)
                .build();
    }

    public static Account savings() {
        return Account.builder()
                .id(2L)
                .user(TestUsers.user1())
                .name("Savings")
                .type(AccountType.SAVINGS)
                .openingBalance(Money.of("5000.00"))
                .createdAt(LocalDate.of(2026, 1, 1))
                .closedAt(null)
                .build();
    }

    public static Account creditCard() {
        return Account.builder()
                .id(3L)
                .user(TestUsers.user1())
                .name("Visa")
                .type(AccountType.CREDIT_CARD)
                .openingBalance(Money.zero())
                .createdAt(LocalDate.of(2026, 1, 1))
                .closedAt(null)
                .build();
    }
    
}
