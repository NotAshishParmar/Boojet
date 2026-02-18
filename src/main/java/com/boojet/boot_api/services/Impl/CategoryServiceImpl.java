package com.boojet.boot_api.services.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.controllers.dto.CategoryCreateRequest;
import com.boojet.boot_api.controllers.dto.CategoryPatchRequest;
import com.boojet.boot_api.controllers.dto.CategoryPutRequest;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.CategoryNotFoundException;
import com.boojet.boot_api.repositories.CategoryRepository;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.repositories.UserRepository;
import com.boojet.boot_api.services.CategoryService;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private CategoryRepository categoryRepo;
    private UserRepository userRepo;
    private TransactionRepository transactionRepo;

    private static final Long DEFAULT_USER_ID = 1L; //temporary until user management is implemented


    public CategoryServiceImpl (CategoryRepository categoryRepo, UserRepository userRepo, TransactionRepository transactionRepo){
        this.categoryRepo = categoryRepo;
        this.userRepo = userRepo;
        this.transactionRepo = transactionRepo;
    }

    @Override
    @Transactional
    public Category createCategory(CategoryCreateRequest req) {

        //use default user for now
        User user = userRepo.getReferenceById(DEFAULT_USER_ID);

        String code = normalizeCode(req.code());

        //unique per user
        if(categoryRepo.existsByUserIdAndCodeIgnoreCase(DEFAULT_USER_ID, code)){
            throw new BadRequestException("Category code already exists: " + code);
        }

        Category parent = null;

        if(req.parentId() != null){
            parent = requireParent(user.getId(), req.parentId());
        }

        Category cat = Category.builder()
                                .user(user)
                                .code(code)
                                .name(req.name().trim())
                                .type(req.type())
                                .essential(req.essential())
                                .sortOrder(req.sortOrder())
                                .parent(parent)
                                .system(false)
                                .active(true)
                                .build();

        //keep both sides consistent
        if(parent != null)
            parent.getChildren().add(cat);

        return categoryRepo.save(cat);
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
    public List<Category> listAllCategories() {
        return categoryRepo.listAll(DEFAULT_USER_ID);
    }

    @Override
    public List<Category> listActiveCategories() {
        return categoryRepo.listActive(DEFAULT_USER_ID);
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
        if(code == null || code.isBlank())
            throw new BadRequestException("Category code must not be null or blank");

        String normalized = code.trim().toUpperCase();

        Category cat = categoryRepo.findByCodeIgnoreCase(normalized);

        if(cat == null){
            throw new CategoryNotFoundException("Category with Code " + normalized + " not found!");
        }

        return cat;
    }

    @Override
    @Transactional
    public Category putCategory(Long id, CategoryPutRequest req) {
        validateCategoryId(id);
        
        Category existing = categoryRepo.findById(id)
                                .orElseThrow(() -> new CategoryNotFoundException(id));

        Long userId = existing.getUser().getId();

        String code = normalizeCode(req.code());

        if(categoryRepo.existsByUserIdAndCodeIgnoreCaseAndIdNot(userId, code, id)){
            throw new BadRequestException("Category code already exists: " + code);
        }

        existing.setCode(code);
        existing.setName(req.name().trim());
        existing.setEssential(req.essential());
        existing.setSortOrder((req.sortOrder()));


        //parent update logic
        Category newParent = null;
        if(req.parentId() != null){
            newParent = requireParent(userId, req.parentId());

            if(newParent.getId().equals(existing.getId()))
                throw new BadRequestException("Category cannot be it's own parent");

            if(wouldCreateCycle(existing, newParent))
                throw new BadRequestException("Invalid parent: would create a cycle");
        }

        setParent(existing, newParent);

        return categoryRepo.save(existing);

    }

    @Override
    @Transactional
    public Category patchCategory(Long id, CategoryPatchRequest req) {
        validateCategoryId(id);

        Category existing = categoryRepo.findById(id)
                                .orElseThrow(() -> new CategoryNotFoundException(id));

        Long userId = existing.getUser().getId();

        //code semantics
        if(req.code() != null){
            String code = normalizeCode(req.code());

            //"AndIdNot" is added to prevent the lookup from finding itself and return true always
            if(categoryRepo.existsByUserIdAndCodeIgnoreCaseAndIdNot(userId, code, id)){
                throw new BadRequestException("Category code already exists for this user: "+ code);
            }
            existing.setCode(code);
        }

        //patch basics
        if(req.name() != null)
            existing.setName(req.name().trim());
        if(req.essential() != null)
            existing.setEssential(req.essential());
        if(req.sortOrder() != null)
            existing.setSortOrder(req.sortOrder());

        //parent semantics
        //if request does not include parent then do nothing
        if(req.parentId() != null){
            //parentId included in payload

            //request intends to set parent to null (defining root category)
            if(req.parentId().isNull()){
                //clear parent
                setParent(existing, null);
            }
            //request intends to reassign the parent
            else if(req.parentId().isNumber()){
                Long newParentId = req.parentId().asLong();
                Category newParent = requireParent(userId, newParentId);

                //self reference check
                if(newParent.getId().equals(existing.getId()))
                    throw new BadRequestException("Category cannot be it's own parent!");
                //cycle check
                if(wouldCreateCycle(existing, newParent))
                    throw new BadRequestException("Invalid parent: would create a cycle");

                setParent(existing, newParent);
            }
            //request has been made by an idiot
            else{
                throw new BadRequestException("parentId must be a number or null");
            }
        }

        return categoryRepo.save(existing);

    }

    @Override
    @Transactional
    public void delete(Long id) {
        validateCategoryId(id);

        Category cat = categoryRepo.findById(id)
                        .orElseThrow(() -> new CategoryNotFoundException(id));

        //soft delete if refernced by transactions
        if(transactionRepo.existsByCategoryId(id)){
            cat.setActive(false);
            categoryRepo.save(cat);
            return;
        }

        //hard delete for non-referenced
        categoryRepo.delete(cat);
    }

    @Override
    public boolean isExists(Long id) {
        return id != null && id > 0 && categoryRepo.existsById(id);
    }

    @Override
    public boolean hasChildren(Long id) {
        validateCategoryId(id);

        Category cat = categoryRepo.findById(id)
                            .orElseThrow(() -> new CategoryNotFoundException(id));
        
        return !cat.getChildren().isEmpty();
    }

    @Override
    public List<Category> listAllEssentialCategories() {
        return categoryRepo.findByEssential(true);
    }

    @Override
    public List<Category> listAllNonEssentialCategories() {
        return categoryRepo.findByEssential(false);
    }


    //-----------------------------------------Helpers---------------------------------------------------------

    private String normalizeCode(String code){
        if(code == null)
            throw new BadRequestException("Code is required for Categories");

        String format = code.trim().toUpperCase();

        if(format.isBlank())
            throw new BadRequestException("Code must not be blank");

        return format;
    }

    private Category requireParent(Long userId, Long parentId){
        Category parent = categoryRepo.findById(parentId)
                            .orElseThrow(() -> new BadRequestException("Parent categoryu not found: "+ parentId));

        if(!parent.getUser().getId().equals(userId))
            throw new BadRequestException("Parent must belong to the same user");

        return parent;
    }

    private void setParent(Category child, Category newParent){
        Category oldParent = child.getParent();

        //remove from old
        if(oldParent != null)
            oldParent.getChildren().remove(child);

        //set new parent
        child.setParent(newParent);

        //link parent back to child
        if(newParent != null)
            newParent.getChildren().add(child);
    }

    private boolean wouldCreateCycle(Category existing, Category newParent){
        Category curr = newParent;

        while(curr != null){
            if(curr.getId().equals(existing.getId()))
                return true;

            curr = curr.getParent();
        }
        return false;
    }


    private void validateCategoryId(Long id){
        if(id == null || id <= 0){
            throw new BadRequestException("Category Id must be positive and valid");
        }
    }
    
}
