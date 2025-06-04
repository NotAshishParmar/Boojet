package com.boojet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Scanner;

public class BoojetApp{
    public static void main(String[] args){
        TransactionManager manager = new TransactionManager();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while(running){
            
            System.out.println("\n--- Boojet Budget Tracker ---");
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. View Transaction");
            System.out.println("4. View Balance");
            System.out.println("5. Exit");
            System.out.println("Choose an option");
            int option = scanner.nextInt();
            scanner.nextLine();

            switch(option){
                case 1, 2 -> {
                    System.out.println("Description: ");
                    String desc = scanner.nextLine();
                    System.out.println("Amount: ");
                    String amountStr = scanner.nextLine().trim();
                    BigDecimal amount = new BigDecimal(amountStr).setScale(2, RoundingMode.HALF_UP);
                    //scanner.nextLine();
                    System.out.println("Category (FOOD, RENT, etc.): ");
                    Category cat = Category.valueOf(scanner.nextLine().toUpperCase());

                    boolean isIncome = (option == 1);
                    Transaction t = new Transaction(desc, amount, LocalDate.now(), cat, isIncome);
                    manager.addTransaction(t);
                    System.out.println("Transaction added.");
                }
                case 3 -> manager.listTransactions();
                case 4 -> System.out.println("Balance: " + NumberFormat.getCurrencyInstance().format(manager.getBalance()));
                case 5 -> running = false;
                default -> System.out.println("Invalid option.");
            }
        }

        System.out.println("Goodbye!");
        scanner.close();
    }
}