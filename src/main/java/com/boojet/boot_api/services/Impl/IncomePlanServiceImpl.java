package com.boojet.boot_api.services.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.domain.PayType;
import com.boojet.boot_api.domain.User;
import com.boojet.boot_api.dto.incomePlan.IncomePlanCreateRequest;
import com.boojet.boot_api.dto.incomePlan.IncomePlanPatchRequest;
import com.boojet.boot_api.dto.incomePlan.IncomePlanPutRequest;
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

    private static final Long DEFAULT_USER_ID = 1L; //temporary until user management is implemented

    private static int DEFAULT_INCOMEPLAN_COUNTER = 1;
    private static final BigDecimal DEFAULT_ESTIMATED_DEDUCTION_RATE = new BigDecimal("0.2200");


    public IncomePlanServiceImpl(IncomePlanRepository incomePlanRepo, TransactionService transactionService, UserRepository userRepo){
        this.incomePlanRepo = incomePlanRepo;
        this.transactionService = transactionService;
        this.userRepo = userRepo;
    }

    //----------------------------------------CRUD---------------------------------------------------
    @Override
    @Transactional
    public IncomePlan createIncomePlan(IncomePlanCreateRequest req){
        
        User defaultUser = userRepo.getReferenceById(DEFAULT_USER_ID);

        String name = normalizeSourceName(req.sourceName());
        LocalDate effectiveFrom = defaultEffectiveFrom(req.effectiveFrom());
        BigDecimal estimatedDeductionRate = normalizeEstimatedDeductionRate(req.estimatedDeductionRate());

        //validate
        validateEffectiveDates(effectiveFrom, req.effectiveTo());
        validateHoursPerWeek(req.hoursPerWeek(), req.payType());
        requirePayType(req.payType());
        requireAmount(req.amount());

        IncomePlan plan = IncomePlan.builder()
                                    .user(defaultUser)
                                    .sourceName(name)
                                    .payType(req.payType())
                                    .amount(req.amount())
                                    .estimatedDeductionRate(estimatedDeductionRate)
                                    .hoursPerWeek(req.hoursPerWeek())
                                    .effectiveFrom(effectiveFrom)
                                    .effectiveTo(req.effectiveTo())
                                    .build();

        return incomePlanRepo.save(plan);
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
        public IncomePlan putIncomePlan(Long id, IncomePlanPutRequest req){
            validateIncomePlanId(id);

            IncomePlan existing = incomePlanRepo.findById(id)
                                    .orElseThrow(() -> new IncomePlanNotFoundException(id));

            String name = requireSourceName(req.sourceName());
            BigDecimal estimatedDeductionRate = normalizeEstimatedDeductionRate(req.estimatedDeductionRate());

            requireEffectiveFrom(req.effectiveFrom());
            requirePayType(req.payType());
            requireAmount(req.amount());
            validateHoursPerWeek(req.hoursPerWeek(), req.payType());
            validateEffectiveDates(req.effectiveFrom(), req.effectiveTo());

            existing.setSourceName(name);
            existing.setPayType(req.payType());
            existing.setAmount(req.amount());
            existing.setEstimatedDeductionRate(estimatedDeductionRate);
            existing.setHoursPerWeek(req.hoursPerWeek());
            existing.setEffectiveFrom(req.effectiveFrom());
            existing.setEffectiveTo(req.effectiveTo());

            return incomePlanRepo.save(existing);
        }

        @Override
        @Transactional
        public IncomePlan patchIncomePlan(Long id, IncomePlanPatchRequest req){
            validateIncomePlanId(id);

            IncomePlan existing = incomePlanRepo.findById(id)
                                    .orElseThrow(() -> new IncomePlanNotFoundException(id));

            if (req.sourceName() != null) {
                if (req.sourceName().isBlank()) {
                    throw new BadRequestException("Source name must not be blank");
                }
                existing.setSourceName(req.sourceName().trim());
            }

            if (req.payType() != null) {
                existing.setPayType(req.payType());
            }

            if (req.amount() != null) {
                if (!req.amount().isPositive()) {
                    throw new BadRequestException("Amount, if provided, must be a positive number");
                }
                existing.setAmount(req.amount());
            }

            if (req.estimatedDeductionRate() != null) {
                if (req.estimatedDeductionRate().isNull()) {
                    existing.setEstimatedDeductionRate(DEFAULT_ESTIMATED_DEDUCTION_RATE);
                } else if (req.estimatedDeductionRate().isNumber()) {
                    existing.setEstimatedDeductionRate(
                        normalizeEstimatedDeductionRate(req.estimatedDeductionRate().decimalValue())
                    );
                } else {
                    throw new BadRequestException("estimatedDeductionRate must be a number or null");
                }
            }

            if (req.hoursPerWeek() != null) {
                if (req.hoursPerWeek().isNull()) {
                    existing.setHoursPerWeek(null);
                } else if (req.hoursPerWeek().isNumber()) {
                    existing.setHoursPerWeek(req.hoursPerWeek().decimalValue());
                } else {
                    throw new BadRequestException("hoursPerWeek must be a number or null");
                }
            }

            if (req.effectiveFrom() != null) {
                existing.setEffectiveFrom(req.effectiveFrom());
            }

            if (req.effectiveTo() != null) {
                if (req.effectiveTo().isNull()) {
                    existing.setEffectiveTo(null);
                } else if (req.effectiveTo().isTextual()) {
                    try {
                        existing.setEffectiveTo(LocalDate.parse(req.effectiveTo().asText()));
                    } catch (RuntimeException e) {
                        throw new BadRequestException("effectiveTo must be a valid ISO date (yyyy-mm-dd) or null");
                    }
                } else {
                    throw new BadRequestException("effectiveTo must be a date string or null");
                }
            }

            validateEffectiveDates(existing.getEffectiveFrom(), existing.getEffectiveTo());
            validateHoursPerWeek(existing.getHoursPerWeek(), existing.getPayType());

            return incomePlanRepo.save(existing);
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
    public Money getGrossExpectedMonthlyIncome(int year, int month){
        //TODO: scope by User once Boojet allows multiple users
        List<IncomePlan> plans = incomePlanRepo.findAll();
        Money total = Money.zero();

        YearMonth ym = buildYearMonthOrThrow(year, month);

        for(IncomePlan plan : plans){
            total = total.add(plan.calculateGrossMonthlyAmount(ym));
        }

        return total;
    }

    public Money getNetExpectedMonthlyIncome(int year, int month){
        //TODO: scope by User once Boojet allows multiple users
        List<IncomePlan> plans = incomePlanRepo.findAll();
        Money total = Money.zero();

        YearMonth ym = buildYearMonthOrThrow(year, month);

        for(IncomePlan plan : plans){
            total = total.add(plan.calculateNetMonthlyAmount(ym));
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

    //-----------------------------------------------helpers----------------------------------------------------

    private void validateIncomePlanId(Long id){
        if(id == null || id <= 0){
            throw new BadRequestException("Income plan Id must be positive and valid");
        }
    }

    private String normalizeSourceName(String name){
        if(name == null || name.isBlank()){
            String generated = "Income Plan " + DEFAULT_INCOMEPLAN_COUNTER;
            DEFAULT_INCOMEPLAN_COUNTER++;
            return generated;     
        }
        return name.trim();
    }

    private BigDecimal normalizeEstimatedDeductionRate(BigDecimal rate) {
            BigDecimal normalized = rate != null ? rate : DEFAULT_ESTIMATED_DEDUCTION_RATE;

            if (normalized.compareTo(BigDecimal.ZERO) < 0 || normalized.compareTo(BigDecimal.ONE) > 0) {
                throw new BadRequestException("Estimated deduction rate must be between 0 and 1");
            }

            return normalized;
        }

    private String requireSourceName(String name){
        if(name == null || name.isBlank())
            throw new BadRequestException("Name cannot be empty for a PUT");

        return name.trim();
    }

    private void requireEffectiveFrom(LocalDate effectiveFrom){
        if(effectiveFrom == null)
            throw new BadRequestException("Effective From cannot be empty for a PUT");
    }

    private LocalDate defaultEffectiveFrom (LocalDate effectiveFrom){
        return effectiveFrom != null ? effectiveFrom : LocalDate.now();
    }

    private void validateHoursPerWeek (BigDecimal hoursPerWeek, PayType payType){
        if(payType == PayType.HOURLY && hoursPerWeek == null)
            throw new BadRequestException("Hours per week are required for HOURLY PayType");
    }

    private void requirePayType(PayType payType){
        if(payType == null)
            throw new BadRequestException("Pay Type of an Income Plan cannot be null");
    }

    private void requireAmount(Money amount) {
    if (amount == null) {
        throw new BadRequestException("Income plan amount is required");
    }
    if (!amount.isPositive()) {
        throw new BadRequestException("Income plan amount must be positive");
    }
}

    private void validateEffectiveDates(LocalDate effectiveFrom, LocalDate effectiveTo){
        if (effectiveFrom == null) return;
        if (effectiveTo == null) return;

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException("Income plan cannot expire before the Effective From date");
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
