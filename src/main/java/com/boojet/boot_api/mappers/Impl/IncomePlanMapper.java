package com.boojet.boot_api.mappers.Impl;

import org.springframework.stereotype.Component;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.dto.incomePlan.IncomePlanResponse;
import com.boojet.boot_api.mappers.Mapper;

@Component
public class IncomePlanMapper implements Mapper<IncomePlan, IncomePlanResponse>{

    @Override
    public IncomePlanResponse mapTo(IncomePlan plan){

        return new IncomePlanResponse(
            plan.getId(),
            plan.getSourceName(),
            plan.getPayType(),
            plan.getAmount(),
            plan.getHoursPerWeek(),
            plan.getEffectiveFrom(),
            plan.getEffectiveTo()
        );
    }

    @Override
    public IncomePlan mapFrom(IncomePlanResponse ignored) {
        throw new UnsupportedOperationException("Use IncomePlanCreateRequest, IncomePlanPutRequest, IncomePlanPatchRequest for writes.");
    }
    
}
