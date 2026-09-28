package com.sfms.mf02.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.common.enums.RoleKey;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.repository.RoleRecordRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.service.AccountActivationService;
import com.sfms.common.storage.StorageService;
import com.sfms.mf02.domain.School;
import com.sfms.mf02.domain.SchoolDocument;
import com.sfms.mf02.domain.SchoolStatus;
import com.sfms.mf02.dto.SchoolDtos.RegisterSchoolRequest;
import com.sfms.mf02.dto.SchoolDtos.SchoolDirectoryResponse;
import com.sfms.mf02.dto.SchoolDtos.SchoolDocumentResponse;
import com.sfms.mf02.dto.SchoolDtos.SchoolResponse;
import com.sfms.mf02.dto.SchoolDtos.UpdateSchoolRequest;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.mapper.SchoolMapper;
import com.sfms.mf02.repository.ClassroomRepository;
import com.sfms.mf02.repository.SchoolDocumentRepository;
import com.sfms.mf02.repository.SchoolRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UC-070 Register · UC-001 Active/Deactivate · UC-095 Update · UC-122 View · UC-140 View List.
 *
 * <p>Bounded Context: Organization & Academic Structure — MF02 là vertical slice mẫu (P0).
 */
@Service
public class SchoolService {

    /** BR-028 — mã số thuế VN: 10 hoặc 13 chữ số (UC-070, MSG-007). */
    private static final Pattern TAX_ID_PATTERN = Pattern.compile("^\\d{10}(\\d{3})?$");

    private final SchoolRepository schoolRepository;
    private final ClassroomRepository classroomRepository;
    private final SchoolMapper schoolMapper;
    private final CodeGenerator codeGenerator;
    private final Clock clock;
    private final UserAccountRepository userAccountRepository;
    private final RoleRecordRepository roleRecordRepository;
    private final AccountActivationService accountActivationService;
    private final SchoolDocumentRepository schoolDocumentRepository;
    private final StorageService storageService;

    @PersistenceContext
    private EntityManager em;

    public SchoolService(SchoolRepository schoolRepository,
                         ClassroomRepository classroomRepository,
                         SchoolMapper schoolMapper,
                         CodeGenerator codeGenerator,
                         Clock clock,
                         UserAccountRepository userAccountRepository,
                         RoleRecordRepository roleRecordRepository,
                         AccountActivationService accountActivationService,
                         SchoolDocumentRepository schoolDocumentRepository,
                         StorageService storageService) {
        this.schoolRepository = schoolRepository;
        this.classroomRepository = classroomRepository;
        this.schoolMapper = schoolMapper;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
        this.userAccountRepository = userAccountRepository;
        this.roleRecordRepository = roleRecordRepository;
        this.accountActivationService = accountActivationService;
        this.schoolDocumentRepository = schoolDocumentRepository;
        this.storageService = storageService;
    }

    // ───────────────────────── UC-140 View School List ─────────────────────────

