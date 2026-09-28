package com.sfms.mf13.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.service.AuditService;
import com.sfms.mf13.domain.ReportFormat;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.ReportExportResponse;
import com.sfms.mf13.repository.ReportExportRepository;
import com.sfms.mf13.repository.ReportViewGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF13-TC-005 — BR-177: hạn lưu trữ ≥ 12 tháng.
 *
 * <p><b>Lệch so với giả định của TestSpec:</b> {@code report.retention-months} KHÔNG phải là
 * cấu hình bên ngoài đọc được (không {@code @Value}, không {@code application.yml} key nào tên
 * đó) — {@code ReportExportServiceImpl} khoá cứng {@code Math.max(MIN_RETENTION_MONTHS=12,
 * DEFAULT_RETENTION_MONTHS=24) = 24} tháng cho MỌI lần xuất, không có nhánh đọc cấu hình nào để
 * "kẹp về sàn" khi bị chỉnh dưới 12. Test này viết đúng hành vi thật: retention LUÔN LÀ 24 tháng
 * — vẫn thoả BR-177 (≥12 tháng), chỉ không phải một tham số vận hành có thể chỉnh được như spec
 * gốc giả định. Dùng Mockito để cô lập {@code ReportExportServiceImpl} khỏi Spring/Testcontainers
 * — nhanh hơn nhiều so với việc bọc lại thành integration test chỉ để đo một phép cộng ngày.
 */
class ReportRetentionTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private ReportExportRepository repository;
    private ReportViewGateway viewGateway;
    private StorageService storageService;
    private ReferenceResolver referenceResolver;
    private AuditService auditService;
    private CodeGenerator codeGenerator;
    private ReportExportServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(ReportExportRepository.class);
        viewGateway = mock(ReportViewGateway.class);
        storageService = mock(StorageService.class);
        referenceResolver = mock(ReferenceResolver.class);
        auditService = mock(AuditService.class);
        codeGenerator = mock(CodeGenerator.class);
        ReportAccessPolicy accessPolicy = new ReportAccessPolicy();
        ReportRowProjection projection = new ReportRowProjection();

        ReportRenderer stubRenderer = new ReportRenderer() {
            @Override
            public byte[] render(String title, String periodLabel, String schoolName, Instant generatedAt,
                                 List<Map<String, Object>> rows) {
                return "stub-pdf-bytes".getBytes();
            }

            @Override
            public String contentType() {
                return "application/pdf";
            }

            @Override
            public String fileExtension() {
                return "pdf";
            }
        };
        Map<String, ReportRenderer> renderers = Map.of("pdf", stubRenderer, "xlsx", stubRenderer);

        Clock fixedClock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        when(viewGateway.rows(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(List.of());
        when(referenceResolver.schoolName(any())).thenReturn("TH Test");
        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("RPX-2026-001");

        service = new ReportExportServiceImpl(repository, viewGateway, accessPolicy, projection, renderers,
                storageService, referenceResolver, auditService, codeGenerator, fixedClock);
    }

    @Test
    void expiresAtIsAlwaysGeneratedAtPlus24Months() {
        AuthUser schoolAdmin = new AuthUser(USER_ID, "U-0001", "admin@school.vn",
                com.sfms.common.enums.RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);

        ReportExportResponse response = service.export(schoolAdmin, ReportSlug.HYGIENE, ReportFormat.PDF,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 30), null, Map.of());

        Instant expectedExpiry = response.generatedAt().atZone(ZoneOffset.UTC)
                .plus(Period.ofMonths(24)).toInstant();
        assertThat(response.expiresAt()).isEqualTo(expectedExpiry);

        // Bất biến chính của BR-177 — luôn ≥ 12 tháng, không phụ thuộc cấu hình.
        Instant floor = response.generatedAt().atZone(ZoneOffset.UTC).plus(Period.ofMonths(12)).toInstant();
        assertThat(response.expiresAt()).isAfterOrEqualTo(floor);
    }

    @Test
    void savedEntityCarriesTheSameExpiresAtAsResponse() {
        AuthUser schoolAdmin = new AuthUser(USER_ID, "U-0001", "admin@school.vn",
                com.sfms.common.enums.RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);

        service.export(schoolAdmin, ReportSlug.RECEIVING, ReportFormat.XLSX,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, Map.of());

        ArgumentCaptor<com.sfms.mf13.domain.ReportExport> captor =
                ArgumentCaptor.forClass(com.sfms.mf13.domain.ReportExport.class);
        verify(repository).save(captor.capture());
        com.sfms.mf13.domain.ReportExport saved = captor.getValue();

        Instant expected = saved.getGeneratedAt().atZone(ZoneOffset.UTC).plus(Period.ofMonths(24)).toInstant();
        assertThat(saved.getExpiresAt()).isEqualTo(expected);
    }
}
