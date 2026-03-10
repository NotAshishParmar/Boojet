package com.boojet.boot_api.services;

import java.util.List;

import com.boojet.boot_api.domain.Category;
import com.boojet.boot_api.dto.category.CategoryPatchRequest;
import com.boojet.boot_api.dto.category.CategoryCreateRequest;
import com.boojet.boot_api.dto.category.CategoryPutRequest;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.CategoryNotFoundException;

/**
 * Service contract for managing {@link Category} records.
 * This service defines the business-level operations for creating and managing
 * categories in Boojet. Implementations are responsible for validating input
 * and persisting categories through the repository layer.
 *
 * <b>Notes:</b>
 * <ul>
 *   <li>Category codes are unique per user and are normalized to uppercase.</li>
 *   <li>Parent-child relationships are validated to prevent cycles and self-references.</li>
 *   <li>Soft delete is performed if a category is referenced by transactions.</li>
 * </ul>
 */
public interface CategoryService {

    /**
     * Creates and persists a new {@link Category} for the default user.
     *
     * <ul>
     *   <li>{@code req.code} must not be {@code null}, blank, or already exist for the user.</li>
     *   <li>If {@code req.parentId} is provided, the parent must exist and belong to the same user.</li>
     *   <li>Category code is normalized to uppercase and trimmed.</li>
     * </ul>
     *
     * @param req the request object containing category creation details
     * @return the created Category entity
     * @throws BadRequestException if the code is missing, blank, already exists, or if the parent is invalid
     */
    Category createCategory(CategoryCreateRequest req);

    /**
     * Retrieves all categories in the system.
     *
     * @return a list of all {@link Category} entities
     */
    List<Category> findAllCategories();

    /**
     * Retrieves all root categories (categories without a parent).
     *
     * @return a list of root {@link Category} entities
     */
    List<Category> findAllRootCategories();

    /**
     * Lists the direct children of a category specified by its ID.
     *
     * @param id the ID of the parent category
     * @return a list of child {@link Category} entities
     * @throws BadRequestException if the category ID is {@code null} or not positive
     */
    List<Category> listChildren(Long id);

    /**
     * Lists the direct children of a category specified by its code.
     *
     * <ul>
     *   <li>Code is normalized to uppercase and trimmed.</li>
     *   <li>Throws if the code does not exist.</li>
     * </ul>
     *
     * @param code the code of the parent category
     * @return a list of child {@link Category} entities
     * @throws BadRequestException if the code is {@code null} or blank
     * @throws CategoryNotFoundException if the category code does not exist
     */
    List<Category> listChildren(String code);

    /**
     * Retrieves all categories for the default user, including nested ones, in a flat list.
     *
     * @return a list of all {@link Category} entities for the user
     */
    List<Category> listAllCategories();

    /**
     * Retrieves all active categories for the default user.
     *
     * @return a list of active {@link Category} entities
     */
    List<Category> listActiveCategories();

    /**
     * Finds a {@link Category} by its ID.
     *
     * @param id the ID of the category
     * @return the found Category entity
     * @throws BadRequestException if the category ID is {@code null} or not positive
     * @throws CategoryNotFoundException if the category is not found
     */
    Category findCategory(Long id);

    /**
     * Finds a {@link Category} by its code.
     *
     * <ul>
     *   <li>Code is normalized to uppercase and trimmed.</li>
     * </ul>
     *
     * @param code the code of the category
     * @return the found Category entity
     * @throws BadRequestException if the code is {@code null} or blank
     * @throws CategoryNotFoundException if the category is not found
     */
    Category findCategory(String code);

    /**
     * Updates an existing {@link Category} by replacing all fields with those from the provided request.
     *
     * <ul>
     *   <li>All fields in {@code req} are used to update the existing record.</li>
     *   <li>Category code must remain unique for the user.</li>
     *   <li>Parent update is validated to prevent cycles and self-references.</li>
     * </ul>
     *
     * @param id the ID of the category to update
     * @param req the request object containing new category data
     * @return the updated Category entity
     * @throws BadRequestException if the ID or code is invalid, or if the parent is invalid or would create a cycle
     * @throws CategoryNotFoundException if the category is not found
     */
    Category putCategory(Long id, CategoryPutRequest req);

    /**
     * Partially updates an existing {@link Category} with the non-null fields from the provided request.
     *
     * <ul>
     *   <li>Only non-null fields in {@code req} are used to update the existing record.</li>
     *   <li>Parent update is validated to prevent cycles and self-references.</li>
     * </ul>
     *
     * @param id the ID of the category to update
     * @param req the request object containing fields to update
     * @return the updated Category entity
     * @throws BadRequestException if the ID or code is invalid, or if the parent is invalid or would create a cycle
     * @throws CategoryNotFoundException if the category is not found
     */
    Category patchCategory(Long id, CategoryPatchRequest req);

    /**
     * Deletes a {@link Category} by its ID.
     * <ul>
     *   <li>If the category is referenced by transactions, performs a soft delete (sets active to false).</li>
     *   <li>Otherwise, deletes the category from the repository.</li>
     * </ul>
     *
     * @param id the ID of the category to delete
     * @throws BadRequestException if the category ID is invalid
     * @throws CategoryNotFoundException if the category is not found
     */
    void delete(Long id);

    /**
     * Checks if a {@link Category} exists by its ID.
     *
     * @param id the ID of the category
     * @return {@code true} if the category exists, {@code false} otherwise
     */
    boolean isExists(Long id);

    /**
     * Checks if a {@link Category} has any child categories.
     *
     * @param id the ID of the category
     * @return {@code true} if the category has children, {@code false} otherwise
     * @throws BadRequestException if the category ID is invalid
     * @throws CategoryNotFoundException if the category is not found
     */
    boolean hasChildren(Long id);

    /**
     * Retrieves all categories marked as essential.
     *
     * @return a list of essential {@link Category} entities
     */
    List<Category> listAllEssentialCategories();

    /**
     * Retrieves all categories not marked as essential.
     *
     * @return a list of non-essential {@link Category} entities
     */
    List<Category> listAllNonEssentialCategories();
}