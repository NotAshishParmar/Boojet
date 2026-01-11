package com.boojet.boot_api.exceptions;

public abstract class NotFoundException extends RuntimeException{

    public NotFoundException(String message){
        super(message);
    }
}
