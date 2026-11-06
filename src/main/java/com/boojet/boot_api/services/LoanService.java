package com.boojet.boot_api.services;

import com.boojet.boot_api.domain.LoanDetails;
import com.boojet.boot_api.dto.account.LoanCreateRequest;

public interface LoanService {
    
    LoanDetails createLoan (LoanDetails loanDetails);

    LoanDetails readLoan (Long id);

    LoanDetails updateLoan (Long id, LoanDetails loanDetails);

    void deleteLoan (Long id);
}
