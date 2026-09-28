package com.sfms.mf12.service;

import com.sfms.mf12.dto.SentReportDtos.SendComplianceReportRequest;
import com.sfms.mf12.dto.SentReportDtos.SentReportResponse;

import java.util.UUID;

/** UC-185 — BR-209; kỳ rỗng → NoDataInPeriodException (MSG-092); thành công → MSG-091. */
public interface ComplianceReportSendService {

    SentReportResponse send(UUID schoolId, SendComplianceReportRequest request, UUID actorUserId, String actorEmail,
                             String ip);
}
