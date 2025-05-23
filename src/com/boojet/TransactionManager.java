package com.boojet;

import java.util.ArrayList;
import java.util.List;

public class TransactionManager{

    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t){
        transactions.add(t);
    }

    public void listTransactions(){
        for(Transaction t : transactions){
            System.out.println(t);
        }
    }

    public double getBalance(){
        double balance = 0;

        for(Transaction t : transactions){
            balance += t.isIncome() ? t.getAmount() : -t.getAmount();
        }

        return balance;
    }

}