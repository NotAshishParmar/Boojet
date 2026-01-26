package com.boojet.boot_api.exceptions;

/**
 * Thrown when a requested transaction resource cannot be found
 */
public class TransactionNotFoundException extends NotFoundException{

    /**
     * @param id the missing transaction id
     */
    public TransactionNotFoundException(Long id){
        super("Transaction " + String.valueOf(id) + " not found!");
    }

    public TransactionNotFoundException(String message){
        super(message);
    }
}
