package com.boojet.boot_api.exceptions;

/**
 * Thrown when a transaction with the given id cannot be found.
 */
public class TransactionNotFoundException extends NotFoundException{

    /**
     * @param id the missing transaction id
     */
    public TransactionNotFoundException(Long id){
        super("Transaction " + String.valueOf(id) + " not found!");
    }
}
