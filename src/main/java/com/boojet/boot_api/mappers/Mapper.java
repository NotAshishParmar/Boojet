package com.boojet.boot_api.mappers;

public interface Mapper<A, B> {

    B mapTo (A a);

    A mapFrom (B b);
    
}
