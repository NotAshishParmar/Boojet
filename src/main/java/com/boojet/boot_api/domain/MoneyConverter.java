package com.boojet.boot_api.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;


/**
 * JPA attribute converter that maps {@link Money} to a {@link BigDecimal} database column and back.
 * <p>
 * With {@code autoApply = true}, JPA will automatically use this converter for all entity attributes
 * of type {@link Money} unless explicitly overridden.
 *
 * <p><b>Persistence format:</b>
 * {@code Money} is stored as a plain {@link BigDecimal} (typically with scale 2).
 * On read, the {@link Money} constructor normalizes the value to scale 2 using the application's
 * rounding rules.
 */
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

    /**
     * Converts a {@link Money} value to its database representation.
     *
     * @param money the money value from the entity (nullable)
     * @return the underlying {@link BigDecimal} amount to store (or {@code null} if {@code money} is {@code null})
     */
    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        return money != null ? money.getAmount() : null;
    }

    /**
     * Converts a database value into a {@link Money} value object.
     *
     * @param dbData the {@link BigDecimal} value read from the database (nullable)
     * @return a {@link Money} instance (or {@code null} if {@code dbData} is {@code null})
     */
    @Override
    public Money convertToEntityAttribute(BigDecimal dbData) {
        return dbData != null ? new Money(dbData) : null;
    }
}
