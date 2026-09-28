package com.sfms.mf10.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackResponseView;
import com.sfms.mf10.dto.FeedbackDtos.RespondRequest;

/** @version 1.0 — UC-163, BR-143. TDS-MF10_Backend.md §8.1. */
public interface FeedbackResponseService {

    /** Ném validationFailed (UC-163 EX1 — content rỗng); đặt lateResponse=true nếu >48h & rating<3 (EX2). */
    FeedbackResponseView respond(String feedbackCode, RespondRequest req, AuthUser actor);
}
