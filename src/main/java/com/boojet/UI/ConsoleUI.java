package com.boojet.UI;

import com.boojet.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Scanner;


public class ConsoleUI {
    private final TransactionManager manager;
    private final Scanner scanner = new Scanner(System.in);
    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance();

    public ConsoleUI(TransactionManager manager){
        this.manager = manager;
    }

    public void run(){
        boolean running = true;

        while(running){
            printMenu();

            int option = readInt("Choose an option: ");
            switch (option) {
                case 1 -> handleAdd(true);
                case 2 -> handleAdd(false);
                case 3 -> listWithIndexes();                            // view
                case 4 -> showBalance();
                case 5 -> handleEdit();                                 //edit
                case 6 -> handleDelete();                               //delete
                case 7 -> handleCategoryView();
                case 8 -> handleMonthySummary();
                case 9 -> { manager.save(); running = false; }          // Save & Exit
                case 0 -> running = !confirmExit();                      // possibly discard 
                default -> System.out.println("Invalid Option. Try 1-7.");
            }
        }

        System.out.println("Goodbye!");
    }

    // ───────────────── helpers ─────────────────

    private void printMenu(){
        System.out.println("\n\u001B[36m=== Boojet Budget Tracker ===\u001B[0m");
        System.out.println("1. Add Income");
        System.out.println("2. Add Expense");
        System.out.println("3. View Transactions");
        System.out.println("4. View Balance");
        System.out.println("5. Edit Transaction");
        System.out.println("6. Delete Transaction");
        System.out.println("7. Show by Category");
        System.out.println("8. Monthly Summary");
        System.out.println("9. Save & Exit");
        System.out.println("0. Exit (discard)");
    }

    private void handleAdd(boolean isIncome){
        String desc = readLine("Description: ");
        BigDecimal amount = readMoney("Amount: ");
        Category cat = readCategory("Catergory (FOOD, RENT, etc.): ");
        Transaction t = new Transaction(desc, amount, LocalDate.now(), cat, isIncome);
        manager.addTransaction(t);
        System.out.println("Transaction added!");
    }

    private void listWithIndexes(){
        List<Transaction> list = manager.getTransactions();

        if(list.isEmpty()){
            System.out.println("No transaction recorded.");
            return;
        }

        for(int i = 0; i < list.size(); i++){
            System.out.printf("%3d) %s%n", i + 1, list.get(i));
        }
    }

    private void handleEdit(){
        listWithIndexes();

        if(manager.getTransactions().isEmpty()){
            System.out.println("There are no Transactions recorded!");
            return;
        }

        int choice = readInt("Choose which transaction number to edit: ") - 1;
        Transaction old = manager.getTransactions().get(choice);

        String desc = readLine("Description [" + old.getDescription()+ "]: ");
        
        if(desc.isBlank()){
            desc = old.getDescription();
        }

        BigDecimal amount = optionalMoney("Amount [" + old.getAmount() + "]: ", old.getAmount());
        Category cat = optionalCategory("Category [" + old.getCategory() + "]: ", old.getCategory());

        //Ask the user if the transaction is income or expense.
        //Show the current value as the default (I or E) based on old.isIncome().
        //If the user enters "I", it's income → isIncome = true
        //If the user enters anything else (or presses Enter), keep the old value
        boolean isIncome = readLine("Type (I)ncome / (E)xpense [" +
                                    (old.isIncome() ? "I" : "E") + "]: ").strip()
                                    .equalsIgnoreCase("I") ? true : old.isIncome();

        Transaction updated = new Transaction(desc, amount, old.getDate(), cat, isIncome);
        manager.updateTransaction(choice, updated);
        System.out.println(" Transaction Updated.");
        
    }

    private void handleDelete(){
        listWithIndexes();

        if(manager.getTransactions().isEmpty()){
            System.out.println("There are no Transactions recorded!");
            return;
        }

        int choice = readInt("Choose which transaction number to delete: ") - 1;
        String confirm = readLine("Are you sure you want to delete this? (y/n): ");
        
        if(confirm.toLowerCase().startsWith("y")){
            manager.deleteTransaction(choice);
            System.out.println("DELETED!");
        }
    }

