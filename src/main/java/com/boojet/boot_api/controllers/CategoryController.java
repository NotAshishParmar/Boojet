package com.boojet.boot_api.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.dto.category.CategoryPatchRequest;
import com.boojet.boot_api.dto.category.CategoryCreateRequest;
import com.boojet.boot_api.dto.category.CategoryPutRequest;
import com.boojet.boot_api.dto.category.CategoryResponse;
import com.boojet.boot_api.mappers.Impl.CategoryMapper;
import com.boojet.boot_api.services.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Category")
@RestController
@RequestMapping("/category")
public class CategoryController {

    private CategoryService categoryService;
    private CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    // ----------------------------------------------CRUD-------------------------------------------------------

    @Operation(summary = "Create a new category", description = "Creates a new category with the provided details.")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody
    CategoryCreateRequest req){

        Category cat = categoryService.createCategory(req);
        CategoryResponse response = categoryMapper.mapTo(cat);

        //correct pattern of REST: POST create -> 201 + Location
        URI location = URI.create("/category/" + cat.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List all categories (admin)", description = "Returns active and inactive categories.")
    @GetMapping
    public List<CategoryResponse> listAll() {
        return categoryService.listAllCategories()
                .stream()
                .map(categoryMapper::mapTo)
                .toList();
    }

    @Operation(summary = "List active categories (picker)", description = "Returns only active categories for picking in transactions.")
    @GetMapping("/active")
    public List<CategoryResponse> listActive() {
        return categoryService.listActiveCategories()
                .stream()
                .map(categoryMapper::mapTo)
                .toList();
    }

    @Operation(summary = "Get a category by ID", description = "Retreive the details for a category by it's ID.")
    @GetMapping("/{id}")
    public CategoryResponse getOne(@PathVariable Long id) {
        return categoryMapper.mapTo(categoryService.findCategory(id));
    }

    @Operation(summary = "Get a category by code", description = "Retreive the details for a category by it's code.")
    @GetMapping("/{code}")
    public CategoryResponse getOne(@PathVariable String code) {
        return categoryMapper.mapTo(categoryService.findCategory(code));
    }

    @Operation(summary = "Partially update a category by ID", description = "Partially update the details of an existing category by it's ID.")
    @PatchMapping("/{id}")
    public CategoryResponse patchCategory(@PathVariable Long id, @RequestBody CategoryPatchRequest req) {
        return categoryMapper.mapTo(categoryService.patchCategory(id, req));
    }

    @Operation(summary = "Update a category by ID", description = "Update the details of an existing category by its ID.")
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @RequestBody CategoryPutRequest req) {
        return categoryMapper.mapTo(categoryService.putCategory(id, req));
    }

    @Operation(summary = "Delete a category by ID", description = "Delete an existing category by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // ---------------------------------------------------------------------------------------------------------------

    @Operation(summary = "List all children of Category by code", description = "Retreive an ordered list of all children of an existing category by it's code. Order determined by the sortOrder value of Category.")
    @GetMapping("/{code}/children")
    public List<CategoryResponse> listChildren(@PathVariable String code) {
        return categoryService.listChildren(code)
                .stream()
                .map(categoryMapper::mapTo)
                .toList();
    }

    @Operation(summary = "List all root categories", description = "Retreive an ordered list of all root (parent) categories. Order determined by sortOrder value of Category.")
    @GetMapping("/roots")
    public List<CategoryResponse> listRootCategories() {
        return categoryService.findAllRootCategories()
                .stream()
                .map(categoryMapper::mapTo)
                .toList();
    }

}
