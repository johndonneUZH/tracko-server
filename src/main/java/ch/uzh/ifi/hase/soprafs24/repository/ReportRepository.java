package ch.uzh.ifi.hase.soprafs24.repository;
import ch.uzh.ifi.hase.soprafs24.models.report.Report;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByReportName(String reportName);
    Report findByReportId(String reportId);
    List<Report> findByUserId(String userId);
}    