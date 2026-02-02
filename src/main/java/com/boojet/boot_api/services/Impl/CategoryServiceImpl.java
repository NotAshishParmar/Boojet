package com.boojet.boot_api.services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.domain.ValidationMode;
import com.boojet.boot_api.repositories.CategoryRepository;
import com.boojet.boot_api.repositories.UserRepository;
import com.boojet.boot_api.services.CategoryService;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepo;
    private UserRepository userRepo;

    private static final Long DEFAULT_USER_ID = 1L; //temporary until user management is implemented


    public CategoryServiceImpl (CategoryRepository categoryRepo, UserRepository userRepo){
        this.categoryRepo = categoryRepo;
        this.userRepo = userRepo;
    }

    @Override
    public Category createCategory(Category cat) {
        if(cat.getUser() == null){
            User defaultUser = userRepo.getReferenceById(DEFAULT_USER_ID);
            cat.setUser(defaultUser);
        }
        return null;
    }

    @Override
    public List<Category> findAllCategories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllCategories'");
    }

    @Override
    public List<Category> findAllRootCategories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllRootCategories'");
    }

    @Override
    public List<Category> listChildren(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listChildren'");
    }

    @Override
    public List<Category> listChildren(String code) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listChildren'");
    }

    @Override
    public Category findCategory(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findCategory'");
    }

    @Override
    public Category findCategory(String code) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findCategory'");
    }

    @Override
    public Category updateCategoryComplete(Long id, Category cat) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateCategoryComplete'");
    }

    @Override
    public Category updateCategory(Long id, Category cat) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateCategory'");
    }

    @Override
    public void delete(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public boolean isExists(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isExists'");
    }

    @Override
    public boolean hasChildren(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasChildren'");
    }

    @Override
    public List<Category> listAllEssentialCategories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listAllEssentialCategories'");
    }

    @Override
    public List<Category> listAllNonEssentialCategories() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listAllNonEssentialCategories'");
    }



    private Category validateCategory(Category cat, ValidationMode mode){
        return null;
    }
    
}
