package com.boojet.boot_api.services;

import static com.boojet.boot_api.testutil.TestDataUtil.anIncomePlan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boojet.boot_api.domain.IncomePlan;
import com.boojet.boot_api.repositories.IncomePlanRepository;
import com.boojet.boot_api.services.Impl.IncomePlanServiceImpl;

@ExtendWith(MockitoExtension.class)
public class IncomePlanServiceTest {
    
    @Mock
    IncomePlanRepository incomeRepo;                    //Mocked repository to isolate service layer for testing

    @InjectMocks
    IncomePlanServiceImpl incomePlanServiceImpl;        //Injecting mocked repository into service implementation
    
    IncomePlanService incomePlanService;                //Service under test


    @BeforeEach
    void setUp(){
        incomePlanService = incomePlanServiceImpl;
    }


    @Test
    @DisplayName("Test that isExists method returns true for existing IncomePlan ID")
    void testIsExists_ReturnsTrueForExistingId(){

        IncomePlan plan = anIncomePlan().sourceName("Main Plan").build();
        Long planId = plan.getId();
        Long nonExistentId = 999L;

        //Mocking repository behavior
        when(incomeRepo.existsById(planId)).thenReturn(true);
        when(incomeRepo.existsById(nonExistentId)).thenReturn(false);

        boolean exists = incomePlanService.isExists(planId);
        boolean notExists = incomePlanService.isExists(nonExistentId);

        //verify that repository method was called twice
        verify(incomeRepo).existsById(planId);
        verify(incomeRepo).existsById(nonExistentId);

        //assert that the results are as expected
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }


}
