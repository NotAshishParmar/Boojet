package com.boojet;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class TransactionManager{

    private List<Transaction> transactions = FileStorage.loadTransactions();

    
    public TransactionManager(){}

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

    //read-only list for UI to display Transaction Index to user
    public List<Transaction> getTransactions(){
        return Collections.unmodifiableList(transactions);
    }

    //replace the transaction at "index"
    public void updateTransaction(int index, Transaction replacement){
        if(index < 0 || index >= transactions.size()) throw new IndexOutOfBoundsException();
        transactions.set(index, replacement);
        FileStorage.saveTransactions(transactions);
    }

    //delete transaction at "index"
    public void deleteTransaction(int index){
        if(index < 0 || index >= transactions.size()) throw new IndexOutOfBoundsException();
        transactions.remove(index);
        FileStorage.saveTransactions(transactions);
    }


}