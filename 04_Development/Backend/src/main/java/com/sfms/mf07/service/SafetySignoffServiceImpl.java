package com.sfms.mf07.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.domain.KitchenInspection;
import com.sfms.mf07.domain.SafetySignoff;
import com.sfms.mf07.domain.StepStatus;
import com.sfms.mf07.dto.SafetySignoffDtos.SignoffRequest;
import com.sfms.mf07.dto.SafetySignoffDtos.SignoffResponse;
import com.sfms.mf07.error.InspExceptions;
import com.sfms.mf07.repository.InspectionSessionRepository;
import com.sfms.mf07.repository.KitchenInspectionRepository;
import com.sfms.mf07.repository.SafetySignoffRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class SafetySignoffServiceImpl implements SafetySignoffService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final SafetySignoffRepository signoffRepository;
    private final KitchenInspectionRepository kitchenInspectionRepository;
    private final InspectionSessionRepository sessionRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public SafetySignoffServiceImpl(SafetySignoffRepository signoffRepository,
                                     KitchenInspectionRepository kitchenInspectionRepository,
                                     InspectionSessionRepository sessionRepository,
                                     ReferenceResolver referenceResolver, CodeGenerator codeGenerator, Clock clock) {
        this.signoffRepository = signoffRepository;
        this.kitchenInspectionRepository = kitchenInspectionRepository;
        this.sessionRepository = sessionRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SignoffResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = actor.schoolId();
        Page<SafetySignoff> page = signoffRepository.findAll(
                (root, cq, cb) -> cb.equal(root.get("schoolId"), schoolId), query.toPageable());
        List<SignoffResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public SignoffResponse signoff(SignoffRequest request, AuthUser actor) {
        if (actor.role() != RoleKey.SCHOOL_ADMIN) {
            throw InspExceptions.accessDenied();
        }
        UUID schoolId = actor.schoolId();
        LocalDate today = LocalDate.now(clock);
        if (signoffRepository.findBySchoolIdAndDate(schoolId, today).isPresent()) {
            throw InspExceptions.duplicateValue("date", today.toString());
        }

        SafetySignoff signoff = new SafetySignoff();
        signoff.setCode(codeGenerator.next("mf07_safety_signoff:" + Year.now(clock).getValue(),
                "SGN-" + Year.now(clock).getValue(), 3));
        signoff.setSchoolId(schoolId);
        signoff.setDate(today);
        signoff.setSignedBy(actor.userId());
        signoff.setSignedAt(clock.instant());
        signoff.setNote(request.note());

        // CF-32 — snapshot căn cứ tại thời điểm ký (không đổi theo dữ liệu về sau).
        Double avgScore = kitchenInspectionRepository.averageScoreToday(schoolId, today);
        signoff.setHygieneScore(avgScore == null ? null : (int) Math.round(avgScore));
        long failedSteps = sessionRepository.countBySchoolIdAndDateAndOverall(schoolId, today, StepStatus.FAILED);
        signoff.setFailedSteps((int) failedSteps);

        signoffRepository.saveAndFlush(signoff);
        return toResponse(signoff);
    }

    private SignoffResponse toResponse(SafetySignoff s) {
        String[] signer = referenceResolver.userById(s.getSignedBy());
        return new SignoffResponse(s.getCode(), s.getDate().toString(), signer == null ? null : signer[2],
                TIMESTAMP.format(s.getSignedAt().atZone(java.time.ZoneOffset.UTC)), s.getNote(),
                s.getHygieneScore(), s.getFailedSteps());
    }
}
