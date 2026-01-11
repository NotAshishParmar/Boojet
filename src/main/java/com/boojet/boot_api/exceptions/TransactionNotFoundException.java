package com.boojet.boot_api.exceptions;

public class TransactionNotFoundException extends NotFoundException{
    public TransactionNotFoundException(Long id){
        super("Transaction " + id + " not found");
    }
}
