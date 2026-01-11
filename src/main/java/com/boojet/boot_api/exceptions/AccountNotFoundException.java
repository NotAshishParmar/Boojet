package com.boojet.boot_api.exceptions;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(Long id){
        super("Account " + id + " not found!!");
    }
}
