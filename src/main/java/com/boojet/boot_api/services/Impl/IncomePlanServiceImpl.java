package com.boojet.boot_api.services.Impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.PayType;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.domain.ValidationMode;
import com.boojet.boot_api.repositories.IncomePlanRepository;
import com.boojet.boot_api.repositories.UserRepository;
import com.boojet.boot_api.services.IncomePlanService;
import com.boojet.boot_api.services.TransactionService;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.exceptions.IncomePlanNotFoundException;


@Service
@Transactional(readOnly = true)
public class IncomePlanServiceImpl implements IncomePlanService{
    
    private final UserRepository userRepo;
    private final IncomePlanRepository incomePlanRepo;
    private final TransactionService transactionService;                    //NOTE: IncomePLanServiceImpl depends on TransactionService
                                                                            //DO NOT CREATE CIRCULAR DEPENDENCY

    private static final Long DEAFULT_USER_ID = 1L; //temporary until user management is implemented

    private static int DEFAULT_INCOMEPLAN_COUNTER = 1;


    public IncomePlanServiceImpl(IncomePlanRepository incomePlanRepo, TransactionService transactionService, UserRepository userRepo){
        this.incomePlanRepo = incomePlanRepo;
        this.transactionService = transactionService;
        this.userRepo = userRepo;
    }

    //----------------------------------------CRUD---------------------------------------------------
    @Override
    @Transactional
    public IncomePlan createPlan(IncomePlan plan){
        if(plan.getUser() == null){
            User defaultUser = userRepo.getReferenceById(DEAFULT_USER_ID);
            plan.setUser(defaultUser);
        }

        applyCreateDefaults(plan);
        IncomePlan verifiedIncomePlan = validateIncomePlan(plan, ValidationMode.CREATE);

        return incomePlanRepo.save(verifiedIncomePlan);
    }

    @Override
    public List<IncomePlan> findAllPlans(){
        return incomePlanRepo.findAll();
    }

    @Override
    public IncomePlan findPlan(Long id){

        validateIncomePlanId(id);
        return incomePlanRepo.findById(id).
            orElseThrow(() -> new IncomePlanNotFoundException(id));
    }

    @Override
    @Transactional
    public IncomePlan updatePlanComplete(Long id, IncomePlan incomePlan){
        validateIncomePlanId(id);
        IncomePlan verifiedIncomePlan = validateIncomePlan(incomePlan, ValidationMode.PUT_FULL);
        return updatePlan(id, verifiedIncomePlan);
    }

    @Override
    @Transactional
    public IncomePlan updatePlan(Long id, IncomePlan incomePlan){

        validateIncomePlanId(id);
        IncomePlan verifiedIncomePlan = validateIncomePlan(incomePlan, ValidationMode.PATCH_PARTIAL);

        //making sure the new incomePlan has the id corrected to the current incomePlan we are replacing
        verifiedIncomePlan.setId(id);

        return incomePlanRepo.findById(id).map(existingPlan -> {
            Optional.ofNullable(verifiedIncomePlan.getUser()).ifPresent(existingPlan::setUser);
            Optional.ofNullable(verifiedIncomePlan.getSourceName()).ifPresent(existingPlan::setSourceName);
            Optional.ofNullable(verifiedIncomePlan.getPayType()).ifPresent(existingPlan::setPayType);
            Optional.ofNullable(verifiedIncomePlan.getAmount()).ifPresent(existingPlan::setAmount);
            Optional.ofNullable(verifiedIncomePlan.getHoursPerWeek()).ifPresent(existingPlan::setHoursPerWeek);
            Optional.ofNullable(verifiedIncomePlan.getEffectiveFrom()).ifPresent(existingPlan::setEffectiveFrom);
            Optional.ofNullable(verifiedIncomePlan.getEffectiveTo()).ifPresent(existingPlan::setEffectiveTo);
            return incomePlanRepo.save(existingPlan);
        }).orElseThrow(() -> new IncomePlanNotFoundException(id));
    }

    @Override
    @Transactional
    public void delete(Long id){
        validateIncomePlanId(id);

        if(!incomePlanRepo.existsById(id)){
            throw new IncomePlanNotFoundException(id);
        }
 
        incomePlanRepo.deleteById(id);
    }

