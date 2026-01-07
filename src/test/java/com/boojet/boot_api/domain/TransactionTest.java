/**
 * Unit tests for the Transaction domain object.
 *
 * <p>These are pure unit tests that exercise creation and basic property accessors on
 * Transaction without starting a Spring context or involving JPA. Any external collaborators
 * such as Account should be mocked; tests must not rely on persistence or container-managed
 * services.
 *
 * <p>Tests are written using JUnit Jupiter (org.junit.jupiter) and AssertJ for fluent
 * assertions. A money(String) helper is used to construct monetary values consistently.
 *
 * <p>Primary responsibilities verified by this test class:
 * - Newly constructed Transaction objects preserve the values supplied to their constructor.
 * - Newly constructed Transaction objects have a null identifier (not yet persisted).
 *
 * @see com.boojet.boot_api.domain.Transaction
 */
 

package com.boojet.boot_api.domain;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static com.boojet.boot_api.testutil.TestDataUtil.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;




// Pure unit tests for TransactionTest with no Spring context, no JPA and Account is mocked 
public class TransactionTest {
    

    private static ObjectMapper mapper() {
    // Jackson needs JavaTimeModule for LocalDate in tests
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }


    @Test
    @DisplayName("Create Transaction and verify properties")
    // Test logic to create and validate a Transaction object
    public void testCreateTransaction() {
        
        
        Account acc = anAccount().name("Cheuquing Account").opening("1000.00").build();

        Transaction transaction = new Transaction(
            "Groceries",
            Money.of("12.34"),
            LocalDate.of(2025, 12, 1),
            Category.FOOD,
            false,
            acc
        );

        assertThat(transaction.getId()).isNull();       //IDs are only assigned by JPA when the entity is persisted \
                                                        //In a pure unit test, you are just new-ing a POJO, no EntityManager -> ID remains null
        assertThat(transaction.getDescription()).isEqualTo("Groceries");
        assertThat(transaction.getAmount().toString()).contains("12.34");
        assertThat(transaction.getDate()).isEqualTo(LocalDate.of(2025, 12, 1));
        assertThat(transaction.getCategory()).isEqualTo(Category.FOOD);
        assertThat(transaction.isIncome()).isFalse();
        assertThat(transaction.getAccount()).isEqualTo(acc);
    }

    @Test
    @DisplayName("Jackson serializes boolean as `income`, not DB column `is_income`; date as ISO; enum as name")
    void jackson_serialization_shape() throws Exception {
        Transaction tx = aTransaction()
            .income()
            .desc("Gift")
            .amt("25.00")
            .on(LocalDate.of(2025, 2, 10))
            .build();

        String json = mapper().writeValueAsString(tx);

        assertThat(json).contains("\"income\":true");          // from @JsonProperty("income")
        assertThat(json).doesNotContain("is_income");          // DB column must not leak
        assertThat(json).contains("\"date\":\"2025-02-10\"");  // ISO-8601 LocalDate
        assertThat(json).contains("\"category\":\"INCOME\"");  // enum name
        // We don't lock down Money JSON shape—just ensure it's present
        assertThat(json).contains("\"amount\"");
    }

    @Test
    @DisplayName("toString contains key info")
    void testToStringContainsKeyInfo(){
        Account acc = anAccount().name("Savings Account").opening("5000.00").build();

        Transaction transaction = new Transaction(
            "Salary",
            Money.of("3000.00"),
            LocalDate.of(2024, 6, 1),
            Category.FOOD,
            false,
            acc
        );

        String toString = transaction.toString();
        assertThat(toString).contains("Salary");
        assertThat(toString).contains("3000.00");
        assertThat(toString).contains("2024-06-01");
        assertThat(toString).contains("FOOD");
        assertThat(toString).contains("Expense");
        assertThat(toString).contains("Savings Account");
    }

    @Test
    @DisplayName("toString throws if account is null (signals invariant break)")
    void toString_throwsWhenAccountNull() {
        Transaction tx = aTransaction().acct((Account) null).build();
        assertThatThrownBy(tx::toString).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("toString prints 'Income' when income is true")
        void toString_indicatesIncome() {
        Transaction tx = aTransaction().income().desc("Refund").amt("50.00").build();
        assertThat(tx.toString()).contains("Income").doesNotContain("Expense");
    }
    
}
