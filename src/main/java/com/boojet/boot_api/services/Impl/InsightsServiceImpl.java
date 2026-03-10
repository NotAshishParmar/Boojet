package com.boojet.boot_api.services.Impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boojet.boot_api.domain.Money;
import com.boojet.boot_api.dto.credit.CreditMonthlySummaryResponse;
import com.boojet.boot_api.exceptions.BadRequestException;
import com.boojet.boot_api.repositories.TransactionRepository;
import com.boojet.boot_api.repositories.projections.CreditCardTotalView;
import com.boojet.boot_api.services.InsightsService;

@Service
@Transactional(readOnly = true)
public class InsightsServiceImpl implements InsightsService {

    private final TransactionRepository txRepo;

    public InsightsServiceImpl(TransactionRepository txRepo) {
        this.txRepo = txRepo;
    }

    @Override
    public CreditMonthlySummaryResponse creditMonthly(int year, int month) {

        YearMonth ym;
        try {
            ym = YearMonth.of(year, month);
        } catch (RuntimeException e) {
            throw new BadRequestException("Invalid year/month for credit insights");
        }

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // --- totals ---
        BigDecimal accumulatedBd = txRepo.creditAccumulatedBetween(start, end);
        BigDecimal paidOffBd = txRepo.creditPaidOffBetween(start, end);

        Money accumulated = Money.of(accumulatedBd);
        Money paidOff = Money.of(paidOffBd);
        Money netChange = accumulated.add(paidOff.negate());

        // --- per-card breakdown ---
        List<CreditCardTotalView> accRows = txRepo.creditAccumulatedByCardBetween(start, end);
        List<CreditCardTotalView> payRows = txRepo.creditPaidOffByCardBetween(start, end);

        record AccPay(String name, BigDecimal acc, BigDecimal pay) {}
        Map<Long, AccPay> map = new LinkedHashMap<>();

        for (CreditCardTotalView r : accRows) {
            map.put(r.getAccountId(), new AccPay(r.getAccountName(), r.getTotal(), BigDecimal.ZERO));
        }
        for (CreditCardTotalView r : payRows) {
            AccPay existing = map.get(r.getAccountId());
            if (existing == null) {
                map.put(r.getAccountId(), new AccPay(r.getAccountName(), BigDecimal.ZERO, r.getTotal()));
            } else {
                map.put(r.getAccountId(), new AccPay(existing.name(), existing.acc(), r.getTotal()));
            }
        }

        List<CreditMonthlySummaryResponse.CardBreakdown> byCard = map.entrySet().stream()
                .map(e -> {
                    Long accountId = e.getKey();
                    AccPay v = e.getValue();

                    Money cardAcc = Money.of(v.acc());
                    Money cardPaid = Money.of(v.pay());
                    Money cardNet = cardAcc.add(cardPaid.negate());

                    return new CreditMonthlySummaryResponse.CardBreakdown(
                            accountId,
                            v.name(),
                            cardAcc,
                            cardPaid,
                            cardNet
                    );
                })
                .toList();

        return new CreditMonthlySummaryResponse(
                year,
                month,
                accumulated,
                paidOff,
                netChange,
                byCard
        );
    }
}