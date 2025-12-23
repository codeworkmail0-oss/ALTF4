package com.HeatTrackingWeb.ServiceClass;

import com.HeatTrackingWeb.Models.CommunityReport;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class CommunityReportService {

    private final List<CommunityReport> reports = Collections.synchronizedList(new ArrayList<>());

    public CommunityReport createReport(CommunityReport report) {
        report.setId(UUID.randomUUID().toString());
        report.setTimestamp(LocalDateTime.now().toString());
        reports.add(report);
        return report;
    }

    public List<CommunityReport> getAllReports() {
        return new ArrayList<>(reports);
    }
}
