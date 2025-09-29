package com.boojet.boot_api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class Money implements Comparable<Money>{
    
    private BigDecimal amount;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public Money(BigDecimal amount){
        if(amount == null){
            amount = BigDecimal.ZERO;
        }

        this.amount = amount.setScale(2, RM);
    }

    //default constructor so Jackson can serialize
    public Money(){
        this.amount = BigDecimal.ZERO;
    }

    public static Money zero(){
        return new Money(BigDecimal.ZERO);
    }

    public static Money of(BigDecimal v){
        return new Money(v);
    }

    public static Money of(String v){
        return of(new BigDecimal(v));
    }

    public Money add(Money other){
        BigDecimal sum = amount.add(other.asBigDecimal());
        return new Money(sum);
    }

    public Money subtract(Money other){
        BigDecimal sub = amount.subtract(other.asBigDecimal());
        return new Money(sub);
    }

    public Money negate(){
        return new Money(amount.negate());
    }

    public String format(){
        return NumberFormat.getCurrencyInstance().format(amount);
    }


    public BigDecimal asBigDecimal(){
        return amount;
    }

    //for Jackson
    public BigDecimal getAmount(){
        return amount;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.asBigDecimal());
    }

    @Override
    public String toString(){
        return "$"+ amount.toPlainString();
    }

}
