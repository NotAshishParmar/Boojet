package com.boojet.boot_api.services;

import java.util.List;

import com.boojet.boot_api.domain.Category;

public interface CategoryService {
    
    Category createCategory(Category cat);

    List<Category> findAllCategories();

    List<Category> findAllRootCategories();

    List<Category> listChildren(Long id);

    List<Category> listChildren (String code);

    Category findCategory(Long id);

    Category findCategory(String code);

    Category updateCategoryComplete(Long id, Category cat);

    Category updateCategory(Long id, Category cat);

    void delete(Long id);

    boolean isExists(Long id);

    boolean hasChildren(Long id);

    List<Category> listAllEssentialCategories();

    List<Category> listAllNonEssentialCategories();
}
