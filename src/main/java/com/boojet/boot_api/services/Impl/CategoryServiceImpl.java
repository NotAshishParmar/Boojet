package com.boojet.boot_api.services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.domain.ValidationMode;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.CategoryNotFoundException;
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
    @Transactional
    public Category createCategory(Category cat) {
        if(cat.getUser() == null){
            User defaultUser = userRepo.getReferenceById(DEFAULT_USER_ID);
            cat.setUser(defaultUser);
        }

        Category verifiedCategory = validateCategory(cat, ValidationMode.CREATE);
        return categoryRepo.save(verifiedCategory);
    }

    @Override
    public List<Category> findAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    public List<Category> findAllRootCategories() {
        return categoryRepo.findChildCategories();
    }

    @Override
    public List<Category> listChildren(Long id) {
        validateCategoryId(id);
        return categoryRepo.listChildren(id);
    }

    @Override
    public List<Category> listChildren(String code) {

        if(code == null || code.isBlank())
            throw new BadRequestException("Category code must not be null or blank");

        String normalized = code.trim().toUpperCase();

        List<Category> children = categoryRepo.listChildren(normalized);

        if(children.isEmpty() && !categoryRepo.existsByCodeIgnoreCase(normalized)){
            throw new CategoryNotFoundException("No category found with code "+ normalized);
        }

        return children;
    }

    @Override
    public Category findCategory(Long id) {
        validateCategoryId(id);
        Category cat = categoryRepo.findById(id).
            orElseThrow(() -> new CategoryNotFoundException(id));

        return cat;
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


    private void validateCategoryId(Long id){
        if(id == null || id <= 0){
            throw new BadRequestException("Category Id must be positive and valid");
        }
    }

    private Category validateCategory(Category cat, ValidationMode mode){
        return null;
    }

    private void applyCreateDefaults(Category cat){

    }
    
}
