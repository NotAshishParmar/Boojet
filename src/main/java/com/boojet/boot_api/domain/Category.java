package com.boojet.boot_api.domain;

import java.beans.Transient;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //unique identifier used by API; FOOD, FOOD_GROCERIES, INCOME_PAY
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    //display name for UI (Groceries, Paystub)
    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CategoryType type;

    //only applicable to subcategories like FOOD_GROCERIES. Null for parent categories
    @Column
    private Boolean essential;

    //who's this category's parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    //childrens under this parent
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, name ASC")
    @JsonIgnore
    @Builder.Default
    private Set<Category> children = new HashSet<>();

    //system defined category or user defined
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private boolean system = true;

    //for future as created categories will be specific to the user (multi-user prep)
    @Column(name= "owner_user_id")
    private Long ownerUserId;

    //to allow soft deletes, so historical use of this category can still be displayed in ledger
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_At", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    //this annotation implies that this property is computed but not persisted
    @Transient
    public boolean isLeaf(){
        return children == null || children.isEmpty();
    }

    @Override
    public String toString(){
        return "Category {id = %s, code = '%s', name = '%s', type = '%s'}".formatted(id, code, name, type);
    }

}
