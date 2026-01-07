
/**
 * Test class for {@link TransactionService}.
 * 
 * This test class contains unit tests for the TransactionService class,
 * verifying the functionality and behavior of transaction-related operations.
 * 
 * @see TransactionService
 */

package com.boojet.boot_api.services;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.services.Impl.TransactionServiceImpl;

// Import test data utility methods (static import because of usage frequency)
import static com.boojet.boot_api.testutil.TestDataUtil.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock                                           // Mocked TransactionRepository because TransactionService depends on it
    TransactionRepository transactionRepo;     
    
    @InjectMocks             
    TransactionServiceImpl transactionServiceImpl;  // Inject the mocked repository into the service implementation
    
    TransactionService transactionService;          // Service under test

    Account accountA;
    Transaction transactionA;
    List<Transaction> transactionsList;

    @BeforeEach                                     
    void setUp(){                                   // Initialize the service with the mocked repository
        transactionService = transactionServiceImpl;

        accountA = createTestAccountA();
        transactionA = createTestTransactionA(accountA);
        transactionsList = someTransactions(accountA);
    }


    //---------------------------- Test CRUD operations (Unecessary but good practice) ----------------------------------

    @Test
    @DisplayName("Test that adding a new transaction works correctly")
    void testAddTransaction(){

        // mock the repository's save method to return the transaction when called
        when(transactionRepo.save(transactionA)).thenReturn(transactionA);

        Transaction saved = transactionService.addTransaction(transactionA);

        //assert that the repository's save method was called with the correct transaction
        verify(transactionRepo).save(transactionA);

        //assert that the returned transaction is the same as the saved one
        assertThat(saved).usingRecursiveComparison()
                         .isEqualTo(transactionA);   
    }

    @Test
    @DisplayName("Test that addTransaction throws exception when saving null transaction")
    void testAddTransactionthrowsExceptionWhenNull(){
        assertThatThrownBy(() -> transactionService.addTransaction(null))
            .isInstanceOf(Exception.class);
        
        // Verify that the repository's save method was never called
        verifyNoInteractions(transactionRepo);
    }

    @Test
    @DisplayName("Test that finding all transactions works correctly")
    void testFindAllTransactions(){

        // mock the repository's findAll method to return a list with transactionA
        when(transactionRepo.findAll()).thenReturn(transactionsList);

        List<Transaction> transactions = transactionService.findAllTransactions();

        //assert that the repository's findAll method was called
        verify(transactionRepo).findAll();

        
        //assert that the size of the returned list is correct
        assertThat(transactions).hasSize(transactionsList.size());

        //assert that the first transaction in the list is transactionA
        assertThat(transactions.get(0)).usingRecursiveComparison()
                                       .isEqualTo(transactionsList.get(0));

        //assert that the second transaction in the list is transactionB
        assertThat(transactions.get(1)).usingRecursiveComparison()
                                       .isEqualTo(transactionsList.get(1));

        //assert that the third transaction in the list is transactionC
        assertThat(transactions.get(2)).usingRecursiveComparison()
                                       .isEqualTo(transactionsList.get(2));
    }

    @Test
    @DisplayName("Test that finding all transactions returns empty list when no transactions exist")
    void testFindAllTransactionsReturnsEmptyListWhenNoneExist(){

        // mock the repository's findAll method to return an empty list
        when(transactionRepo.findAll()).thenReturn(List.of());

        List<Transaction> transactions = transactionService.findAllTransactions();

        //assert that the repository's findAll method was called
        verify(transactionRepo).findAll();

        //assert that the returned list is empty
        assertThat(transactions).isEmpty();
    }

    @Test
    @DisplayName("Test that finding a transaction by ID works correctly")
    void testFindTransactionById(){

        // mock the repository's findById method to return transactionA when called with its ID
        when(transactionRepo.findById(transactionA.getId())).thenReturn(Optional.of(transactionA));

        Optional<Transaction> foundOpt = transactionService.findTransaction(transactionA.getId());

        //assert that the repository's findById method was called with the correct ID
        verify(transactionRepo).findById(transactionA.getId());

        //assert that the returned Optional contains transactionA
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get()).usingRecursiveComparison()
                                  .isEqualTo(transactionA);
        
    }

    @Test
    @DisplayName("Test that finding a transaction by ID returns empty when not found")
    void testFindTransactionByIdReturnsEmptyWhenNotFound(){
        Long nonExistentId = 999L;

        // mock the repository's findById method to return empty when called with a non-existent ID
        when(transactionRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Transaction> foundOpt = transactionService.findTransaction(nonExistentId);

        //assert that the repository's findById method was called with the correct ID
        verify(transactionRepo).findById(nonExistentId);

        //assert that the returned Optional is empty
        assertThat(foundOpt).isNotPresent();

    }

    @Test
    @DisplayName("Test that updating a transaction works correctly")
    void testUpdateTransaction(){
        

        // mock the repository's findById method to return transactionA when called with its ID
        when(transactionRepo.findById(transactionA.getId())).thenReturn(Optional.of(transactionA));


        Transaction updatedTransaction = new Transaction(
            "Updated Description",
            transactionA.getAmount(),
            transactionA.getDate(),
            transactionA.getCategory(),
            transactionA.isIncome(),
            transactionA.getAccount()
        );

        // mock the repository's save method to return the updated transaction
        when(transactionRepo.save(any(Transaction.class))).thenReturn(updatedTransaction);

        Transaction result = transactionService.updateTransaction(transactionA.getId(), updatedTransaction);

        //assert that the repository's findById method was called with the correct ID
        verify(transactionRepo).findById(transactionA.getId());

        //assert that the repository's save method was called
        verify(transactionRepo).save(any(Transaction.class));

        //assert that the returned transaction has the updated description
        assertThat(result.getDescription()).isEqualTo("Updated Description");

        //assert that other properties remain unchanged
        assertThat(result.getAmount()).isEqualTo(transactionA.getAmount());
        assertThat(result.getDate()).isEqualTo(transactionA.getDate());
        assertThat(result.getCategory()).isEqualTo(transactionA.getCategory());
        assertThat(result.isIncome()).isEqualTo(transactionA.isIncome());
        assertThat(result.getAccount()).isEqualTo(transactionA.getAccount());
    }

    @Test
    @DisplayName("Test that updating a non-existent transaction throws exception")
    void testUpdateTransactionThrowsExceptionWhenNotFound(){
        Long nonExistentId = 999L;

        Transaction updatedTransaction = new Transaction(
            "Updated Description",
            transactionA.getAmount(),
            transactionA.getDate(),
            transactionA.getCategory(),
            transactionA.isIncome(),
            transactionA.getAccount()
        );

        // mock the repository's findById method to return empty when called with a non-existent ID
        when(transactionRepo.findById(nonExistentId)).thenReturn(Optional.empty());

        //assert that updating a non-existent transaction throws RuntimeException
        assertThatThrownBy(() -> transactionService.updateTransaction(nonExistentId, updatedTransaction))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Transaction not found with id " + nonExistentId);

        // Verify that the repository's save method was never called
        verify(transactionRepo).findById(nonExistentId);
        verify(transactionRepo, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Test that deleting a transaction works correctly")
    void testDeleteTransaction(){

        Long transactionIdToDelete = transactionA.getId();

        // Call the delete method
        transactionService.delete(transactionIdToDelete);

        //assert that the repository's deleteById method was called with the correct ID
        verify(transactionRepo).deleteById(transactionIdToDelete);
    }

    //---------------------------- End of CRUD tests ----------------------------------

    //----------------------------Business logic tests ----------------------------------

    @Test
    @DisplayName("Test that calculating total balance works correctly")
    void testCalculateTotalBalance(){

        // mock the repository's findAll method to return a list with transactionA
        when(transactionRepo.findAll()).thenReturn(transactionsList);

        // Calculate the total balance
        Money totalBalance = transactionService.calculateTotalBalance();

        //assert that the repository's findAll method was called
        verify(transactionRepo).findAll();

        // Manually calculate expected total balance
        Money expectedBalance = transactionsList.stream()
            .map(tx -> tx.isIncome() ? tx.getAmount() : tx.getAmount().negate())
            .reduce(Money.zero(), Money::add);

        //assert that the calculated total balance is correct
        assertThat(totalBalance.toString()).isEqualTo(expectedBalance.toString());
    }

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

    @Test
    @DisplayName("Test that checking existence of a transaction by ID works correctly")
    void testIsExists(){
        Long existingId = transactionA.getId();
        Long nonExistentId = 999L;

        // mock the repository's existsById method
        when(transactionRepo.existsById(existingId)).thenReturn(true);
        when(transactionRepo.existsById(nonExistentId)).thenReturn(false);

        //assert that isExists returns true for existing ID
        assertThat(transactionService.isExists(existingId)).isTrue();

        //assert that isExists returns false for non-existent ID
        assertThat(transactionService.isExists(nonExistentId)).isFalse();

        // Verify that the repository's existsById method was called with correct IDs
        verify(transactionRepo).existsById(existingId);
        
        // Verify that the repository's existsById method was called with correct IDs
        verify(transactionRepo).existsById(nonExistentId);

    }

    @Test
    @DisplayName("Test that checking existence of a transaction by ID returns false when no transactions exist")
    void testIsExistsReturnsFalseWhenNoTransactionsExist(){
        Long anyId = 1L;

        // mock the repository's existsById method to return false
        when(transactionRepo.existsById(anyId)).thenReturn(false);

        //assert that isExists returns false
        assertThat(transactionService.isExists(anyId)).isFalse();

        // Verify that the repository's existsById method was called with correct ID
        verify(transactionRepo).existsById(anyId);
    }

    @Test
    @DisplayName("Test that finding transactions by month works correctly")
    void testFindTransactionsByMonthFiltersCorrectly(){

        Account acc = createTestAccountA();
        YearMonth ym = YearMonth.of(2025, 3);

        Transaction txMar01 = aTransaction().desc("March 1").on(LocalDate.of(2025,3,1)).acct(acc).build();
        Transaction txMar31 = aTransaction().desc("March 31").on(LocalDate.of(2025,3,31)).acct(acc).build();
        Transaction txFeb28 = aTransaction().desc("February 28").on(LocalDate.of(2025, 2, 28)).acct(acc).build();
        Transaction txApr01 = aTransaction().desc("April 1").on(LocalDate.of(2025, 4, 1)).acct(acc).build();
        
        //mock the repos findTransactionsByMonth method by returning
        when(transactionRepo.findAll()).thenReturn(List.of(txMar01, txMar31, txApr01, txFeb28));

        List<Transaction> filtered = transactionService.findTransactionsByMonth(ym);

        //assert that the filtered list of the correct size
        assertThat(filtered.size()).isEqualTo(2);

        //asser that the list contains the same Transactions
        assertThat(filtered).containsExactlyInAnyOrder(txMar01, txMar31)
                            .doesNotContain(txApr01, txFeb28);

    }

    @Test
    @DisplayName("Test that finding transactions on an empty list returns empty list")
    void testFindTransactionsByMonthReturnsNullWhenEmpty(){
        

        //mock the behavior of the findByMonth method
        when(transactionRepo.findAll()).thenReturn(List.of());

        List<Transaction> filtered = transactionService.findTransactionsByMonth(YearMonth.of(2025,10));

        assertThat(filtered).isEmpty();

    }

    @Test
    @DisplayName("Test that calculating Monthly balance returns the correct output")
    void testCalculateMonthlyBalanceComputesCorrectly(){

        Transaction txJuneA = aTransaction().desc("June 1").on(LocalDate.of(2025,6,1)).amt("200.00").income().build();
        Transaction txJuneB = aTransaction().desc("June 15").on(LocalDate.of(2025,6,15)).amt("1000.00").income().build();
        Transaction txJuneC = aTransaction().desc("June 30").on(LocalDate.of(2025,6,30)).amt("600.00").expense().build();
        Transaction txJuly = aTransaction().desc("July 15").on(LocalDate.of(2025, 7, 15)).amt("900.00").expense().build();

        List<Transaction> transactions = List.of(txJuneA, txJuneB, txJuneC, txJuly);

        //mock behavior of findAll method by return the list of transactions in that Month
        when(transactionRepo.findAll()).thenReturn(transactions);

        Money balanceJune = transactionService.calculateMonthlyBalance(YearMonth.of(2025,6));
        Money balanceJuly = transactionService.calculateMonthlyBalance(YearMonth.of(2025, 7));

        //verify that findAll method was called twice
        verify(transactionRepo, times(2)).findAll();

        //assert that the balnce calculated is as expected for both June and July
        assertThat(balanceJune.toString()).isEqualTo(Money.of("600.00").toString());
        assertThat(balanceJuly.toString()).isEqualTo(Money.of("-900.00").toString());
    }

    @Test
    @DisplayName("Test that monthly balance returns zero when no transactions are present in month")
    void testMonthlyBalanceReturnsZeroWhenNoTransactionsInMonth(){

        Transaction txJuneA = aTransaction().desc("June 1").on(LocalDate.of(2025,6,1)).amt("200.00").income().build();
        Transaction txJuneB = aTransaction().desc("June 15").on(LocalDate.of(2025,6,15)).amt("1000.00").income().build();

        //mock behavior of findAll method by return the list of transactions in that Month
        when(transactionRepo.findAll()).thenReturn(List.of(txJuneA, txJuneB));

        Money balance = transactionService.calculateMonthlyBalance(YearMonth.of(2025, 7));

        //verify that findAll method was called only once
        verify(transactionRepo).findAll();

        //assert that the balance for July is zero (Not null or something else)
        assertThat(balance.toString()).isEqualTo(Money.zero().toString());
        assertThat(balance).isNotNull();
    }

    @Test
    @DisplayName("Test that balance is zero when there are no transactions")
    void testMonthlyBalanceIsZeroWhenNoTransactionsInLedger(){

        //mock behavior of findAll method by return the list of transactions in that Month
        when(transactionRepo.findAll()).thenReturn(List.of());

        Money balance = transactionService.calculateMonthlyBalance(YearMonth.of(2025,1));

        //verify that findAll method was called only once
        verify(transactionRepo).findAll();

        //assert that balance is zero and not Null
        assertThat(balance.toString()).isEqualTo(Money.zero().toString());
        assertThat(balance).isNotNull();
    }

    @Test
    @DisplayName("Test that finding transactions by category works correctly")
    void testFindTransactionsByCategoryFiltersCorrectly(){

        Account acc = createTestAccountA();

        Transaction txFoodA = aTransaction().desc("Grocery A").cat(Category.FOOD).acct(acc).build();
        Transaction txFoodB = aTransaction().desc("Grocery B").cat(Category.FOOD).acct(acc).build();
        Transaction txUtilities = aTransaction().desc("Electricity Bill").cat(Category.UTILITIES).acct(acc).build();
        Transaction txIncome = aTransaction().desc("Salary").cat(Category.INCOME).income().acct(acc).build();

        //mock the repos findTransactionsByCategory method by returning
        when(transactionRepo.findAll()).thenReturn(List.of(txFoodA, txFoodB, txUtilities, txIncome));

        List<Transaction> filtered = transactionService.findTransactionsByCategory(Category.FOOD);

        //assert that the filtered list of the correct size
        assertThat(filtered.size()).isEqualTo(2);

        //asser that the list contains the same Transactions
        assertThat(filtered).containsExactlyInAnyOrder(txFoodA, txFoodB)
                            .doesNotContain(txUtilities, txIncome);

    }

    @Test
    @DisplayName("Test that finding transactions by category on an empty list returns empty list")
    void testFindTransactionsByCategoryReturnsNullWhenEmpty(){
        
        //mock the behavior of the findByCategory method
        when(transactionRepo.findAll()).thenReturn(List.of());

        List<Transaction> filtered = transactionService.findTransactionsByCategory(Category.FOOD);

        assertThat(filtered).isEmpty();
    }

    @Test
    @DisplayName("Test that calculating total by category works correctly")
    void testCalculateTotalByCategoryComputesCorrectly(){

        Transaction txFoodA = aTransaction().desc("Grocery A").cat(Category.FOOD).amt("150.00").expense().build();
        Transaction txFoodB = aTransaction().desc("Grocery B").cat(Category.FOOD).amt("200.00").expense().build();
        Transaction txUtilities = aTransaction().desc("Electricity Bill").cat(Category.UTILITIES).amt("100.00").expense().build();
        Transaction txIncome = aTransaction().desc("Salary").cat(Category.INCOME).amt("3000.00").income().build();

        List<Transaction> transactions = List.of(txFoodA, txFoodB, txUtilities, txIncome);

        //mock behavior of findAll method by return the list of transactions in that Month
        when(transactionRepo.findAll()).thenReturn(transactions);

        Money totalFood = transactionService.calculateTotalByCategory(Category.FOOD);
        Money totalUtilities = transactionService.calculateTotalByCategory(Category.UTILITIES);
        Money totalIncome = transactionService.calculateTotalByCategory(Category.INCOME);

        //verify that findAll method was called three times
        verify(transactionRepo, times(3)).findAll();

        //assert that the total calculated is as expected for each category
        assertThat(totalFood.toString()).isEqualTo(Money.of("-350.00").toString());
        assertThat(totalUtilities.toString()).isEqualTo(Money.of("-100.00").toString());
        assertThat(totalIncome.toString()).isEqualTo(Money.of("3000.00").toString());
    }

    @Test
    @DisplayName("Test that calculating total by category returns zero when no transactions exist in that category")
    void testCalculateTotalByCategoryReturnsZeroWhenNoTransactionsInCategory(){
        Transaction txFoodA = aTransaction().desc("Grocery A").cat(Category.FOOD).amt("150.00").expense().build();
        Transaction txFoodB = aTransaction().desc("Grocery B").cat(Category.FOOD).amt("200.00").expense().build();

        //mock behavior of findAll method by return the list of transactions in that Month
        when(transactionRepo.findAll()).thenReturn(List.of(txFoodA, txFoodB));

        Money totalUtilities = transactionService.calculateTotalByCategory(Category.UTILITIES);

        //verify that findAll method was called only once
        verify(transactionRepo).findAll();

        //assert that the total for Utilities is zero (Not null or something else)
        assertThat(totalUtilities.toString()).isEqualTo(Money.zero().toString());
        assertThat(totalUtilities).isNotNull();
    }

    @Test
    @DisplayName("Test that total by category is zero when there are no transactions")
    void testTotalByCategoryIsZeroWhenNoTransactionsInLedger(){

        //mock behavior of findAll method by return the list of transactions in that Month
        when(transactionRepo.findAll()).thenReturn(List.of());

        Money totalFood = transactionService.calculateTotalByCategory(Category.FOOD);

        //verify that findAll method was called only once
        verify(transactionRepo).findAll();

        //assert that total is zero and not Null
        assertThat(totalFood.toString()).isEqualTo(Money.zero().toString());
        assertThat(totalFood).isNotNull();
    }

    @Test
    @DisplayName("Test that summarizing by category works correctly")
    void testSummarizeByCategoryComputesCorrectly(){

        Transaction txFood = aTransaction().desc("Grocery A").cat(Category.FOOD).amt("150.00").expense().build();
        Transaction txUtilities = aTransaction().desc("Electricity Bill").cat(Category.UTILITIES).amt("100.00").expense().build();
        Transaction txIncome = aTransaction().desc("Salary").cat(Category.INCOME).amt("3000.00").income().build();

        List<Transaction> transactions = List.of(txFood, txUtilities, txIncome);

        var summary = transactionService.summariseByCategory(transactions);

        //assert that the summary contains correct totals for each category
        assertThat(summary.get(Category.FOOD).toString()).isEqualTo(Money.of("150.00").toString());
        assertThat(summary.get(Category.UTILITIES).toString()).isEqualTo(Money.of("100.00").toString());
        assertThat(summary.get(Category.INCOME).toString()).isEqualTo(Money.of("3000.00").toString());
    }

    @Test
    @DisplayName("Test that summarizing by category on empty list returns empty map")
    void testSummarizeByCategoryReturnsEmptyMapWhenEmpty(){

        var summary = transactionService.summariseByCategory(List.of());

        //assert that the summary map is empty
        assertThat(summary).isEmpty();
    }
    
}


