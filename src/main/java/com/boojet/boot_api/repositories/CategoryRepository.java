package com.boojet.boot_api.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.boojet.boot_api.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
    
}
