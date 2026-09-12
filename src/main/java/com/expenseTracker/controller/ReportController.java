package com.expenseTracker.controller;

import com.expenseTracker.dto.CardWiseDTO;
import com.expenseTracker.dto.CategoryWiseDTO;
import com.expenseTracker.dto.DailyReportDTO;
import com.expenseTracker.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "APIs for generating expense reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Get summary report by time period")
    public ResponseEntity<Map<String, Object>> getSummaryReport(
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "filter", required = false) String filter) {

        String effectivePeriod = period != null ? period : filter;
        if (effectivePeriod == null) {
            effectivePeriod = "monthly";
        }

        Map<String, Object> report = reportService.generateSummaryReport(effectivePeriod);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/category")
    @Operation(summary = "Get category-wise expense breakdown")
    public ResponseEntity<List<CategoryWiseDTO>> getCategoryWiseReport() {
        List<CategoryWiseDTO> report = reportService.generateCategoryWiseReport();
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/by-card")
    @Operation(summary = "Get card-wise expense report")
    public ResponseEntity<List<CardWiseDTO>> getCardWiseReport() {
        List<CardWiseDTO> report = reportService.generateCardWiseReport();
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily expense report for date range")
    public ResponseEntity<List<DailyReportDTO>> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyReportDTO> report = reportService.generateDailyReport(startDate, endDate);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}
