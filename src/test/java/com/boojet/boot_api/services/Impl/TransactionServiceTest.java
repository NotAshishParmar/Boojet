package com.boojet.boot_api.services.Impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.dto.transaction.TransactionCreateRequest;
import com.boojet.boot_api.dto.transaction.TransactionPatchRequest;
import com.boojet.boot_api.dto.transaction.TransactionPutRequest;
import com.boojet.boot_api.dto.transaction.TxSuggestionDetails;
import com.boojet.boot_api.exceptions.AccountNotFoundException;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.CategoryNotFoundException;
import com.boojet.boot_api.exceptions.TransactionNotFoundException;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.CategoryRepository;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.testutil.TestAccounts;
import com.boojet.boot_api.testutil.TestCategories;
import com.boojet.boot_api.testutil.TestTransactionCreateRequests;
import com.boojet.boot_api.testutil.TestTransactionPatchRequests;
import com.boojet.boot_api.testutil.TestTransactionPutRequests;
import com.boojet.boot_api.testutil.TestTransactions;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private CategoryRepository categoryRepo;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account chequing;
    private Account savings;
    private Category groceries;
    private Category salary;
    private Category transfer;

    @BeforeEach
    void setup(){
        chequing = TestAccounts.chequing();
        savings = TestAccounts.savings();
        groceries = TestCategories.groceries();     //expense type category
        salary = TestCategories.salary();           //income type category
        transfer = TestCategories.transfer();       //transfer type category
    }

    @Test
    void createTransaction_shouldSaveExpenseTransaction_whenRequestIsValid(){

        //build input
        TransactionCreateRequest req = TestTransactionCreateRequests.validExpense();

        //stub repository behavior
        stubValidExpenseTransactionCreate();

        //call the method under test
        Transaction result = transactionService.createTransaction(req);

        //verify that save() was called ANDDD capture the actual transaction object passed into it
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepo).save(captor.capture());

        Transaction saved = captor.getValue();

        //assert
        assertThat(result).isSameAs(saved);
        assertThat(saved.getDescription()).isEqualTo("Groceries");
        assertThat(saved.getAmount()).isEqualTo(Money.of("45.99"));
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(saved.getCategory()).isEqualTo(groceries);
        assertThat(saved.getAccount()).isEqualTo(chequing);
        assertThat(saved.getToAccount()).isNull();
        assertThat(saved.isIncome()).isFalse();
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenRequestIsNull(){

        assertThatThrownBy(() -> transactionService.createTransaction(null))
            .isInstanceOf(BadRequestException.class);

    }

    @Test
    void createTransaction_shouldTrimDescription_whenDescriptionHasWhitespace(){

        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withDescription("   Groceries at Walmart  ")
                                        .build();

        //stub
        stubValidExpenseTransactionCreate();

        //call the method
        Transaction result = transactionService.createTransaction(req);

        //assert
        assertThat(result.getDescription()).isEqualTo("Groceries at Walmart");
    }

    @Test
    void createTransaction_shouldDefaultDescription_whenDescriptionIsBlank(){

        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withDescription("    ")
                                        .build();
        
        //stub                                
        stubValidExpenseTransactionCreate();
        
        //action
        Transaction result = transactionService.createTransaction(req);

        //assert
        assertThat(result.getDescription()).isEqualTo("No description");
    }

    @Test
    void createTransaction_shouldDefaultDescription_whenDescriptionIsNull(){

        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withDescription(null)
                                        .build();

        //stub
        stubValidExpenseTransactionCreate();

        //action
        Transaction result = transactionService.createTransaction(req);

        //assert
        assertThat(result.getDescription()).isEqualTo("No description");
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenAmountIsNull(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withAmount(null)
                                        .build();

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenAmountIsNegative(){

        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withAmount(Money.of("-100.00"))
                                        .build();

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenAmountIsZero(){

        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withAmount(Money.zero())
                                        .build();

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldDefaultDateToToday_whenDateIsNull(){
        
        //build 
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withDate(null)
                                        .build();
        
        //stub
        stubValidExpenseTransactionCreate();

        //action
        Transaction result = transactionService.createTransaction(req);

        //assert
        assertThat(result.getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createTransaction_shouldSaveExpenseTransaction_whenDateIsInFuture(){

        //build
        LocalDate futureDate = LocalDate.now().plusDays(10);
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withDate(futureDate)
                                        .build();
        
        //stub
        stubValidExpenseTransactionCreate();

        //action
        Transaction result = transactionService.createTransaction(req);

        //assert
        assertThat(result.getDate()).isEqualTo(futureDate);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenAccountIdIsNull(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withAccountId(null)
                                        .build();

        //stub
        when(categoryRepo.findById(req.categoryId())).thenReturn(Optional.of(groceries));

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenAccountIdIsNegative(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withAccountId(-5L)
                                        .build();

        //stub
        when(categoryRepo.findById(req.categoryId())).thenReturn(Optional.of(groceries));

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldThrowAccountNotFoundException_whenAccountIdDoesNotExist(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withAccountId(9999L)
                                        .build();

        //stub
        when(categoryRepo.findById(req.categoryId())).thenReturn(Optional.of(groceries));       //stub category lookup as it happens before account lookup
        when(accountRepo.findById(9999L)).thenReturn(Optional.empty());         //.thenThrow(AccountNotFoundException.class) would be 
                                                                                //inappropriate since the repo returns optional and it's
                                                                                //the service that throws the exception, not the repo call

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenCategoryIdIsNull(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withCategoryId(null)
                                        .build();

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenCategoryIdIsNegative(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withCategoryId(-5L)
                                        .build();

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldThrowCategoryNotFoundException_whenCategoryIdDoesNotExist(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withCategoryId(9999L)
                                        .build();

        //stub
        when(categoryRepo.findById(req.categoryId())).thenReturn(Optional.empty());

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void createTransaction_shouldSaveTransferTransaction_whenCategoryTypeIsTransfer(){
        
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withCategoryId(transfer.getId())
                                        .withToAccountId(savings.getId())
                                        .build();

        //stub
        when(categoryRepo.findById(transfer.getId())).thenReturn(Optional.of(transfer));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));
        when(accountRepo.findById(savings.getId())).thenReturn(Optional.of(savings));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //action
        Transaction result = transactionService.createTransaction(req);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepo).save(captor.capture());

        Transaction saved = captor.getValue();

        //assert
        assertThat(result).isSameAs(saved);
        assertThat(saved.getAccount()).isEqualTo(chequing);
        assertThat(saved.getToAccount()).isEqualTo(savings);
        assertThat(saved.getCategory()).isEqualTo(transfer);
        assertThat(saved.isIncome()).isFalse();
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenToAccountIsNullForTransferTransaction(){
     
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withCategoryId(transfer.getId())
                                        .withToAccountId(null)
                                        .build();

        //stub
        when(categoryRepo.findById(transfer.getId())).thenReturn(Optional.of(transfer));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));

        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createTransaction_shouldThrowBadRequestException_whenToAccountIsSameAsFromAccountForTransferTransaction(){
     
        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withAccountId(chequing.getId())
                                        .withCategoryId(transfer.getId())
                                        .withToAccountId(chequing.getId())
                                        .build();

        //stub
        when(categoryRepo.findById(transfer.getId())).thenReturn(Optional.of(transfer));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));

        //assert
        assertThatThrownBy(() -> transactionService.createTransaction(req))
            .isInstanceOf(BadRequestException.class);

        verify(accountRepo, times(2)).findById(chequing.getId());
    }

    @Test
    void createTransaction_shouldSetIncomeFalse_whenCategoryTypeIsExpense(){

        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.validExpense();

        //stub
        stubValidExpenseTransactionCreate();

        //action
        Transaction result = transactionService.createTransaction(req);

        //assert
        assertThat(result.isIncome()).isFalse();
    }

    @Test
    void createTransaction_shouldSetIncomeTrue_whenCategoryTypeIsIncome(){

        //build
        TransactionCreateRequest req = TestTransactionCreateRequests.builder()
                                        .withCategoryId(salary.getId())
                                        .build();

        //stub
        stubValidIncomeTransactionCreate();

        //action
        Transaction result = transactionService.createTransaction(req);

        //assert
        assertThat(result.isIncome()).isTrue();
    }

    @Test
    void search_shouldReturnRepositoryResults_whenNoFiltersAreProvided(){

        //build
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Transaction> expected = new PageImpl<>(List.of(TestTransactions.groceriesExpense()));

        //stub
        when(transactionRepo.search(
            null, 
            null, 
            LocalDate.of(1, 1, 1), 
            LocalDate.of(9999, 12, 31), 
            pageable
        )).thenReturn(expected);

        //action
        Page<Transaction> result = transactionService.search(null, null, null, null, pageable);

        //assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void search_shouldFilterByAccount_whenAccountIdIsValid(){
        PageRequest pageable = PageRequest.of(0, 10);
        Long accountId = 1L;
        Page<Transaction> expected = Page.empty();

        when(accountRepo.existsById(accountId)).thenReturn(true);
        when(transactionRepo.search(
            accountId, 
            null,
            LocalDate.of(1, 1, 1), 
            LocalDate.of(9999, 12, 31), 
            pageable
        )).thenReturn(expected);

        Page<Transaction> result = transactionService.search(accountId, null, null, null, pageable);

        assertThat(result).isEqualTo(expected);
        verify(accountRepo).existsById(accountId);
    }

    @Test
    void search_shouldThrow_whenAccountDoesNotExist(){
        PageRequest pageable = PageRequest.of(0, 10);
        
        when(accountRepo.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.search(999L, null, null, null, pageable))
            .isInstanceOf(AccountNotFoundException.class);

        verify(transactionRepo, never()).search(any(), any(), any(), any(), any());
    }

    @Test
    void search_shouldFilterByCategory_whenCategoryIdIsValid(){
        PageRequest pageable = PageRequest.of(0, 10);
        Long categoryId = 1L;
        Page<Transaction> expected = Page.empty();

        when(categoryRepo.existsById(categoryId)).thenReturn(true);
        when(transactionRepo.search(
            null, 
            categoryId,
            LocalDate.of(1, 1, 1), 
            LocalDate.of(9999, 12, 31), 
            pageable
        )).thenReturn(expected);

        Page<Transaction> result = transactionService.search(null, categoryId, null, null, pageable);

        assertThat(result).isEqualTo(expected);
        verify(categoryRepo).existsById(categoryId);
    }

    @Test
    void search_shouldThrow_whencategoryDoesNotExist(){
        PageRequest pageable = PageRequest.of(0, 10);
        
        when(categoryRepo.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.search(null, 999L, null, null, pageable))
            .isInstanceOf(CategoryNotFoundException.class);

        verify(transactionRepo, never()).search(any(), any(), any(), any(), any());
    }

    @Test
    void search_shouldUseMonthDateRange_whenYearAndMonthProvided() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Transaction> expected = Page.empty();

        when(transactionRepo.search(
                null,
                null,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                pageable
        )).thenReturn(expected);

        Page<Transaction> result = transactionService.search(null, null, 2026, 3, pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void search_shouldThrow_whenMonthIsInvalid() {
        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> transactionService.search(null, null, 2026, 13, pageable))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void findTransaction_shouldThrow_whenTransactionNotFound(){
        Long id = 999L;

        when(transactionRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findTransaction(id))
                .isInstanceOf(TransactionNotFoundException.class);

        verify(transactionRepo).findById(id);
    }

    @Test
    void findTransaction_shouldThrow_whenTransactionIdIsInvalid(){
        assertThatThrownBy(() -> transactionService.findTransaction(0L))
            .isInstanceOf(BadRequestException.class);

        verify(transactionRepo, never()).findById(any());
    }

    @Test
    void findTransaction_shouldReturnTransaction_whenIdIsValidAndFound(){
        Transaction tx = TestTransactions.groceriesExpense();

        when(transactionRepo.findById(tx.getId())).thenReturn(Optional.of(tx));

        Transaction result = transactionService.findTransaction(tx.getId());

        assertThat(result).isEqualTo(tx);
        verify(transactionRepo).findById(tx.getId());
    }

    @Test
    void putTransaction_shouldReplaceAllFields_whenRequestIsValidExpense(){
        Transaction existing = TestTransactions.groceriesExpense();
        Category diningOut = TestCategories.diningOut();
        TransactionPutRequest req = TestTransactionPutRequests.validExpense();

        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(diningOut.getId())).thenReturn(Optional.of(diningOut));
        when(accountRepo.findById(savings.getId())).thenReturn(Optional.of(savings));
        when(transactionRepo.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.putTransaction(existing.getId(), req);

        assertThat(result.getDescription()).isEqualTo("Dining with friends");
        assertThat(result.getAmount()).isEqualTo(Money.of("80.00"));
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.getCategory()).isEqualTo(diningOut);
        assertThat(result.getAccount()).isEqualTo(savings);
        assertThat(result.getToAccount()).isNull();
        assertThat(result.isIncome()).isFalse();

        verify(transactionRepo).save(existing);
    }

    @Test
    void putTransaction_shouldSetIncomeTrue_whenCategoryIsIncome(){
        Transaction existing = TestTransactions.groceriesExpense();
        
        TransactionPutRequest req = TestTransactionPutRequests.builder()
                                        .withDescription("New Salary")
                                        .withAccountId(chequing.getId())
                                        .withCategoryId(salary.getId())
                                        .build();

        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(salary.getId())).thenReturn(Optional.of(salary));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.putTransaction(existing.getId(), req);

        assertThat(result.getCategory()).isEqualTo(salary);
        assertThat(result.isIncome()).isTrue();
        assertThat(result.getToAccount()).isNull();
    }

    @Test
    void putTransaction_shouldSerTransferFieldsAndForceIncomeFalse_whenCategoryIsTransfer(){
        Transaction existing = TestTransactions.groceriesExpense();

        TransactionPutRequest req = TestTransactionPutRequests.builder()
                                        .withDescription("Transfer from Savings to Chequing")
                                        .withCategoryId(transfer.getId())
                                        .withToAccountId(chequing.getId())
                                        .build();

        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(transfer.getId())).thenReturn(Optional.of(transfer));
        when(accountRepo.findById(savings.getId())).thenReturn(Optional.of(savings));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.putTransaction(existing.getId(), req);

        assertThat(result.getCategory()).isEqualTo(transfer);
        assertThat(result.getAccount()).isEqualTo(savings);
        assertThat(result.getToAccount()).isEqualTo(chequing);
        assertThat(result.isIncome()).isFalse();
    }

    @Test
    void putTransaction_shouldClearToAccount_whenCategoryIsNotTransferEvenIfRequestContainsToAccountId(){
        Transaction existing = TestTransactions.transferToSavings();

        TransactionPutRequest req = TestTransactionPutRequests.builder()
                                        .withAccountId(savings.getId())
                                        .withToAccountId(chequing.getId())
                                        .withCategoryId(groceries.getId())
                                        .build();
    
        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(groceries.getId())).thenReturn(Optional.of(groceries));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));
        when(accountRepo.findById(savings.getId())).thenReturn(Optional.of(savings));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.putTransaction(existing.getId(), req);

        assertThat(result.getToAccount()).isNull();
        assertThat(result.isIncome()).isFalse();
    }

    @Test
    void patchTransaction_shouldUpdateOnlyProvidedFields_whenSingleFieldPatched() {
        Transaction existing = TestTransactions.groceriesExpense();

        TransactionPatchRequest req = TestTransactionPatchRequests.builder()
                .withDescription("Updated description")
                .withAmount(null)
                .withDate(null)
                .withCategoryId(null)
                .withAccountId(null)
                .withoutToAccountId()
                .build();

        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.patchTransaction(existing.getId(), req);

        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getAmount()).isEqualTo(Money.of("45.99"));
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.getCategory()).isEqualTo(existing.getCategory());
        assertThat(result.getAccount()).isEqualTo(existing.getAccount());
        assertThat(result.getToAccount()).isEqualTo(existing.getToAccount());
        assertThat(result.isIncome()).isFalse();
    }

    @Test
    void patchTransaction_shouldSetIncomeTrue_whenCategoryPatchedToIncome() {
        Transaction existing = TestTransactions.groceriesExpense();

        TransactionPatchRequest req = TestTransactionPatchRequests.builder()
                .withDescription(null)
                .withAmount(null)
                .withDate(null)
                .withCategoryId(salary.getId())
                .withAccountId(null)
                .withoutToAccountId()
                .build();

        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(salary.getId())).thenReturn(Optional.of(salary));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.patchTransaction(existing.getId(), req);

        assertThat(result.getCategory()).isEqualTo(salary);
        assertThat(result.isIncome()).isTrue();
        assertThat(result.getToAccount()).isNull();
    }

    @Test
    void patchTransaction_shouldClearToAccount_whenChangedFromTransferToExpense() {
        Transaction existing = TestTransactions.transferToSavings();

        TransactionPatchRequest req = TestTransactionPatchRequests.builder()
                .withDescription(null)
                .withAmount(null)
                .withDate(null)
                .withCategoryId(groceries.getId())
                .withAccountId(null)
                .withoutToAccountId()
                .build();

        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(groceries.getId())).thenReturn(Optional.of(groceries));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.patchTransaction(existing.getId(), req);

        assertThat(result.getCategory()).isEqualTo(groceries);
        assertThat(result.getToAccount()).isNull();
        assertThat(result.isIncome()).isFalse();
    }

    @Test
    void patchTransaction_shouldThrowBadRequestException_whenRequestIsNull() {
        assertThatThrownBy(() -> transactionService.patchTransaction(1L, null))
                .isInstanceOf(BadRequestException.class);

        verify(transactionRepo, never()).findById(any());
        verify(transactionRepo, never()).save(any());
    }

    @Test
    void patchTransaction_shouldThrowBadRequestException_whenPatchedToTransferWithoutToAccount() {
        Transaction existing = TestTransactions.groceriesExpense();

        TransactionPatchRequest req = TestTransactionPatchRequests.builder()
                .withDescription(null)
                .withAmount(null)
                .withDate(null)
                .withCategoryId(transfer.getId())
                .withAccountId(null)
                .withoutToAccountId()
                .build();

        when(transactionRepo.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(transfer.getId())).thenReturn(Optional.of(transfer));

        assertThatThrownBy(() -> transactionService.patchTransaction(existing.getId(), req))
                .isInstanceOf(BadRequestException.class);

        verify(transactionRepo, never()).save(any());
    }

    @Test
    void delete_shouldDeleteTransaction_whenIdIsValidAndTransactionExists() {
        Long id = 100L;

        when(transactionRepo.existsById(id)).thenReturn(true);

        transactionService.delete(id);

        verify(transactionRepo).existsById(id);
        verify(transactionRepo).deleteById(id);
    }

    @Test
    void delete_shouldThrowBadRequestException_whenIdIsNull() {
        assertThatThrownBy(() -> transactionService.delete(null))
                .isInstanceOf(BadRequestException.class);

        verify(transactionRepo, never()).existsById(any());
        verify(transactionRepo, never()).deleteById(any());
    }

    @Test
    void delete_shouldThrowTransactionNotFoundException_whenTransactionDoesNotExist() {
        Long id = 999L;

        when(transactionRepo.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.delete(id))
                .isInstanceOf(TransactionNotFoundException.class);

        verify(transactionRepo).existsById(id);
        verify(transactionRepo, never()).deleteById(any());
    }


    @Test
    void suggest_shouldReturnEmptyList_whenNameIsNull() {
        List<String> result = transactionService.suggest(null, 5);

        assertThat(result).isEmpty();
        verify(transactionRepo, never()).suggestPrefix(any(), any());
        verify(transactionRepo, never()).suggestContains(any(), any());
    }

    @Test
    void suggest_shouldReturnEmptyList_whenTrimmedNameIsTooShort() {
        List<String> result = transactionService.suggest(" a ", 5);

        assertThat(result).isEmpty();
        verify(transactionRepo, never()).suggestPrefix(any(), any());
        verify(transactionRepo, never()).suggestContains(any(), any());
    }

    @Test
    void suggest_shouldReturnEmptyList_whenHowManyIsNotPositive() {
        List<String> result = transactionService.suggest("gro", 0);

        assertThat(result).isEmpty();
        verify(transactionRepo, never()).suggestPrefix(any(), any());
        verify(transactionRepo, never()).suggestContains(any(), any());
    }

    @Test
    void suggest_shouldReturnPrefixMatchesOnly_whenPrefixAlreadyFillsLimit() {
        when(transactionRepo.suggestPrefix(eq("gro"), any()))
                .thenReturn(List.of("Groceries", "Groceries at Walmart"));

        List<String> result = transactionService.suggest(" gro ", 2);

        assertThat(result).containsExactly("Groceries", "Groceries at Walmart");
        verify(transactionRepo).suggestPrefix(eq("gro"), any());
        verify(transactionRepo, never()).suggestContains(any(), any());
    }

    @Test
    void suggest_shouldAppendContainsMatchesAndDeduplicate() {
        when(transactionRepo.suggestPrefix(eq("gro"), any()))
                .thenReturn(List.of("Groceries"));
        when(transactionRepo.suggestContains(eq("gro"), any()))
                .thenReturn(List.of("Groceries", "Frozen Groceries", "Weekly groceries run"));

        List<String> result = transactionService.suggest("gro", 3);

        assertThat(result).containsExactly(
                "Groceries",
                "Frozen Groceries",
                "Weekly groceries run"
        );
    }

    @Test
    void suggest_shouldCapLimitAt15() {
        List<String> prefixResults = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            prefixResults.add("Item " + i);
        }

        when(transactionRepo.suggestPrefix(eq("gr"), any()))
                .thenReturn(prefixResults);

        List<String> result = transactionService.suggest("gr", 50);

        assertThat(result).hasSize(15);
        verify(transactionRepo).suggestPrefix(eq("gr"), any());
        verify(transactionRepo, never()).suggestContains(any(), any());
    }

    @Test
    void suggestionDetails_shouldReturnMappedDetails_whenMatchingTransactionExists() {
        Transaction match = TestTransactions.groceriesExpense();

        when(transactionRepo.findTopByDescriptionIgnoreCaseOrderByDateDescIdDesc("groceries"))
                .thenReturn(Optional.of(match));

        TxSuggestionDetails result = transactionService.suggestionDetails(" groceries ");

        assertThat(result.description()).isEqualTo(match.getDescription());
        assertThat(result.categoryId()).isEqualTo(match.getCategory().getId());
        assertThat(result.amount()).isEqualTo(match.getAmount());
        assertThat(result.income()).isEqualTo(match.isIncome());
        assertThat(result.accountId()).isEqualTo(match.getAccount().getId());

        verify(transactionRepo)
                .findTopByDescriptionIgnoreCaseOrderByDateDescIdDesc("groceries");
    }

    @Test
    void suggestionDetails_shouldThrowTransactionNotFoundException_whenNoMatchExists() {
        when(transactionRepo.findTopByDescriptionIgnoreCaseOrderByDateDescIdDesc("groceries"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.suggestionDetails(" groceries "))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("No transactions match the description provided (Tx Suggestion)");

        verify(transactionRepo)
                .findTopByDescriptionIgnoreCaseOrderByDateDescIdDesc("groceries");
    }


    @Test
    void findTransactionsByMonth_shouldThrowBadRequestException_whenYearOrMonthIsNull() {
        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> transactionService.findTransactionsByMonth(null, 3, pageable))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Year or month cannot be null");

        verify(transactionRepo, never()).search(any(), any(), any(), any(), any());
    }

    @Test
    void findTransactionsByMonth_shouldThrowBadRequestException_whenMonthIsInvalid() {
        PageRequest pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> transactionService.findTransactionsByMonth(2026, 13, pageable))
                .isInstanceOf(BadRequestException.class);

        verify(transactionRepo, never()).search(any(), any(), any(), any(), any());
    }

    @Test
    void findTransactionsByMonth_shouldSearchUsingMonthDateRange_whenInputsAreValid() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Transaction> expected = new PageImpl<>(List.of(TestTransactions.groceriesExpense()));

        when(transactionRepo.search(
                null,
                null,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                pageable
        )).thenReturn(expected);

        Page<Transaction> result = transactionService.findTransactionsByMonth(2026, 3, pageable);

        assertThat(result).isEqualTo(expected);
        verify(transactionRepo).search(
                null,
                null,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                pageable
        );
    }

    @Test
    void calculateMonthlyBalance_shouldReturnMoneyFromMonthlyNetSum_whenInputsAreValid() {
        when(transactionRepo.sumNetBetween(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        )).thenReturn(new BigDecimal("420.50"));

        Money result = transactionService.calculateMonthlyBalance(2026, 3);

        assertThat(result).isEqualTo(Money.of("420.50"));
        verify(transactionRepo).sumNetBetween(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );
    }

    @Test
    void calculateMonthlyBalance_shouldThrowBadRequestException_whenMonthIsInvalid() {
        assertThatThrownBy(() -> transactionService.calculateMonthlyBalance(2026, 13))
                .isInstanceOf(BadRequestException.class);

        verify(transactionRepo, never()).sumNetBetween(any(), any());
    }

    @Test
    void calculateMonthlyBalance_shouldThrowBadRequestException_whenYearOrMonthIsNull() {
        assertThatThrownBy(() -> transactionService.calculateMonthlyBalance(null, 3))
                .isInstanceOf(BadRequestException.class);

        verify(transactionRepo, never()).sumNetBetween(any(), any());
    }



    //-----------------------------------------------HELPERS--------------------------------------------------

    //stub a valid transaction creation
    private void stubValidExpenseTransactionCreate(){
        when(categoryRepo.findById(groceries.getId())).thenReturn(Optional.of(groceries));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0)); //when saving just return the transaction object that's being passed
    }

    private void stubValidIncomeTransactionCreate(){
        when(categoryRepo.findById(salary.getId())).thenReturn(Optional.of(salary));
        when(accountRepo.findById(chequing.getId())).thenReturn(Optional.of(chequing));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0)); //when saving just return the transaction object that's being passed
    }


}


