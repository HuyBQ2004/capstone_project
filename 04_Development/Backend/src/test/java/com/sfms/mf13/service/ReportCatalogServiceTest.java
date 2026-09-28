package com.sfms.mf13.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.ReportDefinitionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF13-TC-012 — UC-171: {@code ReportCatalogService.listFor(actor)} trả danh mục 7 báo cáo lọc
 * theo quyền (BR-175, CF-M13-7d — thay 2 dòng hardcode FE). Service thật, {@code ReportAccessPolicy}
 * thật (không mock) — không có Spring context, chỉ hai POJO cộng tác trực tiếp.
 */
class ReportCatalogServiceTest {

    private final ReportCatalogServiceImpl service = new ReportCatalogServiceImpl(new ReportAccessPolicy());

    private static AuthUser actor(RoleKey role) {
        return new AuthUser(UUID.randomUUID(), "U-TEST", "test@sfms.vn", role, UUID.randomUUID(), null);
    }

    @Test
    void schoolAdminSeesAllSevenReportsIncludingTraceabilityAndSystemUsage() {
        List<ReportDefinitionResponse> catalog = service.listFor(actor(RoleKey.SCHOOL_ADMIN));

        assertThat(catalog).hasSize(7);
        assertThat(catalog.stream().map(ReportDefinitionResponse::slug))
                .contains(ReportSlug.TRACEABILITY, ReportSlug.SYSTEM_USAGE);
        assertThat(catalog).allMatch(ReportDefinitionResponse::exportable);
    }

    @Test
    void systemAdminSeesAllSevenReports() {
        List<ReportDefinitionResponse> catalog = service.listFor(actor(RoleKey.SYSTEM_ADMIN));
        assertThat(catalog).hasSize(7);
    }

    @Test
    void medicalStaffSeesSixReportsExcludingSystemUsageAndCannotExportAny() {
        List<ReportDefinitionResponse> catalog = service.listFor(actor(RoleKey.MEDICAL_STAFF));

        assertThat(catalog).hasSize(6);
        assertThat(catalog.stream().map(ReportDefinitionResponse::slug))
                .doesNotContain(ReportSlug.SYSTEM_USAGE);
        // BR-175 — Medical Staff xem được nhưng KHÔNG xuất được bất kỳ mục nào.
        assertThat(catalog).allMatch(item -> !item.exportable());
    }

    @Test
    void serviceCaterersSeeZeroReportsAtMvp() {
        // CF-M13-3 — nhánh Service Caterers TẮT ở MVP (⛔ L5, chờ Huy bổ sung Actor §1.13).
        List<ReportDefinitionResponse> catalog = service.listFor(actor(RoleKey.SERVICE_CATERERS));
        assertThat(catalog).isEmpty();
    }

    @Test
    void teacherParentSupplierSeeZeroReports() {
        assertThat(service.listFor(actor(RoleKey.TEACHER))).isEmpty();
        assertThat(service.listFor(actor(RoleKey.PARENT))).isEmpty();
        assertThat(service.listFor(actor(RoleKey.SUPPLIER))).isEmpty();
    }

    @Test
    void everyItemHasSlugTitleGroupAndExportableField() {
        for (ReportDefinitionResponse item : service.listFor(actor(RoleKey.SCHOOL_ADMIN))) {
            assertThat(item.slug()).isNotNull();
            assertThat(item.title()).isNotBlank();
            assertThat(item.group()).isIn("COMPLIANCE", "ADDITIONAL");
        }
    }

    @Test
    void canViewAndCanExportDelegateToAccessPolicy() {
        AuthUser schoolAdmin = actor(RoleKey.SCHOOL_ADMIN);
        AuthUser medical = actor(RoleKey.MEDICAL_STAFF);

        assertThat(service.canView(schoolAdmin, ReportSlug.HYGIENE)).isTrue();
        assertThat(service.canExport(schoolAdmin, ReportSlug.HYGIENE)).isTrue();
        assertThat(service.canView(medical, ReportSlug.HYGIENE)).isTrue();
        assertThat(service.canExport(medical, ReportSlug.HYGIENE)).isFalse();
    }
}
