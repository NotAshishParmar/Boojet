package com.boojet;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.YearMonth;

public class TransactionManager{

    private List<Transaction> transactions = FileStorage.loadTransactions();
    private final SaveMode saveMode;
    private boolean dirty = false;       //tracks unsaved edits
    
    public TransactionManager(SaveMode saveMode){
        this.saveMode = saveMode;
        this.transactions = FileStorage.loadTransactions();
    }

    public void addTransaction(Transaction t){
        transactions.add(t);
        //FileStorage.saveTransactions(transactions); //update file on every change
        markDirty();
    }

    public void updateTransaction(int index, Transaction repl){
        transactions.set(index, repl);
        markDirty();
    }

    public void deleteTransaction(int index) {
        transactions.remove(index);
        markDirty();
    }

    //read-only list for UI to display Transaction Index to user
    public List<Transaction> getTransactions(){
        return Collections.unmodifiableList(transactions);
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

    /* ───── Filtering and Summary ───── */

    //return the transactions in that month
    public List<Transaction> inMonth(YearMonth ym){
        return transactions.stream()
                            .filter(t -> YearMonth.from(t.getDate()).equals(ym))
                            .toList();
    }

    //return all transactions for that category
    public List<Transaction> inCategory(Category cat){
        return transactions.stream()
                            .filter(t -> t.getCategory() == cat)
                            .toList();
    }

    //summary??
    public Map<Category, BigDecimal> summariseByCategory(List<Transaction> list){
        return list.stream().collect(
            Collectors.groupingBy(Transaction::getCategory,
                                    Collectors.mapping(Transaction::getAmount,
                                        Collectors.reducing(BigDecimal.ZERO, (a,b)-> a.add(b)))));

        //make list into stream -> collect data in a map -> group transactions by category -> instead of getting whole transaction object just get the amounts
        //-> add all amounts up/reduce to total (starts at zero) 


        // -------------------A more readable version---------------------
        // Map<Category, BigDecimal> summary = new HashMap<>();

        // for (Transaction t : list) {
        //     Category cat = t.getCategory();
        //     BigDecimal amt = t.getAmount();

        //     summary.put(cat, summary.getOrDefault(cat, BigDecimal.ZERO).add(amt));
        // }

        // return summary;
        // ---------------------------------------------------------------
    
    }

    /* ───── persistence helpers ───── */

    public void save(){
        FileStorage.saveTransactions(transactions);
        dirty = false;
    }

    public boolean hasUnsavedChanges(){
        return dirty;
    }

    public void markDirty(){
        dirty = true;
        if(saveMode == SaveMode.AUTO)
            save();
    }

    // //replace the transaction at "index"
    // public void updateTransaction(int index, Transaction replacement){
    //     if(index < 0 || index >= transactions.size()) throw new IndexOutOfBoundsException();
    //     transactions.set(index, replacement);
    //     FileStorage.saveTransactions(transactions);
    // }

    // //delete transaction at "index"
    // public void deleteTransaction(int index){
    //     if(index < 0 || index >= transactions.size()) throw new IndexOutOfBoundsException();
    //     transactions.remove(index);
    //     FileStorage.saveTransactions(transactions);
    // }


}