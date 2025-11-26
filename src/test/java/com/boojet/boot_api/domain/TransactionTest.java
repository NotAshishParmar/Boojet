// package com.boojet.boot_api.domain;

// import java.time.LocalDate;

// import static org.assertj.core.api.Assertions.*;

// import org.junit.jupiter.api.Test;

// public class TransactionTest {
    
//     private static Money money(String value){
//         return Money.of(value);
//     }

//     @Test
//     // Test logic to create and validate a Transaction object
//     public void testCreateTransaction() {
//         // Example test case (implementation would depend on the testing framework and requirements)
//         Transaction transaction = new Transaction(
//             "Grocery Shopping",
//             money("150.75"),
//             LocalDate.of(2024, 6, 15),
//             Category.FOOD,
//             false
//         );

//         assertThat(transaction.getId()).isNull();
//         assertThat(transaction.getDescription()).isEqualTo("Grocery Shopping");
//         assertThat(transaction.getAmount()).isEqualTo(money("150.75"));
//         assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2024, 6, 15));
//         assertThat(transaction.getCategory()).isEqualTo(Category.FOOD);
//         assertThat(transaction.isIncome()).isFalse();
//     }

    
// }
