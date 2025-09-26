package com.boojet.boot_api;

import com.boojet.boot_api.UI.ConsoleUI;

public class BoojetApp{
    public static void main(String[] args){
        TransactionManager manager = new TransactionManager(SaveMode.MANUAL);
        ConsoleUI ui = new ConsoleUI(manager);
        ui.run();
    }
}
