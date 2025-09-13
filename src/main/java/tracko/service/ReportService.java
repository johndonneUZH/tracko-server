package tracko.service;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tracko.repository.ReportRepository;
import tracko.models.report.Report;
import tracko.models.report.ReportRegister;


@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }
    
    public void createReport(ReportRegister report, String userId, String projectId) {
        Report newReport = new Report();
        newReport.setReportContent(report.getReportContent());
        newReport.setCreatedAt(LocalDateTime.now());
        newReport.setUserId(userId);
        String timestamp = LocalDateTime.now().toString().replace("T", "_").replace(":", "-");
        String reportName = String.format("Project_%s_Report_%s", projectId, timestamp);
        newReport.setReportName(reportName);

        reportRepository.save(newReport);
    }

    public Report getReportById(String reportId) {
        return reportRepository.findById(reportId).orElse(null);
    }

    public List<Report> getReportsByUserId(String userId) {
        return reportRepository.findByUserId(userId);
    }

    public void updateReport(ReportRegister report, String reportId) {
        Report existingReport = reportRepository.findById(reportId).orElse(null);
        if (existingReport != null) {
            existingReport.setReportName(report.getReportContent());
            reportRepository.save(existingReport);
        }
    }
}
