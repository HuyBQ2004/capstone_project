package com.sfms.mf02.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.domain.Grade;
import com.sfms.mf02.dto.GradeDtos.CreateGradeRequest;
import com.sfms.mf02.dto.GradeDtos.GradeResponse;
import com.sfms.mf02.dto.GradeDtos.UpdateGradeRequest;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.repository.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CRUD {@code grades} — school-scoped, cùng khuôn CRUD + soft-delete của
 * {@code IngredientServiceImpl} (MF06), phạm vi trường theo {@code ClassroomService}.
 */
@Service
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final ReferenceResolver referenceResolver;

    public GradeServiceImpl(GradeRepository gradeRepository, ReferenceResolver referenceResolver) {
        this.gradeRepository = gradeRepository;
        this.referenceResolver = referenceResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> list(AuthUser actor) {
        return list(actor, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> list(AuthUser actor, String studentCode) {
        UUID schoolId = resolveSchoolId(actor, studentCode);
        List<Grade> grades = schoolId == null
                ? gradeRepository.findAll()
                : gradeRepository.findBySchoolIdOrderBySortOrder(schoolId);
        return grades.stream().map(GradeServiceImpl::toResponse).toList();
    }

    /**
     * Parent không có {@code schoolId} JWT — trước đây rơi thẳng vào nhánh {@code findAll()}
     * (khối của MỌI trường gộp chung, không cách ly). {@code studentCode} (con đang chọn trên
     * màn) cho phép trả đúng khối của trường con đó; thiếu/không khớp thì lùi về trường ĐẦU
     * TIÊN của phụ huynh (hành vi cũ, cùng {@code MenuServiceImpl.resolvePublicSchoolId}) —
     * vẫn tốt hơn không cách ly gì cả.
     */
    private UUID resolveSchoolId(AuthUser actor, String studentCode) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.role() != RoleKey.PARENT) {
            return null;
        }
        if (studentCode != null && !studentCode.isBlank()) {
            var student = referenceResolver.studentByCodeAnySchool(studentCode).orElse(null);
            if (student != null && referenceResolver.isParentLinkedToStudent(actor.userId(), student.id())) {
                return student.schoolId();
            }
        }
        return referenceResolver.firstSchoolIdForParent(actor.userId()).orElse(null);
    }

    @Override
    @Transactional
    public GradeResponse create(CreateGradeRequest request, AuthUser actor) {
        UUID schoolId = actor.schoolId();
        if (gradeRepository.existsBySchoolIdAndName(schoolId, request.name())) {
            throw SchExceptions.duplicateValue("name", request.name());
        }
        Grade grade = new Grade();
        grade.setSchoolId(schoolId);
        grade.setName(request.name());
        grade.setSortOrder(request.sortOrder());
        grade.setActive(true);
        gradeRepository.saveAndFlush(grade);
        return toResponse(grade);
    }

    @Override
    @Transactional
    public GradeResponse update(UUID id, UpdateGradeRequest request, AuthUser actor) {
        Grade grade = requireInScope(id, actor);
        if (request.name() != null && !request.name().isBlank()) {
            if (gradeRepository.existsBySchoolIdAndNameAndIdNot(grade.getSchoolId(), request.name(), id)) {
                throw SchExceptions.duplicateValue("name", request.name());
            }
            grade.setName(request.name());
        }
        if (request.sortOrder() != null) {
            grade.setSortOrder(request.sortOrder());
        }
        if (request.active() != null) {
            grade.setActive(request.active());
        }
        gradeRepository.saveAndFlush(grade);
        return toResponse(grade);
    }

    @Override
    @Transactional
    public void deactivate(UUID id, AuthUser actor) {
        Grade grade = requireInScope(id, actor);
        grade.setActive(false);
        gradeRepository.save(grade);
    }

    /** BR-022 style — ngoài phạm vi trường của JWT ⇒ 404, không 403 (cùng quy ước ClassroomService). */
    private Grade requireInScope(UUID id, AuthUser actor) {
        Grade grade = gradeRepository.findById(id).orElseThrow(SchExceptions::notFound);
        if (!actor.isSystemAdmin() && !grade.getSchoolId().equals(actor.schoolId())) {
            throw SchExceptions.notFound();
        }
        return grade;
    }

    private static GradeResponse toResponse(Grade g) {
        return new GradeResponse(g.getId(), g.getName(), g.getSortOrder(), g.isActive());
    }
}
