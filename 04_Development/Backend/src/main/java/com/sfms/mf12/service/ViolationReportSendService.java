package com.sfms.mf12.service;

import com.sfms.mf12.dto.SentReportDtos.SendViolationReportRequest;
import com.sfms.mf12.dto.SentReportDtos.SentReportResponse;

import java.util.UUID;

/** UC-189 — BR-214; kỳ rỗng → MSG-097; thành công → MSG-099. */
public interface ViolationReportSendService {

    SentReportResponse send(UUID schoolId, SendViolationReportRequest request, UUID actorUserId, String actorEmail,
                             String ip);
}
