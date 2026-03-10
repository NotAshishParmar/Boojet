package com.boojet.boot_api.mappers.Impl;

import org.springframework.stereotype.Component;

import com.boojet.boot_api.mappers.Mapper;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.dto.category.CategoryResponse;

@Component
public class CategoryMapper implements Mapper<Category, CategoryResponse>{

    @Override
    public CategoryResponse mapTo(Category category) {
        
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        String parentCode = category.getParent() != null ? category.getParent().getCode() : null;

        boolean hasChildren = category.getChildren() != null && !category.getChildren().isEmpty();

        return new CategoryResponse(category.getId(), 
                                    category.getCode(), 
                                    category.getName(), 
                                    category.getType(),
                                    category.getEssential(), 
                                    category.getSortOrder(), 
                                    category.isSystem(), 
                                    category.isActive(), 
                                    parentId,
                                    parentCode, 
                                    hasChildren);
    }

    @Override
    public Category mapFrom(CategoryResponse b) {
        //Categories being updated/created via Patch/Create DTOs handled by the service so no need to convert category response to entity
        throw new UnsupportedOperationException("Use CategoryCreateRequest/CategoryPatchRequest for writes");
    }
    
}