    /** §16 — chỉ System Admin, phạm vi "All" (không lọc theo schoolId). */
    @Transactional(readOnly = true)
    public PagedResponse<SchoolResponse> list(ListQuery query) {
        Page<School> page = schoolRepository.findAll(SchoolSpec.build(query), query.toPageable());
        List<SchoolResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    /**
     * Danh bạ rút gọn cho Supplier (UC chọn trường để xin hợp tác — MF04). Tối thiểu hóa dữ
     * liệu (chốt 2026-08-02): chỉ {@code id/name/status}, KHÔNG taxId/tọa độ/liên hệ khẩn cấp
     * — những field nội bộ School/System Admin. Luôn ép {@code status=ACTIVE} bất kể query
     * param — Supplier chỉ nên thấy trường đang hoạt động để xin hợp tác, không phải trường
     * PENDING/INACTIVE. Bỏ qua search/filter khác của {@link SchoolSpec} có chủ đích, danh
     * sách trường ACTIVE nhỏ, đủ dùng cho dropdown.
     */
    @Transactional(readOnly = true)
    public PagedResponse<SchoolDirectoryResponse> directory(ListQuery query) {
        Specification<School> activeOnly = (root, cq, cb) -> cb.equal(root.get("status"), SchoolStatus.ACTIVE);
        Page<School> page = schoolRepository.findAll(activeOnly, query.toPageable());
        List<SchoolDirectoryResponse> items = page.getContent().stream()
                .map(s -> new SchoolDirectoryResponse(s.getCode(), s.getName(), s.getStatus()))
                .toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    // ───────────────────────── UC-122 View Information School ─────────────────────────

    @Transactional(readOnly = true)
    public SchoolResponse getByCode(String code) {
        return toResponse(schoolRepository.findByCode(code).orElseThrow(SchExceptions::notFound));
    }

    // ───────────────────────── UC-070 Register School (Guest, public) ─────────────────────────

    /**
     * D5 — trùng {@code id}/{@code code} client gửi hoặc {@code taxId} ⇒ 409 SCH-409/MSG-139.
     * BR-028 — validate lat/lng ở đây (KHÔNG ở bean validation) để trả đúng 400 SCH-013/MSG-107
     * theo TDS §9.2, thay vì 422 generic.
     */
    @Transactional
    public SchoolResponse register(RegisterSchoolRequest request) {
        return register(request, List.of());
    }

    /**
     * 11/08/2026 — nhận kèm tài liệu đăng ký (giấy phép hoạt động) NGAY LÚC ĐĂNG KÝ. Trước đó
     * khối "Tải tài liệu" ở form Guest chỉ lưu TÊN FILE cục bộ phía FE, không gửi lên đâu cả
     * (không endpoint public nào nhận) — System Admin duyệt hồ sơ mà không hề xem được giấy
     * phép/chứng nhận thật, `system-admin/schools/[id]` phải bịa 2 dòng tài liệu giả để UI
     * không trống. Dùng lại {@link #storeDocument} — cùng bảng {@code school_documents}, cùng
     * đường tải xuống có {@code @PreAuthorize} với tài liệu do System Admin tự thêm sau này.
     */
    @Transactional
    public SchoolResponse register(RegisterSchoolRequest request, List<MultipartFile> documents) {
        validateCoordinate(request.lat(), request.lng());
        validateTaxId(request.taxId());

        if (schoolRepository.existsByTaxId(request.taxId())) {
            throw SchExceptions.duplicateValue("taxId", request.taxId());
        }

        String code;
        if (request.id() != null && !request.id().isBlank()) {
            if (schoolRepository.existsByCode(request.id())) {
                throw SchExceptions.duplicateValue("id", request.id());   // D5
            }
            code = request.id();
        } else {
            code = codeGenerator.nextSchoolCode();
        }

        School school = new School();
        school.setCode(code);
        school.setName(request.name());
        school.setAddress(request.address());
        school.setTaxId(request.taxId());
        school.setEmail(request.email());
        school.setLat(request.lat());
        school.setLng(request.lng());
        school.setOperatingHours(request.operatingHours());
        school.setEmergencyContact(request.emergencyContact());
        school.setStatus(SchoolStatus.PENDING);            // UC-070 — chờ System Admin duyệt
        school.setCoordinateLocked(false);                 // chưa ACTIVE ⇒ chưa khóa (BR-028)
        school.setRegisteredAt(clock.instant());
        schoolRepository.saveAndFlush(school);              // createdBy: AuditorAware (BaseEntity)

        if (documents != null) {
            for (MultipartFile file : documents) {
                if (file != null && !file.isEmpty()) {
                    storeDocument(school, null, "REGISTRATION_DOC", file);
                }
            }
        }

        createAdminAccountAndSendActivation(school);

        return toResponse(school);
    }

    // ───────────────────────── UC-095 Update Information School ─────────────────────────

    /**
     * BR-028 — {@code lat}/{@code lng} chỉ sửa được khi {@code coordinateLocked=false}; sau khi
     * lưu tọa độ mới, khóa lại (ADR-M02-3). Sửa tọa độ khi đã khóa ⇒ 403 SCH-013/MSG-108
     * (bất kể actor là ai — unlock riêng qua {@link #unlockCoordinate}).
     */
    @Transactional
    public SchoolResponse update(String code, UpdateSchoolRequest request) {
        School school = schoolRepository.findByCode(code).orElseThrow(SchExceptions::notFound);

        boolean touchesCoordinate = request.lat() != null || request.lng() != null;
        if (touchesCoordinate) {
            if (school.isCoordinateLocked()) {
                throw SchExceptions.coordinateLocked();
            }
            BigDecimal newLat = request.lat() != null ? request.lat() : school.getLat();
            BigDecimal newLng = request.lng() != null ? request.lng() : school.getLng();
            validateCoordinate(newLat, newLng);
            school.setLat(newLat);
            school.setLng(newLng);
            school.setCoordinateLocked(true);              // ADR-M02-3 — tự khóa lại sau khi lưu
        }

        if (request.name() != null) {
            school.setName(request.name());
        }
        if (request.address() != null) {
            school.setAddress(request.address());
        }
        if (request.email() != null) {
            school.setEmail(request.email());
        }
        if (request.operatingHours() != null) {
            school.setOperatingHours(request.operatingHours());
        }
        if (request.emergencyContact() != null) {
            school.setEmergencyContact(request.emergencyContact());
        }
        if (request.mealsPerDay() != null) {
            school.setMealsPerDay(request.mealsPerDay());
        }

        schoolRepository.saveAndFlush(school);
        return toResponse(school);
    }

    // ───────────────────────── UC-001 Active/Deactivate School (BR-021) ─────────────────────────

    /**
     * ACTIVE lần đầu ⇒ khóa tọa độ (BR-028, UC-001 duyệt trường). INACTIVE ⇒ cascade tạm dừng
     * classroom + tài khoản của trường (BR-021) — thực thi phía tài khoản qua event, MF02 chỉ
     * tạm dừng phần của mình (classroom) trực tiếp qua ID, KHÔNG import entity MF01/MF03.
     */
    @Transactional
    public SchoolResponse changeStatus(String code, SchoolStatus status) {
        School school = schoolRepository.findByCode(code).orElseThrow(SchExceptions::notFound);

        if (status == SchoolStatus.ACTIVE && school.getStatus() != SchoolStatus.ACTIVE) {
            /*
             * BR-010 mở rộng (11/08/2026) — trước đây System Admin phê duyệt được một hồ sơ
             * PENDING dù chưa ai xác nhận email đăng ký có thật hay không (đăng ký gõ sai/email
             * không tồn tại vẫn duyệt được bình thường). Chặn TRƯỚC khi đổi trạng thái.
             */
            if (!isRegistrantEmailVerified(school)) {
                throw SchExceptions.emailNotVerified();
            }
            school.setCoordinateLocked(true);               // BR-028 — khóa lại khi duyệt/kích hoạt
            createAdminAccountAndSendActivation(school);
        }
        school.setStatus(status);
        schoolRepository.saveAndFlush(school);

        if (status == SchoolStatus.INACTIVE) {
            // BR-021 — cascade tạm dừng: đóng băng toàn bộ lớp của trường (không xóa dữ liệu).
            classroomRepository.findBySchoolId(school.getId())
                    .forEach(c -> c.setReadOnly(true));
        }

        return toResponse(school);
    }

    // ───────────────────────── BR-028 — Unlock tọa độ (System Admin) ─────────────────────────

    /** Chỉ System Admin (enforce ở controller qua {@code @PreAuthorize}); bắt buộc lý do + audit. */
    @Transactional
    public SchoolResponse unlockCoordinate(String code, String reason, AuthUser actor) {
        School school = schoolRepository.findByCode(code).orElseThrow(SchExceptions::notFound);
        school.setCoordinateLocked(false);
        schoolRepository.saveAndFlush(school);
        // Audit ghi actor + reason (BR-028) — nhật ký chi tiết ở AuditService khi wiring đủ event.
        return toResponse(school);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    private void validateTaxId(String taxId) {
        if (!TAX_ID_PATTERN.matcher(taxId).matches()) {
            throw SchExceptions.invalidTaxId();
        }
    }

    /** BR-028 — lat∈[-90,90], lng∈[-180,180]. */
    private void validateCoordinate(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lat.compareTo(BigDecimal.valueOf(-90)) < 0
                || lat.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw SchExceptions.invalidCoordinate("lat", lat);
        }
        if (lng == null || lng.compareTo(BigDecimal.valueOf(-180)) < 0
                || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw SchExceptions.invalidCoordinate("lng", lng);
        }
    }

    private SchoolResponse toResponse(School school) {
        long classes = classroomRepository.findBySchoolId(school.getId()).size();
        long staff = countStaff(school.getId());
        long students = 0L; // Field dẫn xuất thuộc miền MF03 (CF-M02-6) — chưa triển khai.
        return schoolMapper.toResponse(school, classes, staff, students, isRegistrantEmailVerified(school));
    }

    /**
     * 11/08/2026 — trường KHÔNG tự lưu "email đã xác thực chưa": tài khoản school-admin do
     * chính {@link #createAdminAccountAndSendActivation} tạo ra mới có {@code emailVerified}
     * (đổi thành true khi bấm link kích hoạt — {@code AccountActivationService.activate()}).
     * Đọc thẳng qua {@code userAccountRepository} (đã import sẵn cho việc tạo tài khoản ở
     * trên, cùng tiền lệ mf02 gọi thẳng iam thay vì qua port) thay vì đồng bộ ngược một cờ
     * riêng trên `School` — tránh sinh thêm một nguồn dữ liệu có thể lệch nhau.
     */
    private boolean isRegistrantEmailVerified(School school) {
        return userAccountRepository.findAllBySchoolId(school.getId()).stream()
                .anyMatch(u -> u.getRole() != null
                        && RoleKey.SCHOOL_ADMIN.key().equals(u.getRole().getRoleKey())
                        && u.isEmailVerified());
    }

    /**
     * Tự động tạo UserAccount (role school-admin, PENDING_VERIFICATION) và phát hành token
     * gửi email kích hoạt cho đại diện Trường học khi đăng ký/kích hoạt (UC-070, UC-154, BR-010).
     */
    private void createAdminAccountAndSendActivation(School school) {
        if (school.getEmail() == null || school.getEmail().isBlank()) {
            return;
        }
        if (userAccountRepository.existsByEmail(school.getEmail())) {
            return;
        }
        RoleRecord role = roleRecordRepository.findByRoleKey(RoleKey.SCHOOL_ADMIN.key()).orElse(null);
        if (role == null) {
            return;
        }

        UserAccount user = new UserAccount();
        user.setCode(codeGenerator.next("users", "U", 4));
        user.setName(school.getName());
        user.setEmail(school.getEmail());
        user.setPasswordHash(null); // ck_pending_has_no_password
        user.setRole(role);
        user.setSchoolId(school.getId());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        userAccountRepository.saveAndFlush(user);

        accountActivationService.issueAndSend(user);
    }

    /**
     * Tải lên tài liệu đính kèm cho Trường học (UC-070 / UC-122).
     */
    @Transactional
    public SchoolDocumentResponse uploadDocument(String schoolCode, String name, String documentType, MultipartFile file) {
        School school = schoolRepository.findByCode(schoolCode).orElseThrow(SchExceptions::notFound);
        return toDocumentResponse(school.getCode(), storeDocument(school, name, documentType, file));
    }

    /** Dùng chung cho cả upload-sau (UC-122, đã đăng nhập) và upload-lúc-đăng-ký (UC-070, guest). */
    private SchoolDocument storeDocument(School school, String name, String documentType, MultipartFile file) {
        String code = codeGenerator.next("school_documents", "DOC-SCH", 4);
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
        String fileKey = "school-documents/" + school.getCode() + "/" + code + "_" + originalFilename;

        storageService.store(fileKey, file);

        SchoolDocument doc = new SchoolDocument();
        doc.setCode(code);
        doc.setSchoolId(school.getId());
        doc.setName(name != null && !name.isBlank() ? name : originalFilename);
        doc.setDocumentType(documentType != null && !documentType.isBlank() ? documentType : "REGISTRATION_DOC");
        doc.setFileKey(fileKey);
        doc.setFileName(originalFilename);
        doc.setFileSize(file.getSize());
        doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/pdf");
        doc.setStatus("PENDING");
        doc.setUploadedAt(clock.instant());

        schoolDocumentRepository.saveAndFlush(doc);
        return doc;
    }

    /**
     * Danh sách tài liệu đính kèm của Trường học (UC-122).
     */
    @Transactional(readOnly = true)
    public List<SchoolDocumentResponse> listDocuments(String schoolCode) {
        School school = schoolRepository.findByCode(schoolCode).orElseThrow(SchExceptions::notFound);
        return schoolDocumentRepository.findBySchoolId(school.getId()).stream()
                .map(doc -> toDocumentResponse(school.getCode(), doc))
                .toList();
    }

    /**
     * Tải xuống/đọc file tài liệu của Trường học (UC-122).
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(String schoolCode, String docCode) {
        SchoolDocument doc = schoolDocumentRepository.findByCode(docCode).orElseThrow(SchExceptions::notFound);
        return storageService.load(doc.getFileKey());
    }

    @Transactional(readOnly = true)
    public SchoolDocument getDocumentEntity(String docCode) {
        return schoolDocumentRepository.findByCode(docCode).orElseThrow(SchExceptions::notFound);
    }

    private SchoolDocumentResponse toDocumentResponse(String schoolCode, SchoolDocument doc) {
        // 13/08/2026 (Nam) — KHÔNG được thêm tiền tố "/api/v1" ở đây: frontend gọi field này
        // qua client `api` (lib/api/client.ts) vốn đã có baseURL "/api/v1", nên URL tuyệt đối
        // kèm sẵn tiền tố bị ghép đôi thành "/api/v1/api/v1/..." → 404, không xem/tải được tài
        // liệu nào (BR liên quan UC-070/UC-122). Giữ tương đối như mọi resource khác.
        String downloadUrl = "/schools/" + schoolCode + "/documents/" + doc.getCode() + "/download";
        return new SchoolDocumentResponse(
                doc.getCode(),
                doc.getName(),
                doc.getDocumentType(),
                doc.getFileName(),
                doc.getFileSize(),
                doc.getContentType(),
                doc.getStatus(),
                doc.getUploadedAt(),
                downloadUrl
        );
    }

    /**
     * COUNT runtime — native query để KHÔNG import entity/repository của MF01 (ADR-M02-2,
     * D1 tham chiếu module khác bằng ID, AP-AI-005).
     */
    private long countStaff(UUID schoolId) {
        Number result = (Number) em.createNativeQuery(
                        "select count(*) from user_accounts where school_id = :schoolId "
                        + "and staff_code is not null")
                .setParameter("schoolId", schoolId)
                .getSingleResult();
        return result.longValue();
    }
}
