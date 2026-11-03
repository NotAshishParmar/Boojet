package com.boojet.boot_api.exceptions;

/**
 * Thrown when loan details with the given id cannot be found.
 */
public class LoanNotFoundException extends NotFoundException {
    /**
     * @param id the missing loan detail id
     */
    public LoanNotFoundException(Long id){
        super("Loan Detail " + String.valueOf(id) + " not found!");
    }
}
