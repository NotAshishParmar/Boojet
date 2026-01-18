package com.boojet.boot_api.services;

import java.util.List;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.IncomePlanNotFoundException;


/**
 * Service contract for managing {@link IncomePlan} records.
 * This service defines the business-level operations for creating and managing
 * income plans in Boojet. Implementations are responsible for validating input
 * and persisting income plans through the repository layer.
 * 
 * <b>Notes:</b>
 * <ul>
 * <li>Callers should provide a valid {@link IncomePlan} that meets the
 * applications constraints.</li>
 * <li>Adding an IncomePlan, updates the expected income and net values </li>
 * </ul>
 */
public interface IncomePlanService {

    //---------CRUD------------//
    /**
     * Creates and persists a new {@link IncomePlan}.
     * 
     * <ul>
     *  <li>{@code plan} must not be {@code null}.</li>
     *  <li>{@code plan.payType} is required. If payType is set to HOURLY then 
     *  {@code plan.hoursPerWeek} is required. Otherwise, {@code null} is acceptable.</li>
     *  <li>{@code plan.amount} must be positive.</li>
     *  <li>{@code transaction.category} is required.</li>
     *  <li>If {@code transaction.effectiveDate} is {@code null}, it is set to
     *  {@code LocalDate.now()}.</li>
     *  <li>If {@code plan.sourceName} is {@code null} or blank, it is set to
     *  {@code "Default Plan"}.</li>
     *  <li>If {@code plan.effectiveTo} is provided, then it must be after  
     *  {@code plan.effectiveFrom} but before {@code LocalDate.now()}.</li>
     * </ul>
     * 
     * 
     * Runs within a transactional context to ensure data integrity. On any runtime
     * exception, the income plan is rolled back (no partial writes).
     * 
     * @param plan the candidate Income Plan to add
     * @return the saved Income Plan with generated ID and any defaults applied
     * @throws BadRequestException if input is {@code null} or fails validation
     */
    IncomePlan createPlan(IncomePlan plan);

    /**
     * Finds and returns a list of all {@code IncomePlan(s)} present in the repository.
     * 
     * @return A {@code List} of all IncomePlans present
     */
    List<IncomePlan> findAllPlans();

    /**
     * Finds a {@link IncomePlan} by its unique ID.
     * 
     * @param id the ID of the IncomePlan to find
     * @return the found incomePlan
     * @throws BadRequestException if the provided ID is not positive or is {@code null}
     * @throws IncomePlanNotFoundException if no IncomePlan with the given ID exists
     */
    IncomePlan findPlan(Long id);

    /**
     * Updates an existing {@link IncomePlan} by replacing all fields with those
     * from the provided {@code incomePlan}.
     * 
     * <ul>
     *  <li>All fields in {@code incomePlan} are used to update the existing
     *  record.</li>
     *  <li>The {@code id} field in {@code incomePlan} is ignored; the provided
     *  {@code id} parameter is used to locate the existing record.</li>
     *  <li>If no existing record is found with the given {@code id}, an
     *  {@link IncomePlanNotFoundException} is thrown.</li>
     * </ul>
     * 
     * 
     * @param id the ID of the IncomePlan to update
     * @param incomePlan the IncomePlan data to update with (all fileds used)
     * @return the updated IncomePlan
     * @throws IncomePlanNotFoundException if no IncomePlan with the given ID exists
     * @throws BadRequestException if input is {@code null} or fails validation
     */
    IncomePlan updatePlanComplete(Long id, IncomePlan incomePlan);

    /**
     * Partially updates an existing {@link IncomePlan} by replacing all non-null
     * fields from the provided {@code incomePlan}.
     * 
     * <ul>
     *  <li>Only non-null fields in {@code incomePlan} are used to update the existing
     *  record.</li>
     *  <li>The {@code id} field in {@code incomePlan} is ignored; the provided
     *  {@code id} parameter is used to locate the existing record.</li>
     *  <li>If no existing record is found with the given {@code id}, an
     *  {@link IncomePlanNotFoundException} is thrown.</li>
     * </ul>
     * 
     * 
     * @param id the ID of the IncomePlan to update
     * @param incomePlan the IncomePlan data to update with (only non-null fields used)
     * @return the updated IncomePlan
     * @throws IncomePlanNotFoundException if no IncomePlan with the given ID exists
     * @throws BadRequestException if input fails validation
     */
    IncomePlan updatePlan (Long id, IncomePlan incomePlan);

    /**
     * Deletes the {@link IncomePlan} with the given ID.
     * 
     * @param id the ID of the IncomePlan to delete
     * @throws IncomePlanNotFoundException if no IncomePlan with the given ID exists
     * @throws BadRequestException if the provided ID is not positive or is {@code null}
     */
    void delete(Long id);
    //-------------------------//

    /**
     * Check if an {@link IncomePlan} exists by its ID.
     * 
     * @param id the ID of the IncomePlan to check
     * @return {@code true} if an IncomePlan with the given ID exists, {@code false} otherwise. Invalid IDs return false.
     */
    boolean isExists(Long id);

    /**
     * Calculates the total expected income for the given month.
     * 
     * "Expected" income is derived from the user's configured income plans
     * (e.g., salary, hourly, bi-weekly) and projected into the requested month.
     * 
     * @param year the calendar year
     * @param month the calendar month
     * @return the expected income total for the month
     * @throws BadRequestException if {@code month} is not within range (1-12)
     */
    public Money getExpectedMonthlyIncome(int year, int month);

    /**
     * Calculates the total actual income for the given month.
     * 
     * "Actual" income is computed from persisted transactions that represent
     * income.
     * 
     * @param year the calendar year
     * @param month the calendar month
     * @return the actual income total for the month
     * @throws BadRequestException if {@code month} is not within range (1-12)
     */
    public Money getActualMonthlyIncome(int year, int month);

    /**
     * Calculates the total actual expenses for the given month.
     * 
     * "Actual" expenses is computed from persisted transactions that do 
     *  not represent income.
     * 
     * @param year the calendar year
     * @param month the calendar month
     * @return the actual income total for the month
     * @throws BadRequestException if {@code month} is not within range (1-12)
     */
    public Money getActualMonthlyExpenses(int year, int month);

    /**
     * Builds a consolidated net report for the given month, combining expected income,
     * actual income, expenses, and net values.
     *
     * @param year the calendar year
     * @param month the calendar month
     * @return a net report containing expected/actual totals and net calculations
     * @throws BadRequestException if {@code month} is not in the range (1-12)
     */
    public NetReport netReport(int year, int month);

     /**
     * Summary report for a given month.
     *
     * @param month human-readable label for the month (e.g., "2026-01" or "Jan 2026")
     * @param expectedIncome total projected income from income plans
     * @param actualIncome total recorded income from transactions
     * @param expenses total recorded expenses from transactions
     * @param netExpected expectedIncome minus expenses
     * @param netActual actualIncome minus expenses
     */
    public record NetReport(
        String month, 
        Money expectedIncome, 
        Money actualIncome,
        Money expenses, 
        Money netExpected, 
        Money netActual
    ){}

}
