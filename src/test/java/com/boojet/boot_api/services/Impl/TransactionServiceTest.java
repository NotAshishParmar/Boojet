package com.boojet.boot_api.services.Impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.CategoryRepository;
import com.boojet.boot_api.repositories.TransactionRepository;

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

    

    @Test
    void createTransaction_shouldSaveExpesneTransaction_whenRequestIsValid(){
        
    }
}
