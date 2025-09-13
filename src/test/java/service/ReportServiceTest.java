package service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tracko.repository.ReportRepository;
import tracko.models.report.Report;
import tracko.models.report.ReportRegister;
import tracko.service.ReportService;

class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @Captor
    private ArgumentCaptor<Report> reportCaptor;

    private final String USER_ID = "user-123";
    private final String PROJECT_ID = "project-123";
    private final String REPORT_ID = "report-123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReport_success() {
        ReportRegister reportRegister = new ReportRegister();
        reportRegister.setReportContent("Test report content");

        reportService.createReport(reportRegister, USER_ID, PROJECT_ID);

        verify(reportRepository, times(1)).save(reportCaptor.capture());
        
        Report savedReport = reportCaptor.getValue();
        assertEquals("Test report content", savedReport.getReportContent());
        assertEquals(USER_ID, savedReport.getUserId());
        assertNotNull(savedReport.getCreatedAt());
        
        String reportName = savedReport.getReportName();
        assertTrue(reportName.startsWith("Project_" + PROJECT_ID + "_Report_"));
        assertTrue(reportName.contains("_"));
        assertTrue(reportName.contains("-"));
    }

    @Test
    void getReportById_success() {
        Report expectedReport = new Report();
        expectedReport.setReportContent("Test report content");
        expectedReport.setUserId(USER_ID);
        expectedReport.setReportName("Test Report");
        expectedReport.setCreatedAt(LocalDateTime.now());
        
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(expectedReport));

        Report result = reportService.getReportById(REPORT_ID);

        assertNotNull(result);
        assertEquals(expectedReport.getReportContent(), result.getReportContent());
        assertEquals(expectedReport.getUserId(), result.getUserId());
        assertEquals(expectedReport.getReportName(), result.getReportName());
        assertEquals(expectedReport.getCreatedAt(), result.getCreatedAt());
        
        verify(reportRepository, times(1)).findById(REPORT_ID);
    }

    @Test
    void getReportById_notFound() {
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        Report result = reportService.getReportById(REPORT_ID);

        assertNull(result);
        verify(reportRepository, times(1)).findById(REPORT_ID);
    }

    @Test
    void getReportsByUserId_success() {
        Report report1 = new Report();
        report1.setReportContent("Test report 1");
        report1.setUserId(USER_ID);
        report1.setReportName("Test Report 1");
        report1.setCreatedAt(LocalDateTime.now());

        Report report2 = new Report();
        report2.setReportContent("Test report 2");
        report2.setUserId(USER_ID);
        report2.setReportName("Test Report 2");
        report2.setCreatedAt(LocalDateTime.now());

        List<Report> expectedReports = Arrays.asList(report1, report2);
        
        when(reportRepository.findByUserId(USER_ID)).thenReturn(expectedReports);

        List<Report> results = reportService.getReportsByUserId(USER_ID);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(report1.getReportContent(), results.get(0).getReportContent());
        assertEquals(report2.getReportContent(), results.get(1).getReportContent());
        
        verify(reportRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    void getReportsByUserId_empty() {
        when(reportRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList());

        List<Report> results = reportService.getReportsByUserId(USER_ID);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(reportRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    void updateReport_success() {
        Report existingReport = new Report();
        existingReport.setReportContent("Original content");
        existingReport.setUserId(USER_ID);
        existingReport.setReportName("Original name");
        existingReport.setCreatedAt(LocalDateTime.now());
        
        ReportRegister updatedReportRegister = new ReportRegister();
        updatedReportRegister.setReportContent("Updated content");
        
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.of(existingReport));

        reportService.updateReport(updatedReportRegister, REPORT_ID);

        verify(reportRepository, times(1)).findById(REPORT_ID);
        verify(reportRepository, times(1)).save(reportCaptor.capture());
        
        Report savedReport = reportCaptor.getValue();
        assertEquals("Updated content", savedReport.getReportName());
    }

    @Test
    void updateReport_reportNotFound() {
        ReportRegister updatedReportRegister = new ReportRegister();
        updatedReportRegister.setReportContent("Updated content");
        
        when(reportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        reportService.updateReport(updatedReportRegister, REPORT_ID);

        verify(reportRepository, times(1)).findById(REPORT_ID);
        verify(reportRepository, never()).save(any(Report.class));
    }
}