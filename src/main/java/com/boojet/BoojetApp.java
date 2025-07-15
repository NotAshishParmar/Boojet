package com.boojet;

import com.boojet.UI.ConsoleUI;

public class BoojetApp{
    public static void main(String[] args){
        TransactionManager manager = new TransactionManager(SaveMode.MANUAL);
        ConsoleUI ui = new ConsoleUI(manager);
        ui.run();
    }
}