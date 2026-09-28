package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.iam.service.AuditService;
import com.sfms.mf07.port.ComplianceDataPort;
import com.sfms.mf07.port.ComplianceDataPort.ViolationPeriodData;
import com.sfms.mf12.domain.ReportPeriod;
import com.sfms.mf12.domain.ReportType;
import com.sfms.mf12.domain.SentReport;
import com.sfms.mf12.dto.SentReportDtos.SendViolationReportRequest;
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

/** UC-189 — BR-214: chặn gửi khi kỳ rỗng (AIS-097); MSG-099 khi thành công. */
@Service
public class ViolationReportSendServiceImpl implements ViolationReportSendService {

    private static final DateTimeFormatter RANGE_LABEL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ComplianceDataPort complianceDataPort;
    private final SentReportRepository sentReportRepository;
    private final CodeGenerator codeGenerator;
    private final Clock clock;
    private final AuditService auditService;

    public ViolationReportSendServiceImpl(ComplianceDataPort complianceDataPort,
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
    public SentReportResponse send(UUID schoolId, SendViolationReportRequest request, UUID actorUserId,
                                    String actorEmail, String ip) {
        ViolationPeriodData data = complianceDataPort.violationsIn(schoolId, request.rangeFrom(), request.rangeTo());
        if (data.isEmpty()) {
            throw AisExceptions.noViolationData();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("totalViolations", data.totalViolations());
        payload.put("violations", data.violations());

        int year = Year.now(clock).getValue();
        Instant now = clock.instant();
        SentReport report = new SentReport();
        report.setCode(codeGenerator.next("mf12_sent_reports:" + year, "RPTS-" + year, 3));
        report.setSchoolId(schoolId);
        report.setReportType(ReportType.VIOLATION_SUMMARY);
        report.setPeriod(ComplianceReportSendServiceImpl.parsePeriod(request.period()));
        report.setRangeFrom(request.rangeFrom());
        report.setRangeTo(request.rangeTo());
        report.setRangeLabel(RANGE_LABEL.format(request.rangeFrom()) + " – " + RANGE_LABEL.format(request.rangeTo()));
        report.setSentAt(now);
        report.setSentByUserId(actorUserId);
        report.setNotes(request.notes());
        report.setPayload(payload);

        SentReport saved = sentReportRepository.save(report);
        auditService.success(actorEmail, ip, "SEND violation-report=" + saved.getCode(), "Sent Reports");
        return new SentReportResponse(saved.getCode(), saved.getReportType().name(), saved.getPeriod().name(),
                saved.getRangeLabel(), saved.getRangeFrom(), saved.getRangeTo(), saved.getSentAt(), actorEmail,
                saved.getRecipient(), saved.getNotes(), saved.isConfidential(), false, false, saved.getPayload());
    }
}
