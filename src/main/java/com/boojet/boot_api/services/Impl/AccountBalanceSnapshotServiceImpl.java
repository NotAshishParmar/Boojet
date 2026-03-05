package com.boojet.boot_api.services.Impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountBalanceSnapshot;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.exceptions.AccountNotFoundException;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.repositories.AccountBalanceSnapshotRepository;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.services.AccountBalanceSnapshotService;

@Service
@Transactional
public class AccountBalanceSnapshotServiceImpl implements AccountBalanceSnapshotService {

    private final AccountRepository accountRepo;
    private final AccountBalanceSnapshotRepository snapshotRepo;

    public AccountBalanceSnapshotServiceImpl(AccountRepository accountRepo,
            AccountBalanceSnapshotRepository snapshotRepo) {
        this.accountRepo = accountRepo;
        this.snapshotRepo = snapshotRepo;
    }

    @Override
    public AccountBalanceSnapshot upsert(Long accountId, LocalDate asOfDate, Money balance) {
        if (accountId == null) {
            throw new BadRequestException("accountId must not be null");
        }

        if (balance == null) {
            throw new BadRequestException("balance must not be null");
        }

        LocalDate date = (asOfDate == null) ? LocalDate.now() : asOfDate;

        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new BadRequestException("asOfDate must not be in the future");
        }

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        AccountBalanceSnapshot snap = snapshotRepo.findByAccount_IdAndAsOfDate(accountId, date)
                .orElseGet(AccountBalanceSnapshot::new);

        snap.setAccount(account);
        snap.setAsOfDate(date);
        snap.setBalanceAmount(balance);

        return snapshotRepo.save(snap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountBalanceSnapshot> listSnapshots(Long accountId) {
        if (accountId == null) throw new BadRequestException("accountId must not be null");
        return snapshotRepo.findAllByAccount_IdOrderByAsOfDateDesc(accountId);
    }

}
