# Testing

Boojet uses JUnit 5 + Spring Boot Test. Keep tests fast for logic, and add a few end-to-end checks for safety.

## How to run

```bash
# run all tests (uses the test profile automatically)
mvn test

# run a single class
mvn -Dtest=TransactionServiceTest test
```

## Profiles

Use a **separate test profile** so prod data is never touched.

`src/test/resources/application.properties`

```properties
----------------PENDING-----------------
```


## Unit tests

### Services (mock repositories)

- Verify business rules and calculations without touching the DB.

```java
@ActiveProfiles("test") 
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock                                           // Mocked TransactionRepository because TransactionService depends on it
    TransactionRepository transactionRepo;     
    
    @InjectMocks             
    TransactionServiceImpl transactionServiceImpl;  // Inject the mocked repository into the service implementation
    
    TransactionService transactionService;          // Service under test

    @Test
    @DisplayName("Test that calculating total balance returns zero when no transactions exist")
    void testCalculateTotalBalanceReturnsZeroWhenNoTransactionsExist(){

        // mock the repository's findAll method to return an empty list
        when(transactionRepo.findAll()).thenReturn(List.of());

        // Calculate the total balance
        Money totalBalance = transactionService.calculateTotalBalance();

        //assert that the repository's findAll method was called
        verify(transactionRepo).findAll();

        //assert that the calculated total balance is zero
        assertThat(totalBalance.toString()).isEqualTo(Money.zero().toString());
    }
}
```

### Controllers (slice)

- Use `@WebMvcTest` with mocked services to check routing/validation/status codes.

```java
----------------PENDING-----------------
```

### Repository (optional slice)

- For custom JPQL, test with `@DataJpaTest`.

```java
----------------PENDING-----------------
```


## Integration tests (planned)

Goal: prove the **happy paths** work against a real Postgres dialect.



## Test data

- Both builders and factories used for test data creation.

