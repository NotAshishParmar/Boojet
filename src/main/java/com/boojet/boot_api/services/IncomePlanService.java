package com.boojet.boot_api.services;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.domain.Money;

public interface IncomePlanService {

    //---------CRUD------------//
    IncomePlan createPlan(IncomePlan plan);
    List<IncomePlan> findAllPlans();
    Optional<IncomePlan> findPlan(Long id);
    IncomePlan updatePlan (Long id, IncomePlan incomePlan);
    void delete(Long id);
    //-------------------------//

    boolean isExists(Long id);

    public Money getExpectedMonthlyIncome(YearMonth ym);
    public Money getActualMonthlyIncome(YearMonth ym);
    public Money getActualMonthlyExpenses(YearMonth ym);
    public com.boojet.boot_api.services.Impl.IncomePlanServiceImpl.NetReport netReport(YearMonth ym);


    public record NetReport(
        String month, 
        Money expectedIncome, 
        Money actualIncome,
        Money expenses, 
        Money netExpected, 
        Money netActual
    ){}

}
