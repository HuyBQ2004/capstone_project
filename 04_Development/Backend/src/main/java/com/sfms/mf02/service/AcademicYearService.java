package com.sfms.mf02.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;

import java.time.LocalDate;
import com.sfms.mf02.domain.AcademicYear;
import com.sfms.mf02.domain.AcademicYearStatus;
import com.sfms.mf02.domain.Classroom;
import com.sfms.mf02.dto.AcademicYearDtos.AcademicYearResponse;
import com.sfms.mf02.dto.AcademicYearDtos.CreateAcademicYearRequest;
import com.sfms.mf02.dto.AcademicYearDtos.PromoteStudentsRequest;
import com.sfms.mf02.dto.AcademicYearDtos.PromotionResult;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.mapper.AcademicYearMapper;
import com.sfms.mf02.repository.AcademicYearRepository;
import com.sfms.mf02.repository.ClassroomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * UC-174 Create · UC-175 Activate/Close · UC-176 View List · UC-177 Promote
 * (BR-191, BR-192, BR-193, BR-194, BR-195, ADR-M02-4).
 */
@Service
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final ClassroomRepository classroomRepository;
    private final AcademicYearMapper mapper;

    @PersistenceContext
    private EntityManager em;

    public AcademicYearService(AcademicYearRepository academicYearRepository,
                               ClassroomRepository classroomRepository,
                               AcademicYearMapper mapper) {
        this.academicYearRepository = academicYearRepository;
        this.classroomRepository = classroomRepository;
        this.mapper = mapper;
    }

    // ───────────────────────── UC-176 View List (BR-195) ─────────────────────────

    /** BR-195 — Archived vẫn truy vấn được (read-only) — KHÔNG lọc ẩn theo status. */
    @Transactional(readOnly = true)
    public PagedResponse<AcademicYearResponse> list(ListQuery query, AuthUser actor) {
        UUID scope = actor.isSystemAdmin() ? null : actor.schoolId();
        Page<AcademicYear> page = academicYearRepository.findAll(
                AcademicYearSpec.build(query, scope), query.toPageable());
        List<AcademicYearResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    /**
     * UC-176 chi tiết — bổ sung 2026-08-02 (thiếu so với SchoolController/ClassroomController).
     * §16: System Admin đọc mọi trường; School Admin chỉ trường của chính JWT (BR-022) —
     * cross-school ⇒ 404 SCH-404, không phải 403 (đồng nhất pattern L7 đã chốt).
     */
    @Transactional(readOnly = true)
    public AcademicYearResponse get(String code, AuthUser actor) {
        AcademicYear year = actor.isSystemAdmin()
                ? academicYearRepository.findByCode(code).orElseThrow(SchExceptions::notFound)
                : academicYearRepository.findByCodeAndSchoolId(code, actor.schoolId())
                        .orElseThrow(SchExceptions::notFound);
        return toResponse(year);
    }

    // ───────────────────────── UC-174 Create Academic Year (BR-191/192/194) ─────────────────────────

    /**
     * D5 — trùng {@code id}/{@code code} ⇒ 409 SCH-409. {@code copyStructure} sao chép tên/
     * cấp/sức chứa từ năm liền trước, KHÔNG chuyển học sinh (BR-194) — "lên lớp" là hành động
     * tường minh riêng ({@link #promoteStudents}).
     */
    @Transactional
    public AcademicYearResponse create(CreateAcademicYearRequest request, AuthUser actor) {
        if (academicYearRepository.existsBySchoolIdAndName(actor.schoolId(), request.name())) {
            throw SchExceptions.duplicateValue("name", request.name());
        }

        String code;
        if (request.id() != null && !request.id().isBlank()) {
            if (academicYearRepository.existsByCodeAndSchoolId(request.id(), actor.schoolId())) {
                throw SchExceptions.duplicateValue("id", request.id());       // D5
            }
            code = request.id();
        } else {
            code = generateCode(actor.schoolId(), request.startDate().getYear());
        }

        AcademicYear year = new AcademicYear();
        year.setCode(code);
        year.setSchoolId(actor.schoolId());
        year.setName(request.name());
        year.setStartDate(request.startDate());
        year.setEndDate(request.endDate());
        year.setStatus(AcademicYearStatus.DRAFT);
        year.setCopyStructure(request.copyStructure());
        academicYearRepository.saveAndFlush(year);

        if (request.copyStructure()) {
            copyPreviousStructure(year, actor.schoolId());                    // BR-194
        }

        return toResponse(year);
    }

    /** Sao chép tên/cấp/sức chứa lớp từ năm liền trước — KHÔNG copy học sinh/GVCN (BR-194). */
    private void copyPreviousStructure(AcademicYear target, UUID schoolId) {
        academicYearRepository.findFirstBySchoolIdAndStartDateLessThanOrderByStartDateDesc(
                schoolId, target.getStartDate()).ifPresent(previous -> {
            for (Classroom source : classroomRepository.findByAcademicYearId(previous.getId())) {
                Classroom copy = new Classroom();
                copy.setCode(source.getCode());
                copy.setSchoolId(schoolId);
                copy.setAcademicYearId(target.getId());
                copy.setName(source.getName());
                copy.setGrade(source.getGrade());
                copy.setCapacity(source.getCapacity());
                copy.setReadOnly(false);
                classroomRepository.saveAndFlush(copy);
            }
        });
    }

    /** Fallback D5 khi client không gửi {@code id}: {@code AY-{year}}, dò trùng thêm hậu tố chữ cái. */
    private String generateCode(UUID schoolId, int year) {
        String base = "AY-" + year;
        if (!academicYearRepository.existsByCodeAndSchoolId(base, schoolId)) {
            return base;
        }
        for (char suffix = 'B'; suffix <= 'Z'; suffix++) {
            String candidate = base + "-" + suffix;
            if (!academicYearRepository.existsByCodeAndSchoolId(candidate, schoolId)) {
                return candidate;
            }
        }
        throw SchExceptions.duplicateValue("id", base);
    }

    // ───────────────────────── UC-175 Activate (BR-191) ─────────────────────────

    /**
     * BR-191 — ≤1 ACTIVE/trường; enforce trong {@code @Transactional} + {@code PESSIMISTIC_WRITE}
     * (ADR-M02-4) để chống race 2 request activate song song trên cùng trường.
     */
    @Transactional
    public AcademicYearResponse activate(String code, AuthUser actor) {
        AcademicYear year = academicYearRepository.findByCodeAndSchoolId(code, actor.schoolId())
                .orElseThrow(SchExceptions::notFound);

        boolean alreadyActive = !academicYearRepository
                .findBySchoolIdAndStatusForUpdate(actor.schoolId(), AcademicYearStatus.ACTIVE)
                .isEmpty();
        if (alreadyActive) {
            throw SchExceptions.academicYearAlreadyActive();                  // BR-191
        }

        /*
         * Không kích hoạt lại một năm học ĐÃ KẾT THÚC (thêm 08/08/2026).
         *
         * BR-191 chỉ ràng buộc "≤1 năm Active mỗi trường", không nói gì về khoảng ngày — nên
         * trước đây kích hoạt được cả năm đã qua. "Năm học Active" là khoá lọc của rất nhiều
         * màn (lớp học, sĩ số, thực đơn), đặt nhầm vào một năm đã đóng sổ thì cả trường đọc
         * sai dữ liệu mà không có gì báo.
         *
         * CỐ Ý không chặn năm CHƯA BẮT ĐẦU: trường vẫn phải chuẩn bị lớp/sĩ số trước khai
         * giảng nên kích hoạt sớm là hợp lệ. Phần đó xử lý ở tầng hiển thị — FE ghi rõ "chưa
         * bắt đầu" thay vì để người đọc tưởng đó là năm đang diễn ra.
         */
        LocalDate today = LocalDate.now();
        if (year.getEndDate() != null && year.getEndDate().isBefore(today)) {
            throw SchExceptions.validationFailed("endDate",
                    "academic year ended on " + year.getEndDate() + " — cannot activate a finished year");
        }

        year.setStatus(AcademicYearStatus.ACTIVE);
        academicYearRepository.saveAndFlush(year);
        return toResponse(year);
    }

    // ───────────────────────── UC-175 Close (BR-193) ─────────────────────────

    /** Đóng năm ⇒ toàn bộ Classroom của năm đó chuyển read-only (BR-193) — KHÔNG xóa dữ liệu. */
    @Transactional
    public AcademicYearResponse close(String code, AuthUser actor) {
        AcademicYear year = academicYearRepository.findByCodeAndSchoolId(code, actor.schoolId())
                .orElseThrow(SchExceptions::notFound);

        year.setStatus(AcademicYearStatus.CLOSED);
        academicYearRepository.saveAndFlush(year);

        classroomRepository.findByAcademicYearId(year.getId())
                .forEach(c -> c.setReadOnly(true));                           // BR-193

        return toResponse(year);
    }

    // ───────────────────────── UC-177 Promote Students (BR-194) ─────────────────────────

    /**
     * "Lên lớp" là hành động tường minh, hàng loạt theo mapping lớp nguồn→đích, cho phép loại
     * trừ từng HS; hồ sơ sức khỏe/dị ứng KHÔNG bị ảnh hưởng (BR-194). Enrollment thật thuộc
     * MF03 — MF02 chỉ enforce gate capacity (BR-025) trên lớp đích qua sĩ số hiện tại.
     *
     * <p>Việc tồn đọng #5 (00_Common_Conventions.md §0-BIS) — nối vào bảng {@code students}
     * thật của MF03 khi module đó tồn tại. Đọc/ghi bằng <b>native query</b> qua
     * {@link EntityManager}, KHÔNG import entity/repository của {@code mf03} (giữ đúng ranh
     * giới module — cùng khuôn {@code common/reference/ReferenceResolver}, ADR-M02-5).
     * {@code excludeStudentIds} là {@code students.code} (D1); dòng nào không khớp lớp nguồn
     * hoặc đã bị loại trừ thì không đụng tới.
     */
    @Transactional
    public PromotionResult promoteStudents(String code, PromoteStudentsRequest request, AuthUser actor) {
        AcademicYear target = academicYearRepository.findByCodeAndSchoolId(code, actor.schoolId())
                .orElseThrow(SchExceptions::notFound);
        academicYearRepository.findByCodeAndSchoolId(request.sourceAcademicYearId(), actor.schoolId())
                .orElseThrow(SchExceptions::notFound);

        List<String> excludeCodes = request.excludeStudentIds() == null ? List.of() : request.excludeStudentIds();
        long promoted = 0L;
        if (request.mappings() != null) {
            for (var mapping : request.mappings()) {
                Classroom source = classroomRepository
                        .findBySchoolIdAndCode(actor.schoolId(), mapping.sourceClassroomId())
                        .stream().findFirst().orElse(null);
                Classroom targetClassroom = classroomRepository
                        .findBySchoolIdAndCode(actor.schoolId(), mapping.targetClassroomId())
                        .stream().findFirst().orElse(null);
                if (source == null || targetClassroom == null) {
                    continue;                                                 // mapping không khớp lớp thật — bỏ qua
                }
                @SuppressWarnings("unchecked")
                int updated = excludeCodes.isEmpty()
                        ? em.createNativeQuery("UPDATE students SET classroom_id = :targetId "
                                + "WHERE classroom_id = :sourceId AND status = 'ACTIVE'")
                            .setParameter("targetId", targetClassroom.getId())
                            .setParameter("sourceId", source.getId())
                            .executeUpdate()
                        : em.createNativeQuery("UPDATE students SET classroom_id = :targetId "
                                + "WHERE classroom_id = :sourceId AND status = 'ACTIVE' AND code NOT IN :excluded")
                            .setParameter("targetId", targetClassroom.getId())
                            .setParameter("sourceId", source.getId())
                            .setParameter("excluded", excludeCodes)
                            .executeUpdate();
                promoted += updated;
            }
        }

        return new PromotionResult(promoted, excludeCodes.size(), target.getCode());
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    private AcademicYearResponse toResponse(AcademicYear year) {
        long classes = classroomRepository.findByAcademicYearId(year.getId()).size();
        long students = countStudentsInYear(year.getId());
        return mapper.toResponse(year, classes, students);
    }

    /**
     * Việc tồn đọng #5 — trước MF03 luôn trả {@code 0L}. Đếm qua native query trên bảng
     * {@code students} thật (MF03), join {@code classrooms} của chính năm học này; nếu MF03
     * chưa migrate (bảng chưa tồn tại), fallback về {@code 0L} thay vì để migration fail.
     */
    private long countStudentsInYear(UUID academicYearId) {
        try {
            Number count = (Number) em.createNativeQuery(
                            "SELECT COUNT(*) FROM students s JOIN classrooms c ON c.id = s.classroom_id "
                                    + "WHERE c.academic_year_id = :ayId AND s.status = 'ACTIVE'")
                    .setParameter("ayId", academicYearId)
                    .getSingleResult();
            return count.longValue();
        } catch (RuntimeException ex) {
            return 0L;
        }
    }
}