    //------------------------------------------------------------------------------------------------

    @Override
    public boolean isExists(Long id){
        return id != null && id > 0 && incomePlanRepo.existsById(id);
    }

    
    //combined expected monthly income from all plans
    public Money getExpectedMonthlyIncome(int year, int month){
        //TODO: scope by User once Boojet allows multiple users
        List<IncomePlan> plans = incomePlanRepo.findAll();
        Money total = Money.zero();

        YearMonth ym = buildYearMonthOrThrow(year, month);

        for(IncomePlan plan : plans){
            total = total.add(plan.calculateMonthlyAmount(ym));
        }

        return total;
    }


    //actual income from transactions in the given month
    public Money getActualMonthlyIncome(int year, int month){
        
        YearMonth ym = buildYearMonthOrThrow(year, month);

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return transactionService.calculateIncomeBetween(start, end);
    }

    //get total monthly expenses
    public Money getActualMonthlyExpenses(int year, int month){
        
        YearMonth ym = buildYearMonthOrThrow(year, month);

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return transactionService.calculateExpensesBetween(start, end);
    }

    public NetReport netReport(int year, int month){
        Money expenses = getActualMonthlyExpenses(year, month);
        Money expectedIncome = getExpectedMonthlyIncome(year, month);
        Money actualIncome = getActualMonthlyIncome(year, month);

        YearMonth ym = buildYearMonthOrThrow(year, month);

        return new NetReport(ym.toString(), expectedIncome, actualIncome, expenses, 
            expectedIncome.subtract(expenses), actualIncome.subtract(expenses));
    }

    //-----------------------------------------------helpers----------------------------------------------------

    private void validateIncomePlanId(Long id){
        if(id == null || id <= 0){
            throw new BadRequestException("Income plan Id must be positive and valid");
        }
    }

    private IncomePlan validateIncomePlan(IncomePlan plan, ValidationMode mode){
        if(plan == null){
            throw new BadRequestException("Income plan must not be null");
        }

        final boolean requireAll = (mode != ValidationMode.PATCH_PARTIAL);


        //if present, amount must be positive (applies to all validation modes)
        if(plan.getAmount() != null && !plan.getAmount().isPositive()){
            throw new BadRequestException("Income plan amount must be a positive number");
        }
        
        if(plan.getEffectiveFrom() != null && plan.getEffectiveTo() != null &&
            plan.getEffectiveFrom().isAfter(plan.getEffectiveTo())){
            throw new BadRequestException("Income plan closing date cannot be in the future relative to effective from date");
        }

        if(plan.getPayType() == PayType.HOURLY && plan.getHoursPerWeek() == null){
            throw new BadRequestException("Hours per week is required for Hourly Pay type");
        }

        if(requireAll){
            if(plan.getUser() == null || plan.getUser().getId() == null){
                throw new BadRequestException("Income plan must be associated with a valid user");
            }
            if(plan.getSourceName() == null || plan.getSourceName().isBlank()){
                throw new BadRequestException("Income plan source name is required");
            }
            if(plan.getPayType() == null){
                throw new BadRequestException("Income plan type is required");
            }
            if(plan.getAmount() == null){
                throw new BadRequestException("Income plan amount is required");
            }
            if(plan.getEffectiveFrom() == null){
                throw new BadRequestException("Income plan effective date is required");
            }
        }

        return plan;
    }

    private void applyCreateDefaults(IncomePlan plan){
        if(plan.getSourceName() == null || plan.getSourceName().isBlank()){
            plan.setSourceName("Income Plan " + DEFAULT_INCOMEPLAN_COUNTER);
            DEFAULT_INCOMEPLAN_COUNTER++;
        }
        if(plan.getEffectiveFrom() == null){
            plan.setEffectiveFrom(LocalDate.now());
        }
    }

    private YearMonth buildYearMonthOrThrow(int year, int month){
        try{
            return YearMonth.of(year, month);
        }catch(RuntimeException e){
            throw new BadRequestException("Invalid input for year and month Income Plan");
        }
    }



}
