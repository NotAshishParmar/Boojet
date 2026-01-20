package com.boojet.boot_api.exceptions;

/**
 * Thrown when an income plan with the given id cannot be found.
 */
public class IncomePlanNotFoundException extends NotFoundException{

    /**
     * @param id the missing income plan id
     */
    public IncomePlanNotFoundException(Long id){
        super("Income Plan " + String.valueOf(id) + " not found!");
    }
}
