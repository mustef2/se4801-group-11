package com.sustainabilitytracker;

import com.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.entities.SustainabilityScore;
import com.sustainabilitytracker.enums.PeriodType;
import com.sustainabilitytracker.repositories.*;
import com.sustainabilitytracker.services.ScoreCalculationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreCalculationServiceTest {

    @Mock
    private EmissionRepository emissionRepository;
    @Mock
    private EnergyRepository energyRepository;
    @Mock
    private WaterRepository waterRepository;
    @Mock
    private WasteRepository wasteRepository;
    @Mock
    private SocialRepository socialRepository;
    @Mock
    private GovernanceRepository governanceRepository;
    @Mock
    private SustainabilityScoreRepository scoreRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private ScoreCalculationService scoreCalculationService;

    @Mock
    private SustainabilityTargetRepository targetRepository;

    @Test
    void calculateAndSaveScore_WithAllData_ShouldReturnCorrectScore() {
        // Mock data
        when(emissionRepository.getTotalCo2(anyLong(), any(), any())).thenReturn(BigDecimal.valueOf(800));
        when(energyRepository.getTotalKwh(anyLong(), any(), any())).thenReturn(BigDecimal.valueOf(5000));
        when(energyRepository.getTotalRenewableKwh(anyLong(), any(), any())).thenReturn(BigDecimal.valueOf(3500));
        when(waterRepository.getTotalConsumedLiters(anyLong(), any(), any())).thenReturn(BigDecimal.valueOf(15000));
        when(waterRepository.getTotalRecycledLiters(anyLong(), any(), any())).thenReturn(BigDecimal.valueOf(9000));
        when(wasteRepository.getTotalKg(anyLong(), any(), any())).thenReturn(BigDecimal.valueOf(2000));
        when(wasteRepository.getTotalRecycledKg(anyLong(), any(), any())).thenReturn(BigDecimal.valueOf(1200));

        when(scoreRepository.save(any(SustainabilityScore.class))).thenAnswer(i -> i.getArgument(0));
        when(companyRepository.findById(1L)).thenReturn(Optional.of(Company.builder().id(1L).build()));

        SustainabilityScore score = scoreCalculationService
                .calculateAndSaveScore(1L,
                        LocalDate.now().minusMonths(1),
                        LocalDate.now(),
                        PeriodType.MONTHLY);

        assertNotNull(score);
        assertNotNull(score.getTotalScore());
        assertNotNull(score.getGrade());
        assertEquals(PeriodType.MONTHLY, score.getPeriodType());
    }

    @Test
    void determineGrade_Score95_ReturnsA() {
        String grade = scoreCalculationService.determineGrade(95.0);
        assertEquals("A", grade);
    }

    @Test
    void determineGrade_Score30_ReturnsF() {
        String grade = scoreCalculationService.determineGrade(30.0);
        assertEquals("F", grade);
    }

    @Test
    void determineGrade_Score75_ReturnsB() {
        String grade = scoreCalculationService.determineGrade(75.0);
        assertEquals("B", grade);
    }
}