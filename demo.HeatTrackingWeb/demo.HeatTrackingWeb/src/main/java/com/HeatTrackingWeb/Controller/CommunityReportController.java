package com.HeatTrackingWeb.Controller;

import com.HeatTrackingWeb.Models.CommunityReport;
import com.HeatTrackingWeb.ServiceClass.CommunityReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@CrossOrigin
public class CommunityReportController {

    private final CommunityReportService service;

    public CommunityReportController(CommunityReportService service) {
        this.service = service;
    }

    @PostMapping("/report")
    public CommunityReport createReport(@RequestBody CommunityReport report) {
        return service.createReport(report);
    }

    @GetMapping("/reports")
    public List<CommunityReport> getAllReports() {
        return service.getAllReports();
    }
}
