package com.boojet.boot_api.repositories;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.boojet.boot_api.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{

    @Query("""
            select c from Category c
            where c.parent is not null
            order by c.createdAt desc
            """)
    List<Category> findChildCategories();


    @Query("""
            select c from Category c
            where c.parent.id = :id
            order by c.sortOrder asc, c.name asc
            """)
    List<Category> listChildren(@Param("id") Long id);


    @Query("""
            select c from Category c
            where upper(c.parent.code) = :code
            order by 
                case when c.sortOrder is null then 1 else 0 end,
                c.sortOrder asc,
                c.name asc,
                c.code asc
            """)
    List<Category> listChildren(@Param("code") String code);

    boolean existsByCodeIgnoreCase(String code);

    Category findByCodeIgnoreCase(String code);

    boolean existsByUserIdAndCodeIgnoreCaseAndIdNot(Long userId, String code, Long id);

    boolean existsByUserIdAndCodeIgnoreCase(Long userId, String code);

    List<Category> findByEssential(Boolean essential);

    
}
