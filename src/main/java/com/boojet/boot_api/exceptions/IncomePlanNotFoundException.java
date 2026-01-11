package com.boojet.boot_api.exceptions;

public class IncomePlanNotFoundException extends NotFoundException{
    public IncomePlanNotFoundException(Long id){
        super("Income Plan " + id + " not found!!");
    }
}
