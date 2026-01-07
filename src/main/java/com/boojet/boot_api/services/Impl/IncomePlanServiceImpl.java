package com.boojet.boot_api.services.Impl;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.Transaction;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.repositories.IncomePlanRepository;
import com.boojet.boot_api.repositories.UserRepository;
import com.boojet.boot_api.services.IncomePlanService;
import com.boojet.boot_api.services.TransactionService;


@Service
public class IncomePlanServiceImpl implements IncomePlanService{
    
    private final UserRepository userRepo;
    private final IncomePlanRepository incomePlanRepo;
    private final TransactionService transactionService;                    //NOTE: IncomePLanServiceImpl depends on TransactionService
                                                                            //DO NOT CREATE CIRCULAR DEPENDENCY

    private static final Long DEAFULT_USER_ID = 1L; //temporary until user management is implemented



    public IncomePlanServiceImpl(IncomePlanRepository incomePlanRepo, TransactionService transactionService, UserRepository userRepo){
        this.incomePlanRepo = incomePlanRepo;
        this.transactionService = transactionService;
        this.userRepo = userRepo;
    }

    //----------------------------------------CRUD---------------------------------------------------
    @Override
    public IncomePlan createPlan(IncomePlan plan){
        if(plan.getUser() == null){
            User defaultUser = userRepo.getReferenceById(DEAFULT_USER_ID);
            plan.setUser(defaultUser);
        }
        
        return incomePlanRepo.save(plan);
    }

    @Override
    public List<IncomePlan> findAllPlans(){
        return incomePlanRepo.findAll();
    }

    @Override
    public Optional<IncomePlan> findPlan(Long id){
        return incomePlanRepo.findById(id);
    }

    @Override
    public IncomePlan updatePlan(Long id, IncomePlan incomePlan){
        //making sure the new incomePlan has the id corrected to the current incomePlan we are replacing
        incomePlan.setId(id);

        return incomePlanRepo.findById(id).map(existingPlan -> {
            Optional.ofNullable(incomePlan.getUser()).ifPresent(existingPlan::setUser);
            Optional.ofNullable(incomePlan.getSourceName()).ifPresent(existingPlan::setSourceName);
            Optional.ofNullable(incomePlan.getPayType()).ifPresent(existingPlan::setPayType);
            Optional.ofNullable(incomePlan.getAmount()).ifPresent(existingPlan::setAmount);
            Optional.ofNullable(incomePlan.getHoursPerWeek()).ifPresent(existingPlan::setHoursPerWeek);
            Optional.ofNullable(incomePlan.getEffectiveFrom()).ifPresent(existingPlan::setEffectiveFrom);
            Optional.ofNullable(incomePlan.getEffectiveTo()).ifPresent(existingPlan::setEffectiveTo);
            return incomePlanRepo.save(existingPlan);
        }).orElseThrow(() -> new RuntimeException ("Income Plan not found with id " + id));
    }

    @Override
    public void delete(Long id){
        incomePlanRepo.deleteById(id);
    }

    //------------------------------------------------------------------------------------------------

    @Override
    public boolean isExists(Long id){
        return incomePlanRepo.existsById(id);
    }

    
    //combined expected monthly income from all plans
    public Money getExpectedMonthlyIncome(YearMonth ym){
        List<IncomePlan> plans = incomePlanRepo.findAll();
        Money total = Money.zero();

        for(IncomePlan plan : plans){
            total = total.add(plan.calculateMonthlyAmount(ym));
        }

        return total;
    }


    //actual income from transactions in the given month
    public Money getActualMonthlyIncome(YearMonth ym){
        List<Transaction> transactions = transactionService.findTransactionsByMonth(ym);
        Money total = Money.zero();
        for(Transaction t : transactions){
            if(t.isIncome()){
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    //get total monthly expenses
    public Money expenses(YearMonth ym){
        List<Transaction> transactions = transactionService.findTransactionsByMonth(ym);
        Money total = Money.zero();

        for(Transaction t: transactions){
            if(!t.isIncome()){
                total = total.add(t.getAmount());
            }
        }

        return total;
    }

    public NetReport netReport(YearMonth ym){
        Money expenses = expenses(ym);
        Money expectedIncome = getExpectedMonthlyIncome(ym);
        Money actualIncome = getActualMonthlyIncome(ym);

        return new NetReport(ym.toString(), expectedIncome, actualIncome, expenses, 
            expectedIncome.subtract(expenses), actualIncome.subtract(expenses));
    }



}
