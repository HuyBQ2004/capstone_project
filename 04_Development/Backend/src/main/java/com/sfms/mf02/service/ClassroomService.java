package com.sfms.mf02.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.TeacherRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.domain.AcademicYear;
import com.sfms.mf02.domain.AcademicYearStatus;
import com.sfms.mf02.domain.Classroom;
import com.sfms.mf02.domain.Grade;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomDetailResponse;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomResponse;
import com.sfms.mf02.dto.ClassroomDtos.CreateClassroomRequest;
import com.sfms.mf02.dto.ClassroomDtos.UpdateClassroomRequest;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.mapper.ClassroomMapper;
import com.sfms.mf02.repository.AcademicYearRepository;
import com.sfms.mf02.repository.ClassroomRepository;
import com.sfms.mf02.repository.GradeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * UC-018 Create · UC-090 Update · UC-008 Assign Homeroom Teacher · UC-037/165/166 View
 * (BR-020, BR-024, BR-025, BR-192, BR-193, BR-219, ADR-M02-5).
 */
@Service
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final ClassroomMapper classroomMapper;
    private final ReferenceResolver referenceResolver;

    @PersistenceContext
    private EntityManager em;

    public ClassroomService(ClassroomRepository classroomRepository,
                            AcademicYearRepository academicYearRepository,
                            GradeRepository gradeRepository,
                            ClassroomMapper classroomMapper,
                            ReferenceResolver referenceResolver) {
        this.classroomRepository = classroomRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeRepository = gradeRepository;
        this.classroomMapper = classroomMapper;
        this.referenceResolver = referenceResolver;
    }

    // ───────────────────────── UC-037/166 View List ─────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<ClassroomResponse> list(ListQuery query, AuthUser actor) {
        Page<Classroom> page = classroomRepository.findAll(
                ClassroomSpec.build(query, actor.schoolId(), null), query.toPageable());
        List<ClassroomResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    // ───────────────────────── UC-165 View Detail ─────────────────────────

    @Transactional(readOnly = true)
    public ClassroomDetailResponse getDetail(String code, AuthUser actor) {
        Classroom classroom = requireInScope(code, actor);
        return toDetailResponse(classroom);
    }

    // ───────────────────────── UC-018 Create Classroom ─────────────────────────

    /**
     * BR-192 — chặn khi trường chưa có năm học Active (SCH-081/MSG-081); BR-024/025 — grade/
     * capacity hợp lệ; D5 — trùng {@code id}/{@code code} trong năm học ⇒ 409 SCH-409.
     */
    @Transactional
    public ClassroomResponse create(CreateClassroomRequest request, AuthUser actor) {
        Grade grade = resolveGrade(request.gradeId(), actor.schoolId());
        validateCapacity(request.capacity());

        boolean hasActiveYear = !academicYearRepository
                .findBySchoolIdAndStatus(actor.schoolId(), AcademicYearStatus.ACTIVE).isEmpty();
        if (!hasActiveYear) {
            throw SchExceptions.noActiveAcademicYear();                     // BR-192
        }

        AcademicYear academicYear = academicYearRepository
                .findByCodeAndSchoolId(request.academicYearId(), actor.schoolId())
                .orElseThrow(SchExceptions::notFound);

        String code = (request.id() != null && !request.id().isBlank())
                ? request.id() : request.name().trim();
        if (classroomRepository.existsByAcademicYearIdAndCode(academicYear.getId(), code)) {
            throw SchExceptions.duplicateValue("id", code);                  // D5
        }

        Classroom classroom = new Classroom();
        classroom.setCode(code);
        classroom.setSchoolId(actor.schoolId());
        classroom.setAcademicYearId(academicYear.getId());
        classroom.setName(request.name());
        classroom.setGrade(grade);
        classroom.setCapacity(request.capacity());
        classroom.setReadOnly(false);
        classroomRepository.saveAndFlush(classroom);

        return toResponse(classroom);
    }

    // ───────────────────────── UC-090 Update Classroom ─────────────────────────

    @Transactional
    public ClassroomResponse update(String code, UpdateClassroomRequest request, AuthUser actor) {
        Classroom classroom = requireInScope(code, actor);
        if (classroom.isReadOnly()) {
            throw SchExceptions.classroomReadOnly();                        // BR-193
        }

        if (request.gradeId() != null) {
            classroom.setGrade(resolveGrade(request.gradeId(), classroom.getSchoolId()));
        }
        if (request.capacity() != null) {
            validateCapacity(request.capacity());
            long enrolled = currentEnrollment(classroom.getId());
            if (request.capacity() < enrolled) {
                throw SchExceptions.capacityBelowEnrolled();                 // BR-219
            }
            classroom.setCapacity(request.capacity());
        }
        if (request.name() != null) {
            classroom.setName(request.name());
        }

        classroomRepository.saveAndFlush(classroom);
        return toResponse(classroom);
    }

    // ───────────────────────── UC-008 Assign Homeroom Teacher (BR-020/026) ─────────────────────────

    /**
     * ADR-M02-5 — {@code teacherId} nhận {@code code} hoặc {@code staff_code}, resolve qua
     * {@link ReferenceResolver} (KHÔNG import entity MF01). Sai mã, không phải Teacher, không
     * ACTIVE, hoặc khác trường ⇒ <b>404 SCH-404/MSG-137</b> (không rò thông tin nội bộ MF01).
     */
    @Transactional
    public ClassroomResponse assignHomeroomTeacher(String code, String teacherId, AuthUser actor) {
        Classroom classroom = requireInScope(code, actor);
        if (classroom.isReadOnly()) {
            throw SchExceptions.classroomReadOnly();                        // BR-193
        }

        TeacherRef teacher = referenceResolver.teacherByCode(teacherId).orElseThrow(SchExceptions::notFound);
        if (!"teacher".equals(teacher.roleKey()) || !"ACTIVE".equals(teacher.status())
                || !classroom.getSchoolId().equals(teacher.schoolId())) {
            throw SchExceptions.notFound();                                 // ADR-M02-5
        }

        // BR-020 — 1 GV chỉ chủ nhiệm 1 lớp/năm học: hủy liên kết cũ nếu đang chủ nhiệm lớp khác.
        classroomRepository.findByAcademicYearIdAndHomeroomTeacherId(
                classroom.getAcademicYearId(), teacher.id())
                .filter(other -> !other.getId().equals(classroom.getId()))
                .ifPresent(other -> {
                    throw SchExceptions.teacherAlreadyAssigned();
                });

        classroom.setHomeroomTeacherId(teacher.id());                       // BR-026: audit qua BaseEntity
        classroomRepository.saveAndFlush(classroom);
        return toResponse(classroom);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    /**
     * BR-022 — ngoài phạm vi trường của JWT ⇒ 404, không 403 (chốt 2026-07-31, đóng L7).
     *
     * <p>⚠️ {@code code} chỉ UNIQUE trong 1 năm học (D1/§5.2), không toàn trường — cùng mã lớp
     * có thể lặp lại qua các năm học (VD {@code "3A"} mỗi năm). TDS §9.1 định nghĩa
     * {@code GET/PATCH /classrooms/{id}} không mang tham số năm học, nên khi có nhiều bản ghi
     * trùng {@code code}, ưu tiên bản ghi thuộc năm học đang <b>ACTIVE</b> của trường (trường
     * hợp thường gặp nhất khi thao tác hàng ngày); nếu không có năm ACTIVE, lấy bản ghi tạo
     * gần nhất. Đây là một giả định hợp lý để lấp khoảng trống của TDS — KHÔNG phải quyết định
     * ẩn về nghiệp vụ (chỉ ảnh hưởng độ mơ hồ của path param khi trùng mã, không đổi bất kỳ BR).
     */
    private Classroom requireInScope(String code, AuthUser actor) {
        UUID scopeSchoolId = actor.isSystemAdmin() ? null : actor.schoolId();
        List<Classroom> candidates = scopeSchoolId == null
                ? classroomRepository.findAll().stream().filter(c -> c.getCode().equals(code)).toList()
                : classroomRepository.findBySchoolIdAndCode(scopeSchoolId, code);
        if (candidates.isEmpty()) {
            throw SchExceptions.notFound();
        }
        return candidates.stream()
                .filter(c -> academicYearRepository.findById(c.getAcademicYearId())
                        .map(ay -> ay.getStatus() == AcademicYearStatus.ACTIVE).orElse(false))
                .findFirst()
                .orElse(candidates.get(candidates.size() - 1));
    }

    /**
     * V44 — thay {@code validateGrade(int)} (BR-024 cũ, CHECK 1..12 toàn cục) bằng tồn tại +
     * {@code active=true} + cùng trường của {@link Grade} tham chiếu (422 SCH-024/MSG-109 giữ
     * nguyên mã lỗi cho tương thích ngược, chỉ đổi điều kiện kiểm).
     */
    private Grade resolveGrade(String gradeId, UUID schoolId) {
        UUID id;
        try {
            id = UUID.fromString(gradeId);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw SchExceptions.invalidGrade();
        }
        Grade grade = gradeRepository.findById(id).orElseThrow(SchExceptions::invalidGrade);
        if (!grade.isActive() || !grade.getSchoolId().equals(schoolId)) {
            throw SchExceptions.invalidGrade();
        }
        return grade;
    }

    private void validateCapacity(int capacity) {
        if (capacity > 50) {
            throw SchExceptions.capacityExceeded();                         // BR-025
        }
    }

    /**
     * Sĩ số hiện tại của lớp (BR-219). Việc tồn đọng #1 (00_Common_Conventions.md §0-BIS) —
     * nối vào bảng {@code students} thật của MF03 qua native query (KHÔNG import entity/
     * repository của {@code mf03}, giữ ranh giới module — ADR-M02-5, cùng khuôn
     * {@code common/reference/ReferenceResolver}). Nếu bảng chưa migrate, fallback {@code 0L}
     * thay vì làm vỡ luồng cập nhật capacity.
     */
    private long currentEnrollment(UUID classroomId) {
        try {
            Number count = (Number) em.createNativeQuery(
                            "SELECT COUNT(*) FROM students WHERE classroom_id = :cid AND status = 'ACTIVE'")
                    .setParameter("cid", classroomId)
                    .getSingleResult();
            return count.longValue();
        } catch (RuntimeException ex) {
            return 0L;
        }
    }

    /** Việc tồn đọng #1/#2 — số HS dị ứng đã VERIFIED trong lớp (native query trên MF03). */
    private long allergiesInClassroom(UUID classroomId) {
        try {
            Number count = (Number) em.createNativeQuery(
                            "SELECT COUNT(DISTINCT s.id) FROM students s "
                                    + "JOIN allergy_declarations a ON a.student_id = s.id "
                                    + "WHERE s.classroom_id = :cid AND a.status = 'VERIFIED'")
                    .setParameter("cid", classroomId)
                    .getSingleResult();
            return count.longValue();
        } catch (RuntimeException ex) {
            return 0L;
        }
    }

    private ClassroomResponse toResponse(Classroom classroom) {
        String ayCode = academicYearRepository.findById(classroom.getAcademicYearId())
                .map(AcademicYear::getCode).orElse(null);
        String[] teacher = referenceResolver.userById(classroom.getHomeroomTeacherId());
        String teacherName = teacher == null ? null : teacher[2];
        String teacherPublicId = teacher == null ? null
                : (teacher[1] == null || teacher[1].isBlank() ? teacher[0] : teacher[1]);
        long students = currentEnrollment(classroom.getId());   // Việc tồn đọng #1 — nối MF03
        long allergies = allergiesInClassroom(classroom.getId()); // Việc tồn đọng #2 — nối MF03
        Grade grade = classroom.getGrade();
        String gradeName = grade == null ? null : grade.getName();
        return classroomMapper.toResponse(classroom, gradeName, ayCode, students, allergies,
                teacherName, teacherPublicId);
    }

    private ClassroomDetailResponse toDetailResponse(Classroom classroom) {
        String ayCode = academicYearRepository.findById(classroom.getAcademicYearId())
                .map(AcademicYear::getCode).orElse(null);
        String[] teacher = referenceResolver.userById(classroom.getHomeroomTeacherId());
        String teacherName = teacher == null ? null : teacher[2];
        String teacherPublicId = teacher == null ? null
                : (teacher[1] == null || teacher[1].isBlank() ? teacher[0] : teacher[1]);
        String schoolCode = referenceResolver.schoolCode(classroom.getSchoolId());
        long students = currentEnrollment(classroom.getId());     // Việc tồn đọng #3 — nối MF03
        long allergies = allergiesInClassroom(classroom.getId());
        Grade grade = classroom.getGrade();
        String gradeName = grade == null ? null : grade.getName();
        return classroomMapper.toDetailResponse(classroom, gradeName, ayCode, students, allergies,
                teacherName, teacherPublicId, schoolCode);
    }
}
