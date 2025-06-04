package com.boojet;

import java.math.BigDecimal;
import java.util.List;

public class TransactionManager{

    private List<Transaction> transactions;

    public TransactionManager(){
        transactions = FileStorage.loadTransactions();
    }

    public void addTransaction(Transaction t){
        transactions.add(t);
        FileStorage.saveTransactions(transactions); //update file on every change
    }

    public void listTransactions(){
        for(Transaction t : transactions){
            System.out.println(t);
        }
    }

    public BigDecimal getBalance(){
        BigDecimal balance = BigDecimal.ZERO;

        //if income then add the amount
        //if not income then subtract the amount using negate
        for(Transaction t : transactions){
            balance = balance.add(t.isIncome()
                    ? t.getAmount()
                    : t.getAmount().negate());
        }

        return balance.setScale(2);
    }

}