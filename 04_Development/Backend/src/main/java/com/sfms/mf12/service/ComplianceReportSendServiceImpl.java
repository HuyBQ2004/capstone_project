package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.iam.service.AuditService;
import com.sfms.mf07.port.ComplianceDataPort;
import com.sfms.mf07.port.ComplianceDataPort.CompliancePeriodData;
import com.sfms.mf07.port.ComplianceDataPort.InspectionBrief;
import com.sfms.mf12.domain.ReportPeriod;
import com.sfms.mf12.domain.ReportType;
import com.sfms.mf12.domain.SentReport;
import com.sfms.mf12.dto.SentReportDtos.SendComplianceReportRequest;
import com.sfms.mf12.dto.SentReportDtos.SentReportResponse;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.repository.SentReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** UC-185 — BR-209: chặn gửi khi kỳ rỗng (AIS-092); MSG-091 khi thành công. */
@Service
public class ComplianceReportSendServiceImpl implements ComplianceReportSendService {

    private static final DateTimeFormatter RANGE_LABEL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ComplianceDataPort complianceDataPort;
    private final SentReportRepository sentReportRepository;
    private final CodeGenerator codeGenerator;
    private final Clock clock;
    private final AuditService auditService;

    public ComplianceReportSendServiceImpl(ComplianceDataPort complianceDataPort,
                                            SentReportRepository sentReportRepository, CodeGenerator codeGenerator,
                                            Clock clock, AuditService auditService) {
        this.complianceDataPort = complianceDataPort;
        this.sentReportRepository = sentReportRepository;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public SentReportResponse send(UUID schoolId, SendComplianceReportRequest request, UUID actorUserId,
                                    String actorEmail, String ip) {
        CompliancePeriodData data = complianceDataPort.kitchenInspectionsIn(schoolId, request.rangeFrom(),
                request.rangeTo());
        if (data.isEmpty()) {
            throw AisExceptions.noComplianceData();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("totalInspections", data.totalInspections());
        payload.put("averageScore", data.averageScore());
        payload.put("passCount", data.passCount());
        payload.put("failCount", data.failCount());
        payload.put("inspections", data.inspections());
        // BR-103 — cả kỳ TRUE chỉ khi MỌI lần kiểm tra trong kỳ đều staffHealthValid=true (1 lần
        // vi phạm là cả kỳ không đạt), quyết định 05/08/2026.
        payload.put("staffHealthValid", data.inspections().stream().allMatch(InspectionBrief::staffHealthValid));
        // Nhận định chuyên môn Y tế — lưu NGUYÊN VĂN, KHÔNG tính lại (khác các field trên).
        payload.put("assessment", request.assessment());

        int year = Year.now(clock).getValue();
        Instant now = clock.instant();
        SentReport report = new SentReport();
        report.setCode(codeGenerator.next("mf12_sent_reports:" + year, "RPTS-" + year, 3));
        report.setSchoolId(schoolId);
        report.setReportType(ReportType.COMPLIANCE);
        report.setPeriod(parsePeriod(request.period()));
        report.setRangeFrom(request.rangeFrom());
        report.setRangeTo(request.rangeTo());
        report.setRangeLabel(RANGE_LABEL.format(request.rangeFrom()) + " – " + RANGE_LABEL.format(request.rangeTo()));
        report.setSentAt(now);
        report.setSentByUserId(actorUserId);
        report.setNotes(request.notes());
        report.setPayload(payload);

        SentReport saved = sentReportRepository.save(report);
        auditService.success(actorEmail, ip, "SEND compliance-report=" + saved.getCode(), "Sent Reports");
        return toResponse(saved, actorEmail);
    }

    /** CF-27 — parse "Weekly"/"Daily" case-insensitive, mặc định WEEKLY. Sai kiểu/giá trị → 422 (D12). */
    static ReportPeriod parsePeriod(String raw) {
        if (raw == null || raw.isBlank()) {
            return ReportPeriod.WEEKLY;
        }
        try {
            return ReportPeriod.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AisExceptions.validationFailed("period", "must be one of DAILY, WEEKLY");
        }
    }

    private static SentReportResponse toResponse(SentReport r, String actorEmail) {
        return new SentReportResponse(r.getCode(), r.getReportType().name(), r.getPeriod().name(),
                r.getRangeLabel(), r.getRangeFrom(), r.getRangeTo(), r.getSentAt(), actorEmail, r.getRecipient(),
                r.getNotes(), r.isConfidential(), r.getFilePdfUrl() != null, r.getFileXlsxUrl() != null,
                r.getPayload());
    }
}
