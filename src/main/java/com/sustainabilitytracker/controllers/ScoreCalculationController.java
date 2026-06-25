package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.entities.SustainabilityScore;
import com.sustainabilitytracker.enums.PeriodType;
import com.sustainabilitytracker.services.ScoreCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreCalculationController {

    private final ScoreCalculationService scoreCalculationService;

    // CALCULATE & SAVE SCORE
    @PostMapping("/calculate")
    public ResponseEntity<SustainabilityScore> calculateAndSaveScore(
            @RequestParam Long companyId,
            @RequestParam LocalDate periodStart,
            @RequestParam LocalDate periodEnd,
            @RequestParam(required = false, defaultValue = "MONTHLY") PeriodType periodType) {

        SustainabilityScore score = scoreCalculationService
                .calculateAndSaveScore(companyId, periodStart, periodEnd, periodType);

        return ResponseEntity.ok(score);
    }

    // GET LATEST SCORE
    @GetMapping("/latest/{companyId}")
    public ResponseEntity<SustainabilityScore> getLatestScore(@PathVariable Long companyId) {
        SustainabilityScore score = scoreCalculationService.getLatestScore(companyId);
        return ResponseEntity.ok(score);
    }

    // GET SCORE HISTORY
    @GetMapping("/history/{companyId}")
    public ResponseEntity<List<SustainabilityScore>> getScoreHistory(@PathVariable Long companyId) {
        List<SustainabilityScore> history = scoreCalculationService.getScoreHistory(companyId);
        return ResponseEntity.ok(history);
    }
}
