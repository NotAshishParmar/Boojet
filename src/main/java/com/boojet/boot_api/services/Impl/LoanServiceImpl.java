package com.boojet.boot_api.services.Impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.AccountType;
import com.boojet.boot_api.domain.LoanDetails;
import com.boojet.boot_api.dto.account.LoanCreateRequest;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.LoanNotFoundException;
import com.boojet.boot_api.repositories.LoanRepository;
import com.boojet.boot_api.services.LoanService;

import jakarta.transaction.Transactional;

@Service
public class LoanServiceImpl implements LoanService{

    private final LoanRepository loanRepo;

    public LoanServiceImpl (LoanRepository loanRepo){
        this.loanRepo = loanRepo;
    }

    @Override
    @Transactional 
    public LoanDetails createLoan(LoanDetails loanDetails) {
        
        LoanDetails saved = loanRepo.save(loanDetails);
        return saved;
    }

    @Override
    public LoanDetails readLoan(Long id) {
        return loanRepo.findById(id).
            orElseThrow(() -> new LoanNotFoundException(id));
    }

    @Override
    @Transactional 
    public LoanDetails updateLoan(Long id, LoanDetails loanDetails) {
        LoanDetails existing = loanRepo.findById(id).
            orElseThrow(() -> new LoanNotFoundException(id));

        existing.setAnnualInterestRate(loanDetails.getAnnualInterestRate());
        existing.setOriginalPrincipal(loanDetails.getOriginalPrincipal());
        existing.setTermMonths(loanDetails.getTermMonths());

        return loanRepo.save(existing);
    }

    @Override
    @Transactional 
    public void deleteLoan(Long id) {
        if(!loanRepo.existsById(id))
            throw new LoanNotFoundException(id);

        loanRepo.deleteById(id);
    }

    @Override 
    @Transactional 
    public void deleteLoan(Account account){
        Optional<LoanDetails> loan = loanRepo.findByLinkedAccount(account);

        if(loan.isPresent()){
            loanRepo.delete(loan.get());
        }
    }
    
}
