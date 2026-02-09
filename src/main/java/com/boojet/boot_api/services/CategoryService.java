package com.boojet.boot_api.services;

import java.util.List;

import com.boojet.boot_api.controllers.dto.CategoryCreateRequest;
import com.boojet.boot_api.controllers.dto.CategoryPatchRequest;
import com.boojet.boot_api.controllers.dto.CategoryPutRequest;
import com.boojet.boot_api.domain.Category;

public interface CategoryService {
    
    Category createCategory(CategoryCreateRequest req);

    List<Category> findAllCategories();

    List<Category> findAllRootCategories();

    List<Category> listChildren(Long id);

    List<Category> listChildren (String code);

    Category findCategory(Long id);

    Category findCategory(String code);

    Category putCategory(Long id, CategoryPutRequest req);

    Category patchCategory(Long id, CategoryPatchRequest req);

    void delete(Long id);

    boolean isExists(Long id);

    boolean hasChildren(Long id);

    List<Category> listAllEssentialCategories();

    List<Category> listAllNonEssentialCategories();
}
