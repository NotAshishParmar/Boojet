package com.boojet.boot_api.exceptions;

/**
 * Thrown when an account with the given id cannot be found.
 */
public class AccountNotFoundException extends NotFoundException {
    /**
     * @param id the missing account id
     */
    public AccountNotFoundException(Long id){
        super("Account " + String.valueOf(id) + " not found!");
    }
}
