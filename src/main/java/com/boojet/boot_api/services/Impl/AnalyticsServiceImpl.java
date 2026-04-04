package com.boojet.boot_api.services.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.analytics.EssentialVsNonEssentialResponse;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.repositories.projections.EssentialSpendingAggregate;
import com.boojet.boot_api.services.AnalyticsService;


@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService{

    private final TransactionRepository transactionRepo;

    public AnalyticsServiceImpl(TransactionRepository transactionRepo){
        this.transactionRepo = transactionRepo;
    }

    @Override
    public EssentialVsNonEssentialResponse essentialVsNonEssential(LocalDate fromDate, LocalDate toDate) {
        validateDateRange(fromDate, toDate);

        List<EssentialSpendingAggregate> aggregates = transactionRepo.aggregateExpenseByEssential(fromDate, toDate);

        EssentialSpendingAggregate essentialAggregate = aggregates.stream()
                                                        .filter(a -> Boolean.TRUE.equals(a.essential()))
                                                        .findFirst()
                                                        .orElse(new EssentialSpendingAggregate(true, BigDecimal.ZERO, 0L));
                        
        EssentialSpendingAggregate nonEssentialAggregate = aggregates.stream()
                                                        .filter(a -> Boolean.FALSE.equals(a.essential()))
                                                        .findFirst()
                                                        .orElse(new EssentialSpendingAggregate(false, BigDecimal.ZERO, 0L));

        BigDecimal essentialAmount = essentialAggregate.totalAmount();
        BigDecimal nonEssentialAmount = nonEssentialAggregate.totalAmount();

        long essentialCount = essentialAggregate.transactionCount();
        long nonEssentialCount = nonEssentialAggregate.transactionCount();

        BigDecimal totalAmount = essentialAmount.add(nonEssentialAmount);
        long totalCount = essentialCount + nonEssentialCount;
        Math.toIntExact(essentialCount);

        BigDecimal essentialPercentage = percentageOf(essentialAmount, totalAmount);
        BigDecimal nonEssentialPercentage = percentageOf(nonEssentialAmount, totalAmount);

        return new EssentialVsNonEssentialResponse(
            fromDate,
            toDate,
            Money.of(totalAmount),
            Math.toIntExact(totalCount),
            new EssentialVsNonEssentialResponse.SpendingBucketDto(
                Money.of(essentialAmount),
                Math.toIntExact(essentialCount),
                essentialPercentage
            ),
            new EssentialVsNonEssentialResponse.SpendingBucketDto(
                Money.of(nonEssentialAmount),
                Math.toIntExact(nonEssentialCount),
                nonEssentialPercentage
            )
        );
            
    }



    //----------------------------------------------HELPERS--------------------------------------------------

    private void validateDateRange(LocalDate from, LocalDate to){
        if(from == null)
            throw new BadRequestException("Start date cannot be null");
        if(to == null)
            throw new BadRequestException("End date cannot be null");

        if(from.isAfter(to))
            throw new BadRequestException("Start date cannot be after End date");
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return amount
                .multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }
    
}
