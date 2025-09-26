package com.boojet.boot_api;

// imports for JSON processing, file handling and data structures
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;    //Jackson doesn't support LocalDate so need to use JavaTime Module

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileStorage {

    public static final String FILE_NAME = "transactions.json";     //file name
    //Jackson ObjectMapper handles converting between Java objects and JSON
    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        //register the module to support LocalDate, LocalDateTime, etc.
        mapper.registerModule(new JavaTimeModule());
    }

    // save JSON
    public static void saveTransactions(List<Transaction> transactions){
        try{
            //convert list of transactions to JSON file
            mapper.writeValue(new File(FILE_NAME), transactions);
        } catch(IOException e){
            System.out.println("Error saving transactions: "+ e.getMessage());
        }
    }

    // load JSON
    public static List<Transaction> loadTransactions(){
        try{
            File file = new File(FILE_NAME);

            //if file exists then load it
            if(file.exists()){
                //converts JSON file into list of Transactions onjects
                return mapper.readValue(file, new TypeReference<>() {});
            }
        } catch(IOException e){
            System.out.println("Error loading transactions: "+ e.getMessage());
        }

        //return empty list if loading failed or no file exists
        return new java.util.ArrayList<>();
    }
}
