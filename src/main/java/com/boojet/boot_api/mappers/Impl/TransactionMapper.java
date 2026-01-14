package com.boojet.boot_api.mappers.Impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.boojet.boot_api.controllers.dto.TransactionDto;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.mappers.Mapper;


@Component
public class TransactionMapper implements Mapper<Transaction, TransactionDto>{
 
    private ModelMapper modelMapper;

    public TransactionMapper(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }

    @Override
    public TransactionDto mapTo(Transaction transaction) {
        return modelMapper.map(transaction, TransactionDto.class);
    }

    @Override
    public Transaction mapFrom(TransactionDto transactionDto) {
        return modelMapper.map(transactionDto, Transaction.class);
    };
}


