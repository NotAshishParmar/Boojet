package com.boojet.boot_api.testutil;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.CategoryType;

public class TestCategories {
    
    private TestCategories() {
    }

    public static Category food() {
        return Category.builder()
                .id(9L)
                .code("FOOD")
                .name("Food")
                .type(CategoryType.EXPENSE)
                .essential(null)
                .parent(null)
                .system(true)
                .user(TestUsers.user1())
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static Category groceries() {
        return Category.builder()
                .id(10L)
                .code("FOOD_GROCERIES")
                .name("Groceries")
                .type(CategoryType.EXPENSE)
                .essential(true)
                .parent(TestCategories.food())
                .system(true)
                .user(TestUsers.user1())
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static Category diningOut() {
        return Category.builder()
                .id(13L)
                .code("FOOD_DINING")
                .name("Dining Out")
                .type(CategoryType.EXPENSE)
                .essential(false)
                .parent(TestCategories.food())
                .system(true)
                .user(TestUsers.user1())
                .active(true)
                .sortOrder(2)
                .build();
    }

    public static Category salary() {
        return Category.builder()
                .id(11L)
                .code("INCOME_SALARY")
                .name("Salary")
                .type(CategoryType.INCOME)
                .essential(null)
                .parent(null)
                .system(true)
                .user(TestUsers.user1())
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static Category transfer() {
        return Category.builder()
                .id(12L)
                .code("TRANSFER")
                .name("Transfer")
                .type(CategoryType.TRANSFER)
                .essential(null)
                .parent(null)
                .system(true)
                .user(TestUsers.user1())
                .active(true)
                .sortOrder(99)
                .build();
    }
}
