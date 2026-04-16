package com.boojet.boot_api.services.Impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.UserRepository;
import com.boojet.boot_api.services.AccountBalanceSnapshotService;
import com.boojet.boot_api.services.AccountService;
import com.boojet.boot_api.services.BalanceService;
import com.boojet.boot_api.services.TransactionService;
import com.boojet.boot_api.testutil.TestAccounts;
import com.boojet.boot_api.testutil.TestUsers;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountRepository accountRepo;

    @Mock
    UserRepository userRepo;

    @Mock
    BalanceService balanceService;

    @Mock
    AccountBalanceSnapshotService snapshotService;

    @Mock
    TransactionService transactionService;

    @InjectMocks
    AccountServiceImpl accountService;

    private User user;
    private Account chequing;
    private Account savings;

    @BeforeEach
    void setup(){
        user = TestUsers.user1();
        chequing = TestAccounts.chequing();
        savings = TestAccounts.savings();
    }
    
}
