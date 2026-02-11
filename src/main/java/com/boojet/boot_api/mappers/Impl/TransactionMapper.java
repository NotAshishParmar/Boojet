package com.boojet.boot_api.mappers.Impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.boojet.boot_api.controllers.dto.TransactionDto;
import com.boojet.boot_api.domain.Account;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.mappers.Mapper;
import com.boojet.boot_api.repositories.AccountRepository;
import com.boojet.boot_api.repositories.CategoryRepository;


@Component
public class TransactionMapper implements Mapper<Transaction, TransactionDto> {

    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepo;
    private final AccountRepository accountRepo;

    public TransactionMapper(ModelMapper modelMapper,
                             CategoryRepository categoryRepo,
                             AccountRepository accountRepo) {
        this.modelMapper = modelMapper;
        this.categoryRepo = categoryRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    public TransactionDto mapTo(Transaction t) {
        TransactionDto dto = modelMapper.map(t, TransactionDto.class);

        // read-only enrich
        if (t.getCategory() != null) {
            dto.setCategoryId(t.getCategory().getId());
            dto.setCategoryCode(t.getCategory().getCode());
            dto.setCategoryName(t.getCategory().getName());
        }
        if (t.getAccount() != null) {
            dto.setAccountId(t.getAccount().getId());
            dto.setAccountName(t.getAccount().getName());
        }
        // income is already on entity
        dto.setIncome(t.isIncome());

        return dto;
    }

    @Override
    public Transaction mapFrom(TransactionDto dto) {
        Transaction t = modelMapper.map(dto, Transaction.class);

        // IMPORTANT: convert IDs -> entity refs
        if (dto.getCategoryId() != null) {
            t.setCategory(categoryRepo.getReferenceById(dto.getCategoryId()));
        }
        if (dto.getAccountId() != null) {
            t.setAccount(accountRepo.getReferenceById(dto.getAccountId()));
        }

        // ignore any client-sent income (even if present)
        // t.setIncome(...)  <-- don't do this

        return t;
    }
}