    private void handleCategoryView(){
        Category cat = readCategory("Which category? ");
        List<Transaction> list = manager.inCategory(cat);
        if(list.isEmpty()){
            System.out.println("No transaction of type " + cat);
            return;
        }

        list.forEach(System.out::println);
        BigDecimal total = list.stream()
                                .map( t-> t.isIncome()? t.getAmount() : t.getAmount().negate())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("Net value for " + cat + ": " + currencyFmt.format(total));



        //--------------------------------More readable format-----------------------------
        // Category cat = readCategory("Which category? ");
    
        // List<Transaction> list = manager.inCategory(cat);

        // if (list.isEmpty()) {
        //     System.out.println("No transactions in " + cat);
        //     return;
        // }

        // for (Transaction t : list) {
        //     System.out.println(t);
        // }

        // BigDecimal total = BigDecimal.ZERO;
        // for (Transaction t : list) {
        //     if (t.isIncome()) {
        //         total = total.add(t.getAmount());
        //     } else {
        //         total = total.subtract(t.getAmount());
        //     }
        // }

        // System.out.println("Net total for " + cat + ": " + currencyFmt.format(total));
        //---------------------------------------------------------------------------------

    }


    private void handleMonthySummary(){

        int y = readInt("Year (e.g. 2025): ");
        int m = readInt("Month (1-12): ");
        YearMonth ym = YearMonth.of(y,m);           //convert to format

        var subset = manager.inMonth(ym);
        if(subset.isEmpty()){
            System.out.println("There were no transaction made in " + ym);
            return;
        }

        Map<Category, BigDecimal> map = manager.summariseByCategory(subset);
        BigDecimal monthNet = subset.stream()
                                        .map(t -> t.isIncome()? t.getAmount() : t.getAmount().negate())
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("\n-------- " + ym + " ---------");
        map.forEach((cat, val) -> System.out.printf("%-12s %s%n", cat, currencyFmt.format(val)));
        System.out.println("--------------------------");
        System.out.println(" Net balance:    "+ currencyFmt.format(monthNet));


        //------------------------------More readable format--------------------------------
        // Map<Category, BigDecimal> map = new HashMap<>();
        // BigDecimal monthNet = BigDecimal.ZERO;

        // for (Transaction t : subset) {
        //     Category cat = t.getCategory();
        //     BigDecimal amount = t.getAmount();

        //     // Add or subtract based on income/expense
        //     if (t.isIncome()) {
        //         monthNet = monthNet.add(amount);
        //     } else {
        //         amount = amount.negate();
        //         monthNet = monthNet.add(amount);
        //     }

        //     // Update category total
        //     BigDecimal current = map.getOrDefault(cat, BigDecimal.ZERO);
        //     map.put(cat, current.add(amount));
        // }

        // // Print summary
        // System.out.println("\n " + ym);
        // for (Map.Entry<Category, BigDecimal> entry : map.entrySet()) {
        //     Category cat = entry.getKey();
        //     BigDecimal total = entry.getValue();
        //     System.out.printf("  %-12s %s%n", cat, currencyFmt.format(total));
        // }

        // System.out.println("-------------------------------");
        // System.out.println("  Net balance     " + currencyFmt.format(monthNet));
        //----------------------------------------------------------------------------------


    }

    private void showBalance(){
        BigDecimal balance = manager.getBalance();
        System.out.println("Current Balance: " + currencyFmt.format(balance));
    }

    private boolean confirmQuit(){
        String ans = readLine("Are you sure you want to Quit? (y/n): ").toLowerCase();
        return ans.startsWith("y");
    }

    private BigDecimal optionalMoney(String prompt, BigDecimal fallback){
        String s = readLine(prompt);
        return s.isBlank() ? fallback : new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
    }

    private Category optionalCategory(String prompt, Category fallback){
        String s = readLine(prompt);
        return s.isBlank() ? fallback : Category.valueOf(s.toUpperCase());
    }

    // ─────Exit Confirmation Helper ─────

    private boolean confirmExit(){
        if(manager.hasUnsavedChanges()){
            String ans = readLine("Unsaved changes! Exit without saving? (y/n): ");
            return ans.equalsIgnoreCase("y");
        }
        return true;
    }

    // ───── low-level input utilities ─────

    private String readLine(String prompt){
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt){
        while (true){
            try{
                return Integer.parseInt(readLine(prompt));
            } catch(NumberFormatException e){
                System.out.println("Please enter a number.");
            }
        }
    }

    private BigDecimal readMoney(String prompt){
        while(true){
            String s = readLine(prompt);
            if(s.matches("\\d+(\\.\\d{1,2})?")){
                return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
            }
            System.out.println("Enter a positive amount with up to two decimals.");
        }
    }

    private Category readCategory(String prompt){
        while(true){
            try{
                return Category.valueOf(readLine(prompt).toUpperCase());
            } catch(IllegalArgumentException e){
                System.out.println("Unknown category. Try again.");
            }
        }
    }

}
