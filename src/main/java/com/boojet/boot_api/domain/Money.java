package com.boojet.boot_api.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

    public boolean isNegative(){
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero(){
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public String format(){
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    //serialize as a plain number
    @JsonValue
    public BigDecimal toJson(){
        return amount;
    }

    //deserialize from a plain number
    @JsonCreator
    public static Money fromJson(BigDecimal value){
        return Money.of(value);
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
