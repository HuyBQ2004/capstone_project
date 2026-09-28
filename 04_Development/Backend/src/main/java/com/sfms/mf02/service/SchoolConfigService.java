package com.sfms.mf02.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.domain.School;
import com.sfms.mf02.domain.SchoolConfig;
import com.sfms.mf02.dto.SchoolConfigDtos.SchoolConfigResponse;
import com.sfms.mf02.dto.SchoolConfigDtos.UpdateSchoolConfigRequest;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.mapper.SchoolConfigMapper;
import com.sfms.mf02.repository.SchoolConfigRepository;
import com.sfms.mf02.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-095 — cấu hình cấp trường: geofence (BR-028), costCeiling (BR-080, miền MF05).
 *
 * <p>§16 — System Admin thấy mọi trường; School Admin chỉ trường của chính JWT (BR-022) —
 * cross-school ⇒ 404 SCH-404 (chốt 2026-07-31, đóng L7), không lộ tồn tại qua 403.
 */
@Service
public class SchoolConfigService {

    private final SchoolRepository schoolRepository;
    private final SchoolConfigRepository configRepository;
    private final SchoolConfigMapper mapper;
    private final ReferenceResolver referenceResolver;

    /**
     * BR-061 — cùng property mà {@code MenuServiceImpl} dùng để CHẶN nộp duyệt. Đọc lại ở
     * đây để FE nhận đúng con số server đang thi hành, thay vì tự khai một hằng riêng.
     */
    @org.springframework.beans.factory.annotation.Value("${sfms.menu.lead-time-days:3}")
    private int menuLeadTimeDays;

    public SchoolConfigService(SchoolRepository schoolRepository,
                               SchoolConfigRepository configRepository,
                               SchoolConfigMapper mapper,
                               ReferenceResolver referenceResolver) {
        this.schoolRepository = schoolRepository;
        this.configRepository = configRepository;
        this.mapper = mapper;
        this.referenceResolver = referenceResolver;
    }

    /**
     * KHÔNG {@code readOnly = true}: {@code configOrDefault} ghi (INSERT) khi trường chưa
     * từng có config — bản sửa lỗi SCH-500 (2026-08-02), trước đó transaction chỉ-đọc khiến
     * Postgres từ chối INSERT ngầm ở lần đọc đầu tiên của mọi trường.
     */
    @Transactional
    public SchoolConfigResponse get(String schoolCode, AuthUser actor) {
        School school = requireInScope(schoolCode, actor);
        return mapper.toResponse(configOrDefault(school), menuLeadTimeDays,
                school.getAddress(), school.getLat(), school.getLng());
    }

    @Transactional
    public SchoolConfigResponse update(String schoolCode, UpdateSchoolConfigRequest request, AuthUser actor) {
        School school = requireInScope(schoolCode, actor);
        SchoolConfig config = configOrDefault(school);

        if (request.costCeiling() != null) {
            config.setCostCeiling(request.costCeiling());
        }
        if (request.geofenceRadius() != null) {
            config.setGeofenceRadius(request.geofenceRadius());   // BR-028: [50,500] (bean validation)
        }
        if (request.reportEmails() != null) {
            config.setReportEmails(request.reportEmails());
        }
        configRepository.saveAndFlush(config);
        return mapper.toResponse(config, menuLeadTimeDays, school.getAddress(), school.getLat(), school.getLng());
    }

    private School requireInScope(String schoolCode, AuthUser actor) {
        School school = schoolRepository.findByCode(schoolCode).orElseThrow(SchExceptions::notFound);
        if (actor.isSystemAdmin() || actor.belongsToSchool(school.getId())) {
            return school;
        }
        /*
         * Tài khoản Đơn vị suất ăn đăng ký qua hợp tác nên KHÔNG có `schoolId` trong JWT ⇒
         * `belongsToSchool` luôn false ⇒ SCH-404 (sửa 08/08/2026).
         *
         * Vì sao lọt lâu: 06/08/2026 đã thêm SERVICE_CATERERS vào `@PreAuthorize` của
         * `GET /{code}/config` để MenuForm đọc `costCeiling` — nhưng chỉ mở CỬA, còn kiểm tra
         * phạm vi bên trong vẫn từ chối. Hệ quả đo được: `menuLeadTimeDays` không bao giờ về
         * tới FE ⇒ màn Lịch thực đơn tuần khoá toàn bộ ô ⇒ đúng vai lập thực đơn thì không
         * thêm được bữa nào. Cùng lớp lỗi với `Mf06Scope`/`Mf11Scope`.
         *
         * Chỉ áp cho SERVICE_CATERERS + đúng trường trong hợp tác đang hiệu lực — KHÔNG mở cho
         * NCC (phục vụ nhiều trường, và không có nhu cầu đọc cấu hình vận hành của trường).
         */
        if (actor.role() == RoleKey.SERVICE_CATERERS && actor.partnerId() != null
                && referenceResolver.activeSchoolIdForPartner(actor.partnerId())
                        .filter(school.getId()::equals).isPresent()) {
            return school;
        }
        throw SchExceptions.notFound();                           // BR-022
    }

    private SchoolConfig configOrDefault(School school) {
        return configRepository.findBySchoolId(school.getId()).orElseGet(() -> {
            SchoolConfig config = new SchoolConfig();
            config.setSchoolId(school.getId());
            return configRepository.saveAndFlush(config);
        });
    }
}
