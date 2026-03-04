package com.boojet.boot_api.services.Impl;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.AccountBalanceSnapshot;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.repositories.AccountBalanceSnapshotRepository;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.services.BalanceService;
import com.boojet.boot_api.exceptions.BadRequestException;

@Service
@Transactional(readOnly = true)
public class BalanceServiceImpl implements BalanceService{

    private final AccountBalanceSnapshotRepository snapshotRepo;
    private final TransactionRepository transactionRepo;

    public BalanceServiceImpl(AccountBalanceSnapshotRepository snapshotRepo, TransactionRepository transactionRepo){
        this.transactionRepo = transactionRepo;
        this.snapshotRepo = snapshotRepo;
    }

    @Override
    public Optional<Money> getBalanceAsOf(Long accountId, LocalDate targetDate) {   
        if(accountId == null){
            throw new BadRequestException("accountId must not be null");
        }

        if(targetDate == null){
            throw new BadRequestException("targetDate must not be null");
        }

        //latest snapshot before the target date. serves as anchor 
        Optional<AccountBalanceSnapshot> anchorSnapshot = snapshotRepo.findTopByAccount_IdAndAsOfDateLessThanEqualOrderByAsOfDateDesc(accountId, targetDate);
        
        if(anchorSnapshot.isEmpty()){
            return Optional.empty();
        }

    
        AccountBalanceSnapshot anchor = anchorSnapshot.get();

        LocalDate snapDate = anchor.getAsOfDate();
        Money snapBalance = anchor.getBalanceAmount();

        if(targetDate.isEqual(snapDate)){
            return Optional.of(snapBalance);
        }

        if(targetDate.isAfter(snapDate)){
            //start of targetdate. Include transactions from snapDate up yo (targetDate - 1)

            LocalDate endInclusive = targetDate.minusDays(1);
            if(endInclusive.isBefore(snapDate)){
                return Optional.of(snapBalance); //no days to sum
            }

            Money delta = Money.of(transactionRepo.sumNetForAccountBetween(accountId, snapDate, endInclusive));

            return Optional.of(snapBalance.add(delta));
        }


        //target date is before snap date. Subtract transactions from targetDate up to (snapDate - 1)
        LocalDate endInclusive = snapDate.minusDays(1);
        if(endInclusive.isBefore(targetDate)){
            return Optional.of(snapBalance);
        }

        Money delta = Money.of(transactionRepo.sumNetForAccountBetween(accountId, targetDate, endInclusive));
        return Optional.of(snapBalance.subtract(delta));
    }

    @Override
    public Optional<Money> getCurrentBalance(Long accountId) {
        if(accountId == null)
            throw new BadRequestException("accountId cannot be null");
        
        //return the balance from all transactions including today (i.e. start of tomorrow)
        return getBalanceAsOf(accountId, LocalDate.now().plusDays(1));
    }
    
}
