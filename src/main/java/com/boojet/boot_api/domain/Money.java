package com.boojet.boot_api.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Value object representing a monetary amount used throughout Boojet.
 * <p>
 * Internally stores a {@link BigDecimal} scaled to 2 decimal places using {@link RoundingMode#HALF_UP}.
 * This ensures consistent arithmetic and display for typical currency use cases.
 *
 * <p><b>JSON representation:</b>
 * This type is serialized as a plain JSON number (e.g., {@code 12.34}) via {@link #toJson()},
 * and deserialized from a number via {@link #fromJson(BigDecimal)}.
 *
 * <p><b>Note:</b> This class does not currently store a currency code; it assumes a single currency
 * context for the application.
 */
public class Money implements Comparable<Money>{
    
    private final BigDecimal amount;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    /**
     * Creates a {@code Money} instance from a {@link BigDecimal}.
     * <p>
     * If {@code amount} is {@code null}, it is treated as zero.
     * The stored value is scaled to 2 decimal places using {@link #RM}.
     *
     * @param amount the raw amount (nullable)
     */
    public Money(BigDecimal amount){
        if(amount == null){
            amount = BigDecimal.ZERO;
        }

        this.amount = amount.setScale(2, RM);
    }

    /**
     * Default constructor for Jackson/serialization frameworks.
     * Initializes the amount to zero.
     */
    public Money(){
        this.amount = BigDecimal.ZERO;
    }

    /**
     * Convenience factory for a zero money value.
     *
     * @return {@code Money} with value 0.00
     */
    public static Money zero(){
        return new Money(BigDecimal.ZERO);
    }

    /**
     * Creates a {@code Money} instance from a {@link BigDecimal}.
     * The value is scaled to 2 decimals using {@link #RM}.
     *
     * @param v the amount (nullable)
     * @return a {@code Money} instance
     */
    public static Money of(BigDecimal v){
        return new Money(v);
    }

    /**
     * Creates a {@code Money} instance from a decimal string.
     *
     * @param v decimal string (e.g., "12.34")
     * @return a {@code Money} instance
     * @throws NumberFormatException if {@code v} is not a valid decimal number
     */
    public static Money of(String v){
        return of(new BigDecimal(v));
    }

    /**
     * Returns a new {@code Money} representing {@code this + other}.
     *
     * @param other the other amount to add
     * @return sum as a new {@code Money}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public Money add(Money other){
        BigDecimal sum = amount.add(other.asBigDecimal());
        return new Money(sum);
    }

    /**
     * Returns a new {@code Money} representing {@code this - other}.
     *
     * @param other the other amount to subtract
     * @return difference as a new {@code Money}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    public Money subtract(Money other){
        BigDecimal sub = amount.subtract(other.asBigDecimal());
        return new Money(sub);
    }

    /**
     * Returns a new {@code Money} with the sign flipped.
     *
     * @return negated amount
     */
    public Money negate(){
        return new Money(amount.negate());
    }

    /** @return {@code true} if this amount is less than zero */
    public boolean isNegative(){
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /** @return {@code true} if this amount is greater than zero */
    public boolean isPositive(){
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /** @return {@code true} if this amount equals zero */
    public boolean isZero(){
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Formats the amount using the JVM's default locale currency format.
     * <p>
     * This is for display only; do not use it for persistence or calculations.
     *
     * @return a currency-formatted string (locale-dependent)
     */
    public String format(){
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    /**
     * Serializes this money value as a plain JSON number.
     *
     * @return the numeric amount
     */
    @JsonValue
    public BigDecimal toJson(){
        return amount;
    }

    /**
     * Deserializes a money value from a plain JSON number.
     * <p>
     * The value is scaled to 2 decimals using {@link #RM}.
     *
     * @param value numeric JSON value
     * @return a {@code Money} instance
     */
    @JsonCreator
    public static Money fromJson(BigDecimal value){
        return Money.of(value);
    }

    /**
     * Returns the underlying {@link BigDecimal} amount.
     *
     * @return amount scaled to 2 decimals
     */
    public BigDecimal asBigDecimal(){
        return amount;
    }

    /**
     * Getter primarily for Jackson and frameworks.
     *
     * @return amount scaled to 2 decimals
     */
    public BigDecimal getAmount(){
        return amount;
    }

     /**
     * Compares this amount to another {@code Money} by numeric value.
     *
     * @param other other money value
     * @return comparison result
     * @throws NullPointerException if {@code other} is {@code null}
     */
    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.asBigDecimal());
    }

    /**
     * Compares this {@code Money} instance to another object for value equality.
     * <p>
     * Two {@code Money} objects are considered equal if their underlying {@link BigDecimal}
     * amounts are equal. Since this class normalizes values to scale 2 (see constructors/factories),
     * equality is consistent for values such as {@code 10}, {@code 10.0}, and {@code 10.00}.
     *
     * @param other the object to compare with
     * @return {@code true} if the other object is a {@code Money} with the same amount; otherwise {@code false}
     */
    @Override
    public boolean equals(Object other){
        if(this == other)
            return true;

        if(!(other instanceof Money money))
            return false;

        //amount is normalized to 2 deciamal places
        return Objects.equals(this.amount, money.amount);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     * <p>
     * This enables {@code Money} to be used reliably as a key in hash-based collections
     * such as {@link java.util.HashMap} or {@link java.util.HashSet}.
     *
     * @return hash code for this money value
     */
    @Override
    public int hashCode(){
        return Objects.hash(amount);
    }

    /**
     * Returns a simple string representation (not locale-aware).
     *
     * @return string form like {@code $12.34}
     */
    @Override
    public String toString(){
        return "$"+ amount.toPlainString();
    }

}
