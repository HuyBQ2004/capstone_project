package com.sfms.mf13.domain;

import com.sfms.mf13.repository.ReportExportRepository;
import com.sfms.mf13.service.ReportExportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF13-TC-010 — BR-182: {@code ReportExport} bất biến (append-only, ADR-M13-3). Reflection-based
 * structural test — không setter công khai cho cột đã ghi, service không có {@code update}, và
 * repository không khai {@code deleteBy*} nào ngoài đường purge ({@code findByExpiresAtBefore}).
 */
class ReportExportImmutabilityTest {

    private static final Set<String> AUDIT_SENSITIVE_FIELDS = Set.of(
            "code", "schoolId", "reportSlug", "rowCount", "fileUri", "checksumSha256",
            "generatedAt", "expiresAt");

    @ParameterizedTest(name = "no public setXxx for {0}")
    @ValueSource(strings = {"code", "schoolId", "reportSlug", "rowCount", "fileUri", "checksumSha256",
            "generatedAt", "expiresAt"})
    void noPublicSetterExistsForAuditSensitiveField(String fieldName) {
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        boolean hasPublicSetter = Arrays.stream(ReportExport.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals(setterName) && Modifier.isPublic(m.getModifiers()));
        assertThat(hasPublicSetter)
                .as("ReportExport.%s() KHÔNG được là public — mở đường sửa số liệu sau khi báo cáo "
                        + "đã gửi cho BGH/thanh tra (BR-182)", setterName)
                .isFalse();
    }

    @Test
    void zeroPublicSetMethodsExistOnEntityAtAll() {
        List<Method> publicSetters = Arrays.stream(ReportExport.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("set") && Modifier.isPublic(m.getModifiers()))
                .toList();
        assertThat(publicSetters)
                .as("ReportExport không được khai BẤT KỲ public setXxx() nào — entity chỉ ghi qua "
                        + "constructor (ADR-M13-3): %s", publicSetters)
                .isEmpty();
    }

    @Test
    void onlyConstructorAndGettersArePublic_assertionPositive() {
        // Assertion dương — chứng minh entity vẫn CÓ getter công khai (không phải class rỗng vô nghĩa).
        long publicGetters = Arrays.stream(ReportExport.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("get") || m.getName().startsWith("is"))
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .count();
        assertThat(publicGetters).isGreaterThanOrEqualTo(AUDIT_SENSITIVE_FIELDS.size());
    }

    @Test
    void reportExportServiceHasNoUpdateMethod() {
        Set<String> methodNames = Arrays.stream(ReportExportService.class.getMethods())
                .map(Method::getName)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(methodNames)
                .as("ReportExportService.update()/*Update* KHÔNG được tồn tại (BR-182 ở tầng API)")
                .containsExactlyInAnyOrder("export", "history", "findByCode", "download");
    }

    @Test
    void reportExportRepositoryDeclaresNoCustomDeleteByQuery() {
        // getDeclaredMethods() — CHỈ method tự khai trên interface này, KHÔNG tính deleteById()/
        // delete()/deleteAll() kế thừa từ JpaRepository (mọi repository đều có, không phải rủi ro
        // BR-182). Rủi ro thật là derived query kiểu deleteByXxx tự viết cho ReportExport.
        Set<String> customDeleteLikeMethods = Arrays.stream(ReportExportRepository.class.getDeclaredMethods())
                .map(Method::getName)
                .filter(n -> n.startsWith("deleteBy") || n.startsWith("removeBy"))
                .collect(java.util.stream.Collectors.toSet());
        assertThat(customDeleteLikeMethods)
                .as("ReportExportRepository không được khai deleteBy*/removeBy* tự viết — retention "
                        + "chỉ do purge job SAU expires_at, không có đường xoá thủ công theo điều kiện")
                .isEmpty();
    }
}
