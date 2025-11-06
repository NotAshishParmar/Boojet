package com.boojet.boot_api.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.boojet.boot_api.domain.IncomePlan;                                           //NOTE: remove dependency on Entity later
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.services.IncomePlanService;
import com.boojet.boot_api.services.IncomePlanService.NetReport;

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




@RestController
@RequestMapping("/plan")
public class IncomePlanController {

    private final IncomePlanService incomePlanService;

    public IncomePlanController(IncomePlanService incomePlanService){
        this.incomePlanService = incomePlanService;
    }

    //CRUD
    @PostMapping
    public IncomePlan createPlan(@RequestBody IncomePlan incomePlan) {          
        return incomePlanService.createPlan(incomePlan);
    }

    @GetMapping
    public List<IncomePlan> getAllPlans(){
        return incomePlanService.findAllPlans();
    }

    @GetMapping("/{id}")
    public IncomePlan getOne(@PathVariable Long id){
        IncomePlan incomePlan = incomePlanService.findPlan(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found"));

        return incomePlan;
    }

    @PutMapping("/{id}")
    public IncomePlan updateIncomePlan(@PathVariable Long id, @RequestBody IncomePlan incomePlan){
        if(!incomePlanService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found.");
        }

        IncomePlan updatedPlan = incomePlanService.updatePlan(id, incomePlan);
        return updatedPlan;
    }

    @PatchMapping("/{id}")
    public IncomePlan patchIncomePlan(@PathVariable Long id, @RequestBody IncomePlan incomePlan){
        if(!incomePlanService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found.");
        }

        IncomePlan patchedPlan = incomePlanService.updatePlan(id, incomePlan);
        return patchedPlan;
    }

    @DeleteMapping("/{id}")
    public void deletePlan(@PathVariable Long id){
        if(!incomePlanService.isExists(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income Plan with ID " + id + " not found.");
        }

        incomePlanService.delete(id);
    }

    //Expected Monthly Income 
    @GetMapping("/expected/{year}/{month}")
    public Money expected(@PathVariable int year, @PathVariable int month){
        return incomePlanService.getExpectedMonthlyIncome(YearMonth.of(year, month));
    }

    //net report
    @GetMapping("/net/{year}/{month}")
    public NetReport net(@PathVariable int year, @PathVariable int month){
        return incomePlanService.netReport(YearMonth.of(year, month));
    }

    
    
}
