package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Feedback;
import com.sfms.mf10.domain.FeedbackResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackResponseView;
import com.sfms.mf10.dto.FeedbackDtos.RespondRequest;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.repository.FeedbackRepository;
import com.sfms.mf10.repository.FeedbackResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.Year;
import java.util.UUID;

/** Hiện thực {@link FeedbackResponseService}. TDS-MF10_Backend.md §8.1, BR-143/UC-163. */
@Service
public class FeedbackResponseServiceImpl implements FeedbackResponseService {

    /** UC-163 EX2 — F5 (TestSpec §8.1): ngưỡng 48h chỉ có ở UC file, không BR đứng sau, treo Huy. */
    private static final Duration LATE_THRESHOLD = Duration.ofHours(48);

    private final FeedbackRepository feedbackRepository;
    private final FeedbackResponseRepository responseRepository;
    private final FeedbackService feedbackService;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;

    public FeedbackResponseServiceImpl(FeedbackRepository feedbackRepository,
                                        FeedbackResponseRepository responseRepository,
                                        FeedbackService feedbackService,
                                        ReferenceResolver referenceResolver,
                                        CodeGenerator codeGenerator) {
        this.feedbackRepository = feedbackRepository;
        this.responseRepository = responseRepository;
        this.feedbackService = feedbackService;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional
    public FeedbackResponseView respond(String feedbackCode, RespondRequest req, AuthUser actor) {
        if (actor.role() != RoleKey.SERVICE_CATERERS) {
            throw FbaExceptions.accessDenied();
        }
        if (req.content() == null || req.content().isBlank()) {
            throw FbaExceptions.validationFailed("content", "must not be blank");
        }
        UUID schoolId = feedbackService.resolveSchoolId(actor);
        Feedback fb = feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, feedbackCode)
                .orElseThrow(FbaExceptions::notFound);

        FeedbackResponse response = new FeedbackResponse();
        response.setCode(codeGenerator.next("mf10_feedback_response:" + Year.now().getValue(),
                "FBR-" + Year.now().getValue(), 3));
        response.setSchoolId(schoolId);
        response.setFeedbackId(fb.getId());
        response.setResponderUserId(actor.userId());
        response.setContent(req.content());
        Instant now = Instant.now();
        response.setRespondedAt(now);
        response.setLateResponse(fb.getRating() < 3
                && Duration.between(fb.getSubmittedAt(), now).compareTo(LATE_THRESHOLD) > 0);
        responseRepository.saveAndFlush(response);

        String[] responder = referenceResolver.userById(actor.userId());
        return new FeedbackResponseView(response.getCode(), fb.getCode(), response.getContent(),
                response.getRespondedAt(), response.isLateResponse(), responder == null ? null : responder[2]);
    }
}
