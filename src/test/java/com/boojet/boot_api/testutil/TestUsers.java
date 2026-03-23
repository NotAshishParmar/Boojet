package com.boojet.boot_api.testutil;

import com.boojet.boot_api.domain.User;

public class TestUsers {
    
    private TestUsers(){
    }

    public static User user1(){
        return User.builder()
                    .id(1L)
                    .username("User 1")
                    .build();
    }

    public static User user2(){
        return User.builder()
                    .id(2L)
                    .username("User 2")
                    .build();
    }
}
