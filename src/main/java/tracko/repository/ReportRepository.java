package tracko.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import tracko.models.report.Report;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByReportName(String reportName);
    Report findByReportId(String reportId);
    List<Report> findByUserId(String userId);
}    