package com.sfms.mf03.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.AllergyDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.domain.RequestStatus;
import com.sfms.mf03.domain.Student;
import com.sfms.mf03.domain.StudentParent;
import com.sfms.mf03.dto.AllergyAlertDtos.AllergyAlert;
import com.sfms.mf03.dto.AllergyAlertDtos.AllergyAlertsResponse;
import com.sfms.mf03.dto.StudentDtos.CreateStudentRequest;
import com.sfms.mf03.dto.StudentDtos.ParentInfoRequest;
import com.sfms.mf03.dto.StudentDtos.ParentSummary;
import com.sfms.mf03.dto.StudentDtos.StudentDietSummaryResponse;
import com.sfms.mf03.dto.StudentDtos.StudentResponse;
import com.sfms.mf03.dto.StudentDtos.UpdateStudentRequest;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.mapper.HealthStatusCalculator;
import com.sfms.mf03.mapper.StudentMapper;
import com.sfms.mf03.repository.AllergyDeclarationRepository;
import com.sfms.mf03.repository.ChronicConditionDeclarationRepository;
import com.sfms.mf03.repository.SpecialMealRequestRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/** UC-025/099/147/145. C2 — phạm vi dữ liệu kiểm ở service (không chỉ {@code @PreAuthorize}). */
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final AllergyDeclarationRepository allergyRepository;
    private final ChronicConditionDeclarationRepository chronicRepository;
    private final SpecialMealRequestRepository specialMealRequestRepository;
    private final ParentLinkService parentLinkService;
    private final HealthHistoryService healthHistoryService;
    private final ReferenceResolver refs;
    private final StudentMapper studentMapper;
    private final CodeGenerator codeGenerator;
    private final ClassroomGuard classroomGuard;
    private final StudentCodeAllocator studentCodeAllocator;

    @PersistenceContext
    private EntityManager em;

    public StudentServiceImpl(StudentRepository studentRepository,
                              StudentParentRepository studentParentRepository,
                              AllergyDeclarationRepository allergyRepository,
                              ChronicConditionDeclarationRepository chronicRepository,
                              SpecialMealRequestRepository specialMealRequestRepository,
                              ParentLinkService parentLinkService,
                              HealthHistoryService healthHistoryService,
                              ReferenceResolver refs,
                              StudentMapper studentMapper,
                              CodeGenerator codeGenerator,
                              ClassroomGuard classroomGuard,
                              StudentCodeAllocator studentCodeAllocator) {
        this.studentRepository = studentRepository;
        this.studentParentRepository = studentParentRepository;
        this.allergyRepository = allergyRepository;
        this.chronicRepository = chronicRepository;
        this.specialMealRequestRepository = specialMealRequestRepository;
        this.parentLinkService = parentLinkService;
        this.healthHistoryService = healthHistoryService;
        this.refs = refs;
        this.studentMapper = studentMapper;
        this.codeGenerator = codeGenerator;
        this.classroomGuard = classroomGuard;
        this.studentCodeAllocator = studentCodeAllocator;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudentResponse> list(ListQuery query, AuthUser actor) {
        Page<Student> page = scopedPage(query, actor);
        List<StudentResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    /**
     * Rút gọn cho SERVICE_CATERERS (chốt 2026-08-02) — cùng phạm vi dữ liệu (school-wide,
     * qua nhánh {@code default} của {@link #scopedPage}, giống School Admin/Medical Staff:
     * caterer phục vụ toàn trường, không chỉ 1 lớp) nhưng response tối thiểu hóa, không
     * tên/DOB/PH.
     *
     * <p>⚠️ SỬA 08/08/2026 — Javadoc cũ ở đây khẳng định "SERVICE_CATERERS gắn 1 trường cố định
     * như Teacher/Medical Staff" nên dùng thẳng {@code actor.schoolId()}. Điều đó KHÔNG đúng với
     * tài khoản đăng ký qua hợp tác (chỉ có {@code partnerId}); phạm vi đọc nay đi qua
     * {@link #readScopeSchoolId}.
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudentDietSummaryResponse> dietSummaryList(ListQuery query, AuthUser actor) {
        Page<Student> page = scopedPage(query, actor);
        List<StudentDietSummaryResponse> items = page.getContent().stream().map(this::toDietSummary).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    private Page<Student> scopedPage(ListQuery query, AuthUser actor) {
        UUID classroomScope = null;
        UUID parentScope = null;
        if (actor.role() != null) {
            switch (actor.role()) {
                case TEACHER -> classroomScope = homeroomClassroomOf(actor.userId());
                case PARENT -> parentScope = actor.userId();
                default -> { /* School Admin / Medical Staff / Service Caterers: chỉ schoolId */ }
            }
        }
        // TDS §9.2 — parentId (code, VD "U-0005"): School Admin/Medical Staff lọc theo tài khoản
        // PH. Parent tự động bị khoá về CHÍNH MÌNH ở trên (parentScope), filter parentId của
        // Parent role KHÔNG được phép ghi đè sang PH khác (C2 — chống rò dữ liệu qua param).
        if (parentScope == null) {
            String parentIdParam = query.filters().get("parentId");
            if (parentIdParam != null && !parentIdParam.isBlank()) {
                var resolvedParent = refs.userIdByCode(parentIdParam);
                if (resolvedParent.isEmpty()) {
                    return Page.empty(query.toPageable(org.springframework.data.domain.Sort.unsorted()));
                }
                parentScope = resolvedParent.get();
            }
        }

        String classroomIdParam = query.filters().get("classroomId");
        UUID classroomFilter = classroomScope;
        if (classroomIdParam != null && !classroomIdParam.isBlank()) {
            var resolvedClassroom = refs.classroomIdByCode(classroomIdParam, readScopeSchoolId(actor));
            if (resolvedClassroom.isEmpty()) {
                return Page.empty(query.toPageable(org.springframework.data.domain.Sort.unsorted()));
            }
            classroomFilter = resolvedClassroom.get();
        }
        if (classroomIdParam != null && !classroomIdParam.isBlank() && classroomScope != null && classroomFilter != null
                && !classroomFilter.equals(classroomScope)) {
            // Teacher cố lọc sang lớp khác lớp chủ nhiệm — vẫn giới hạn về lớp của mình (C2).
            classroomFilter = classroomScope;
        }

        List<UUID> classroomIdsMatchingSearch = List.of();
        if (query.hasSearch()) {
            // SEARCH_FIELDS (C1) = students.name, students.code, classrooms.name — tra tên lớp
            // TRƯỚC bằng native query (không JOIN entity Classroom của MF02, ADR-M02-5).
            @SuppressWarnings("unchecked")
            List<Object> ids = em.createNativeQuery(
                            "select id from classrooms where school_id = :sid and name ilike :pattern")
                    .setParameter("sid", readScopeSchoolId(actor))
                    .setParameter("pattern", "%" + com.sfms.common.query.SpecSupport.escapeLike(query.search()) + "%")
                    .getResultList();
            classroomIdsMatchingSearch = ids.stream().map(o -> (UUID) o).toList();
        }

        return studentRepository.findAll(
                StudentSpec.build(query, readScopeSchoolId(actor), classroomFilter, parentScope, classroomIdsMatchingSearch),
                query.toPageable(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "code")));
    }

    /**
     * Trường áp cho ĐƯỜNG ĐỌC — xem {@link com.sfms.common.scope.ActorScope}.
     *
     * <p>Sửa 08/08/2026: Javadoc của {@code listDietSummaries} khẳng định "SERVICE_CATERERS gắn
     * 1 trường cố định như Teacher/Medical Staff" — SAI. Tài khoản Đơn vị suất ăn đăng ký qua
     * hợp tác chỉ có {@code partnerId}, {@code schoolId} LUÔN null, nên
     * {@code GET /students} trả <b>total = 0</b> cho đúng vai cần biết em nào dị ứng.
     * Hệ quả đo được: màn "Phục vụ bữa ăn" của bếp ghi "Lớp 1A chưa có hồ sơ học sinh nào" —
     * tức cảnh báo dị ứng (BR-063/BR-123) mất sạch đầu vào, im lặng.
     *
     * <p>Chỉ áp cho ĐỌC. Đường GHI (tạo/sửa học sinh) là quyền School Admin nên vẫn dùng
     * {@code actor.schoolId()} — tài khoản đó luôn có.
     */
    private UUID readScopeSchoolId(AuthUser actor) {
        return com.sfms.common.scope.ActorScope.schoolIdOf(actor, refs);
    }

    private StudentDietSummaryResponse toDietSummary(Student student) {
        List<AllergyDeclaration> verifiedAllergies = allergyRepository
                .findByStudentIdAndStatus(student.getId(), DeclarationStatus.VERIFIED);
        List<String> allergens = verifiedAllergies.stream().map(AllergyDeclaration::getAllergen).distinct().toList();
        String allergySeverity = verifiedAllergies.stream()
                .map(AllergyDeclaration::getSeverity)
                .max(java.util.Comparator.comparingInt(StudentServiceImpl::severityRank))
                .map(Enum::name).orElse(null);
        String specialDiet = specialMealRequestRepository
                .findFirstByStudentIdAndStatusOrderByRequestedAtDesc(student.getId(), RequestStatus.APPROVED)
                .map(com.sfms.mf03.domain.SpecialMealRequest::getDietaryType).orElse(null);
        return new StudentDietSummaryResponse(student.getCode(), refs.classroomCode(student.getClassroomId()),
                allergens, specialDiet, allergySeverity);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getByCode(String code, AuthUser actor) {
        Student student = requireInScope(code, actor);
        return toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse create(CreateStudentRequest request, AuthUser actor, String ip) {
        if (request.parent() == null || request.parent().email() == null || request.parent().email().isBlank()) {
            throw StuExceptions.missingParent();                             // BR-030 — MF03-TC-002
        }
        UUID classroomId = refs.classroomIdByCode(request.classroomId(), actor.schoolId()).orElseThrow(StuExceptions::notFound);
        assertClassroomInActorSchool(classroomId, actor);                     // C2/BR-022 — chặn xuyên trường
        assertClassroomHasRoom(classroomId);                                  // BR-025 — STU-010/MSG-142

        if (request.id() != null && !request.id().isBlank()
                && studentRepository.existsByCodeAndSchoolId(request.id(), actor.schoolId())) {
            throw StuExceptions.duplicateCode("id", request.id());            // D5
        }

        Student student = new Student();
        student.setCode(request.id() != null && !request.id().isBlank() ? request.id() : nextStudentCode(classroomId));
        student.setRegistrationNo(nextRegistrationNo());
        student.setName(request.name());
        student.setDob(request.dob());
        student.setSchoolId(actor.schoolId());
        student.setClassroomId(classroomId);
        studentRepository.saveAndFlush(student);

        // BR-030 — liên kết (hoặc tạo) đúng 1 PH ngay trong CÙNG transaction của create().
        var link = parentLinkService.linkOrCreateParent(student.getId(), request.parent(), actor, ip);

        healthHistoryService.append(student.getId(), HealthHistoryEventType.KHAI_BAO,
                "Tạo hồ sơ học sinh", actor.userId());

        StudentResponse base = toResponse(student);
        return new StudentResponse(base.id(), base.studentCode(), base.registrationNo(), base.name(),
                base.classroomId(), base.clazz(), base.dob(), base.allergies(), base.specialDiet(),
                base.healthStatus(), base.allergySeverity(), link.parentName(), link.parentCode(),
                base.parents(), base.hasAllergy(), base.hasSpecialDiet(), link.accountCreated());
    }

    @Override
    @Transactional
    public StudentResponse update(String code, UpdateStudentRequest request, AuthUser actor) {
        Student student = requireInScope(code, actor);
        if (request.name() != null) {
            student.setName(request.name());
        }
        if (request.dob() != null) {
            student.setDob(request.dob());
        }
        if (request.classroomId() != null) {
            UUID newClassroomId = refs.classroomIdByCode(request.classroomId(), actor.schoolId()).orElseThrow(StuExceptions::notFound);
            if (!newClassroomId.equals(student.getClassroomId())) {
                assertClassroomInActorSchool(newClassroomId, actor);          // C2/BR-022
                assertClassroomHasRoom(newClassroomId);                       // BR-025
                student.setClassroomId(newClassroomId);
            }
        }
        studentRepository.saveAndFlush(student);
        return toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public AllergyAlertsResponse allergyAlerts(String code, AuthUser actor) {
        Student student = requireInScope(code, actor);
        // BR-123/ADR-M03-2 — CHỈ status=VERIFIED (không PENDING/NEEDS_INFO).
        List<AllergyDeclaration> verified = allergyRepository
                .findByStudentIdAndStatus(student.getId(), DeclarationStatus.VERIFIED);
        List<AllergyAlert> alerts = verified.stream()
                .map(a -> new AllergyAlert(a.getAllergen(), a.getSeverity(), a.getVerifiedAt()))
                .toList();
        boolean hasSevere = verified.stream()
                .anyMatch(a -> a.getSeverity() == com.sfms.common.enums.AllergySeverity.SEVERE);
        String diet = specialMealRequestRepository
                .findFirstByStudentIdAndStatusOrderByRequestedAtDesc(student.getId(), RequestStatus.APPROVED)
                .map(com.sfms.mf03.domain.SpecialMealRequest::getDietaryType).orElse(null);
        return new AllergyAlertsResponse(student.getCode(), student.getName(),
                refs.classroomCode(student.getClassroomId()), alerts, diet, hasSevere);
    }

    @Override
    @Transactional
    public StudentResponse addParent(String code, ParentInfoRequest request, AuthUser actor, String ip) {
        Student student = requireInScope(code, actor);                         // C2 — SCHOOL_ADMIN scope
        parentLinkService.linkOrCreateParent(student.getId(), request, actor, ip);   // STU-201 (BR-201)
        return toResponse(student);
    }

    @Override
    @Transactional
    public void removeParent(String code, String parentCode, AuthUser actor) {
        Student student = requireInScope(code, actor);
        UUID parentUserId = refs.userIdByCode(parentCode).orElseThrow(StuExceptions::notFound);
        parentLinkService.unlink(student.getId(), parentUserId, actor);         // STU-030 nếu là dòng cuối (BR-030)
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudentResponse> listByParentCode(String parentCode, ListQuery query, AuthUser actor) {
        // C2 — Parent chỉ được tự tra chính mình; School Admin/Medical tra bất kỳ PH nào (vẫn bị
        // giới hạn về trường mình qua scopedPage bên dưới, cùng logic GET /students).
        if (actor.role() == com.sfms.common.enums.RoleKey.PARENT) {
            String ownCode = refs.userCode(actor.userId());
            if (ownCode == null || !ownCode.equals(parentCode)) {
                throw StuExceptions.accessDenied();
            }
        }
        var filters = new java.util.LinkedHashMap<>(query.filters());
        filters.put("parentId", parentCode);
        ListQuery scoped = new ListQuery(query.page(), query.pageSize(), query.search(),
                query.dateFrom(), query.dateTo(), query.mealPrefix(), java.util.Map.copyOf(filters));
        return list(scoped, actor);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    private Student requireInScope(String code, AuthUser actor) {
        Student student = studentRepository.findByCode(code)
                .orElseThrow(StuExceptions::notFound);
        enforceScope(student, actor);
        return student;
    }

    /** C2 — Parent chỉ HS liên kết; Teacher chỉ lớp chủ nhiệm; School/Medical chỉ trường mình. */
    private void enforceScope(Student student, AuthUser actor) {
        if (actor.isSystemAdmin()) {
            return;
        }
        if (!actor.belongsToSchool(student.getSchoolId())) {
            throw StuExceptions.accessDenied();
        }
        if (actor.role() == com.sfms.common.enums.RoleKey.PARENT
                && !studentParentRepository.existsByStudentIdAndParentUserId(student.getId(), actor.userId())) {
            throw StuExceptions.accessDenied();
        }
        if (actor.role() == com.sfms.common.enums.RoleKey.TEACHER) {
            UUID homeroomClassroom = homeroomClassroomOf(actor.userId());
            if (homeroomClassroom == null || !homeroomClassroom.equals(student.getClassroomId())) {
                throw StuExceptions.accessDenied();
            }
        }
    }

    private UUID homeroomClassroomOf(UUID teacherUserId) {
        var results = em.createNativeQuery(
                        "select id from classrooms where homeroom_teacher_id = :tid")
                .setParameter("tid", teacherUserId)
                .getResultList();
        return results.isEmpty() ? null : (UUID) results.get(0);
    }

    /**
     * C2/BR-022 — {@code classroomIdByCode} tra CỨU TOÀN CỤC (không lọc theo trường, vì mã lớp
     * chỉ unique trong phạm vi {@code academic_year_id}, không phải toàn hệ thống — V3 DDL).
     * Nếu không kiểm ở đây, School Admin trường A có thể gán HS vào lớp thuộc trường B bằng
     * cách gửi đúng {@code code} của lớp đó (SEC-003 — cross-school isolation). 404, không 403,
     * để không rò thông tin tồn tại của lớp trường khác (cùng nguyên tắc {@code ClassroomService
     * .requireInScope}, MF02, BR-022 chốt 2026-07-31).
     */
    private void assertClassroomInActorSchool(UUID classroomId, AuthUser actor) {
        if (actor.isSystemAdmin()) {
            return;
        }
        var row = em.createNativeQuery("select school_id from classrooms where id = :id")
                .setParameter("id", classroomId).getResultList();
        if (row.isEmpty() || !actor.schoolId().equals(row.get(0))) {
            throw StuExceptions.notFound();
        }
    }

    /**
     * BR-025 (STU-010/MSG-142) + BR-193 (STU-193) — 13/08/2026 chuyển sang {@link ClassroomGuard}
     * để đường nhập Excel hàng loạt dùng CHUNG đúng ràng buộc này; trước đó nó nằm private ở đây
     * nên {@code StudentImportServiceImpl} không gọi và import vượt được sức chứa lớp.
     */
    private void assertClassroomHasRoom(UUID classroomId) {
        classroomGuard.assertHasRoom(classroomId);
    }

    private String nextStudentCode(UUID classroomId) {
        return studentCodeAllocator.next(classroomId);
    }

    private String nextRegistrationNo() {
        return codeGenerator.next("mf03_registration_no:" + Year.now().getValue(),
                "DTD-" + Year.now().getValue(), 4);
    }

    private StudentResponse toResponse(Student student) {
        List<AllergyDeclaration> verifiedAllergies = allergyRepository
                .findByStudentIdAndStatus(student.getId(), DeclarationStatus.VERIFIED);
        List<String> allergens = verifiedAllergies.stream().map(AllergyDeclaration::getAllergen).distinct().toList();
        String allergySeverity = verifiedAllergies.stream()
                .map(AllergyDeclaration::getSeverity)
                .max(java.util.Comparator.comparingInt(StudentServiceImpl::severityRank))
                .map(Enum::name).orElse(null);

        var allergyStatuses = allergyRepository.findByStudentId(student.getId()).stream()
                .map(AllergyDeclaration::getStatus).toList();
        var chronicStatuses = chronicRepository.findByStudentId(student.getId()).stream()
                .map(com.sfms.mf03.domain.ChronicConditionDeclaration::getStatus).toList();
        String healthStatus = HealthStatusCalculator.computeLabel(allergyStatuses, chronicStatuses);

        String specialDiet = specialMealRequestRepository
                .findFirstByStudentIdAndStatusOrderByRequestedAtDesc(student.getId(), RequestStatus.APPROVED)
                .map(com.sfms.mf03.domain.SpecialMealRequest::getDietaryType).orElse(null);

        List<StudentParent> links = studentParentRepository.findByStudentId(student.getId());
        List<ParentSummary> parents = links.stream().map(l -> {
            String[] u = refs.userById(l.getParentUserId());
            return new ParentSummary(u == null ? null : u[0], u == null ? null : u[2],
                    l.getRelationship(), l.isPrimary());
        }).toList();
        ParentSummary primary = parents.stream().filter(ParentSummary::isPrimary).findFirst()
                .orElse(parents.isEmpty() ? null : parents.get(0));

        return studentMapper.toResponse(student, refs.classroomCode(student.getClassroomId()),
                refs.classroomCode(student.getClassroomId()), allergens, specialDiet, healthStatus,
                allergySeverity, primary == null ? null : primary.name(), primary == null ? null : primary.parentId(),
                parents, allergens.isEmpty() ? "no" : "yes", specialDiet == null ? "no" : "yes", null);
    }

    private static int severityRank(com.sfms.common.enums.AllergySeverity s) {
        return switch (s) {
            case MILD -> 0;
            case MODERATE -> 1;
            case SEVERE -> 2;
        };
    }
}
