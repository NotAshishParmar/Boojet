package com.boojet.boot_api.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.controllers.dto.CategoryCreateRequest;
import com.boojet.boot_api.controllers.dto.CategoryResponse;
import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.services.CategoryService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Category")
@RestController
@RequestMapping("/category")
public class CategoryController {
    
    private CategoryService categoryService;

    public CategoryController (CategoryService categoryService){
        this.categoryService = categoryService;
    }

    //----------------------------------------------CRUD-------------------------------------------------------

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryCreateRequest req){
        Category cat = categoryService.createCategory(req);
        CategoryResponse response = CategoryResponse.from(cat);

        //ADD MAPPING

        //correct pattern of REST: POST create -> 201 + Location
        URI location = URI.create("/category/" + cat.getId());
        return ResponseEntity.created(location).body(response);
    }




}
