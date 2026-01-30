package com.boojet.boot_api.exceptions;

public class CategoryNotFoundException extends NotFoundException{

    public CategoryNotFoundException(Long id) {
        super("Category " + String.valueOf(id) + " not found!");
    }
    
}
