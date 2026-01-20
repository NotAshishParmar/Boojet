package com.boojet.boot_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Represents an application user in Boojet.
 * <p>
 * Users own accounts, transactions, and income plans. This entity is used as the root
 * owner reference for multi-user data separation.
 */
@Data
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    private Long id;

    private String username;

}
