package com.boojet.boot_api.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.boojet.boot_api.domain.IncomePlan;                                           //NOTE: remove dependency on Entity later
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.services.IncomePlanService;
import com.boojet.boot_api.services.IncomePlanService.NetReport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestMapping;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Tag(name = "Income Plan")
@RestController
@RequestMapping("/plan")
public class IncomePlanController {

    private final IncomePlanService incomePlanService;

    public IncomePlanController(IncomePlanService incomePlanService){
        this.incomePlanService = incomePlanService;
    }

    //--------------------------------------------------CRUD-------------------------------------------------------
    @Operation(summary = "Create a new income plan", description = "Creates a new income plan with the provided details.")
    @PostMapping
    public IncomePlan createPlan(@RequestBody IncomePlan incomePlan) {          
        return incomePlanService.createPlan(incomePlan);
    }

    @Operation(summary = "Get all income plans", description = "Retrieve a list of all income plans.")
    @GetMapping
    public List<IncomePlan> getAllPlans(){
        return incomePlanService.findAllPlans();
    }

    @Operation(summary = "Get an income plan by ID", description = "Retrieve the details of an income plan by its ID.")
    @GetMapping("/{id}")
    public IncomePlan getOne(@PathVariable Long id){
        IncomePlan incomePlan = incomePlanService.findPlan(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found"));

        return incomePlan;
    }

    @Operation(summary = "Update an income plan by ID", description = "Update the details of an existing income plan by its ID.")
    @PutMapping("/{id}")
    public IncomePlan updateIncomePlan(@PathVariable Long id, @RequestBody IncomePlan incomePlan){
        if(!incomePlanService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found.");
        }

        IncomePlan updatedPlan = incomePlanService.updatePlan(id, incomePlan);
        return updatedPlan;
    }

    @Operation(summary = "Partially update an income plan by ID", description = "Partially update the details of an existing income plan by its ID.")
    @PatchMapping("/{id}")
    public IncomePlan patchIncomePlan(@PathVariable Long id, @RequestBody IncomePlan incomePlan){
        if(!incomePlanService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found.");
        }

        IncomePlan patchedPlan = incomePlanService.updatePlan(id, incomePlan);
        return patchedPlan;
    }

    @Operation(summary = "Delete an income plan by ID", description = "Delete an existing income plan by its ID.")
    @DeleteMapping("/{id}")
    public void deletePlan(@PathVariable Long id){
        if(!incomePlanService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found.");
        }

        incomePlanService.delete(id);
    }

    //-------------------------------------Reports / Calculations---------------------------------------------

    //Expected Monthly Income 
    @Operation(summary = "Get expected income for a month", description = "Calculate and retrieve the expected income for a specified month and year.")
    @GetMapping("/expected/{year}/{month}")
    public Money expected(@PathVariable int year, @PathVariable int month){
        return incomePlanService.getExpectedMonthlyIncome(YearMonth.of(year, month));
    }

    //net report
    @Operation(summary = "Get net report for a month", description = "Generate a net report for a specified month and year, detailing expectesd vs actual income and expenses. Also includes net expected and actual gain or loss calculations.")
    @GetMapping("/net/{year}/{month}")
    public NetReport net(@PathVariable int year, @PathVariable int month){
        return incomePlanService.netReport(YearMonth.of(year, month));
    }
    
}
