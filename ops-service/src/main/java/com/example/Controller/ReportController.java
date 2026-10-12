package com.example.Controller;

import com.example.Dto.ReportResponse;
import com.example.Service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<ReportResponse>> getDailyReports() {
        return ResponseEntity.ok(reportService.getDailyReports().stream()
                .map(ReportResponse::from)
                .toList());
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(ReportResponse.from(reportService.getById(reportId)));
    }
}
