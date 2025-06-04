package com.boojet.UI;

import com.boojet.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
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
                case 3 -> manager.listTransactions();
                case 4 -> showBalance();
                case 5 -> running = !confirmQuit();
                default -> System.out.println("Invalid Option. Try 1-5.");
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
        System.out.println("5. Exit");
    }

    private void handleAdd(boolean isIncome){
        String desc = readLine("Description: ");
        BigDecimal amount = readMoney("Amount: ");
        Category cat = readCategory("Catergory (FOOD, RENT, etc.): ");
        Transaction t = new Transaction(desc, amount, LocalDate.now(), cat, isIncome);
        manager.addTransaction(t);
        System.out.println("Transaction added!");
    }

    private void showBalance(){
        BigDecimal balance = manager.getBalance();
        System.out.println("Current Balance: " + currencyFmt.format(balance));
    }

    private boolean confirmQuit(){
        String ans = readLine("Are you sure you want to Quit? (y/n): ").toLowerCase();
        return ans.startsWith("y");
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
