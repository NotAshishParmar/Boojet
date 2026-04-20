package com.boojet.boot_api.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.incomePlan.IncomePlanCreateRequest;
import com.boojet.boot_api.dto.incomePlan.IncomePlanPatchRequest;
import com.boojet.boot_api.dto.incomePlan.IncomePlanPutRequest;
import com.boojet.boot_api.dto.incomePlan.IncomePlanResponse;
import com.boojet.boot_api.mappers.Impl.IncomePlanMapper;
import com.boojet.boot_api.services.IncomePlanService;
import com.boojet.boot_api.dto.analytics.MonthlyNetResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
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
    private final IncomePlanMapper incomePlanMapper;

    public IncomePlanController(IncomePlanService incomePlanService, IncomePlanMapper incomePlanMapper){
        this.incomePlanService = incomePlanService;
        this.incomePlanMapper = incomePlanMapper;
    }

    //--------------------------------------------------CRUD-------------------------------------------------------
    @Operation(summary = "Create a new income plan", description = "Creates a new income plan with the provided details.")
    @PostMapping
    public ResponseEntity<IncomePlanResponse> createPlan(@RequestBody IncomePlanCreateRequest req) {    
        
        IncomePlan plan = incomePlanService.createIncomePlan(req);
        IncomePlanResponse response = incomePlanMapper.mapTo(plan);

        //201 + location
        URI location = URI.create("/plan/" + plan.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get all income plans", description = "Retrieve a list of all income plans.")
    @GetMapping
    public List<IncomePlanResponse> getAllPlans(){
        return incomePlanService.findAllPlans()
                .stream()
                .map(incomePlanMapper::mapTo)
                .toList();
    }

    @Operation(summary = "Get an income plan by ID", description = "Retrieve the details of an income plan by its ID.")
    @GetMapping("/{id}")
    public IncomePlanResponse getOne(@PathVariable Long id){
        IncomePlan incomePlan = incomePlanService.findPlan(id);
        return incomePlanMapper.mapTo(incomePlan);
    }

    @Operation(summary = "Update an income plan by ID", description = "Update the details of an existing income plan by its ID.")
    @PutMapping("/{id}")
    public IncomePlanResponse updateIncomePlan(@PathVariable Long id, @RequestBody IncomePlanPutRequest req){
        IncomePlan updatedPlan = incomePlanService.putIncomePlan(id, req);
        return incomePlanMapper.mapTo(updatedPlan);
    }

    @Operation(summary = "Partially update an income plan by ID", description = "Partially update the details of an existing income plan by its ID.")
    @PatchMapping("/{id}")
    public IncomePlanResponse patchIncomePlan(@PathVariable Long id, @RequestBody IncomePlanPatchRequest req){
        IncomePlan patchedPlan = incomePlanService.patchIncomePlan(id, req);
        return incomePlanMapper.mapTo(patchedPlan);
    }

    @Operation(summary = "Delete an income plan by ID", description = "Delete an existing income plan by its ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id){
        incomePlanService.delete(id);
        return ResponseEntity.noContent().build(); //204 no content
    }

    //-------------------------------------Reports / Calculations---------------------------------------------

    //Expected Monthly Income 
    @Operation(summary = "Get expected income for a month", description = "Calculate and retrieve the expected income for a specified month and year.")
    @GetMapping("/expected/{year}/{month}")
    public Money expected(@PathVariable int year, @PathVariable int month){
        return incomePlanService.getGrossExpectedMonthlyIncome(year, month);
    }
    
}
