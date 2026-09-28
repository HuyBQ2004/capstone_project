package com.sfms.mf13.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.mf13.domain.ReportSlug;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** MF13-TC-006 — BR-175 ma trận quyền theo LOẠI báo cáo (§16), CF-M13-3 (Service Caterers tắt ở MVP). */
class ReportAccessPolicyTest {

    private final ReportAccessPolicy policy = new ReportAccessPolicy();

    @ParameterizedTest(name = "{0} x {1} -> {2}")
    @CsvSource({
            "SCHOOL_ADMIN, THREE_STEP, true",
            "SCHOOL_ADMIN, SYSTEM_USAGE, true",
            "MEDICAL_STAFF, HYGIENE, true",
            "MEDICAL_STAFF, SYSTEM_USAGE, false",
            "SYSTEM_ADMIN, SYSTEM_USAGE, true",
            "SERVICE_CATERERS, RECEIVING, false",
            "TEACHER, THREE_STEP, false",
            "PARENT, THREE_STEP, false",
            "SUPPLIER, THREE_STEP, false"
    })
    void canView(RoleKey role, ReportSlug slug, boolean expected) {
        assertThat(policy.canView(role, slug)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "export {0} x {1} -> {2}")
    @CsvSource({
            "SYSTEM_ADMIN, THREE_STEP, true",
            "SCHOOL_ADMIN, THREE_STEP, true",
            "MEDICAL_STAFF, THREE_STEP, false", // xem được nhưng KHÔNG xuất (§1.13 Actor UC-172)
            "SERVICE_CATERERS, THREE_STEP, false"
    })
    void canExport(RoleKey role, ReportSlug slug, boolean expected) {
        assertThat(policy.canExport(role, slug)).isEqualTo(expected);
    }

    @org.junit.jupiter.api.Test
    void onlySystemAdminCanViewAllSchools() {
        assertThat(policy.canViewAllSchools(RoleKey.SYSTEM_ADMIN)).isTrue();
        assertThat(policy.canViewAllSchools(RoleKey.SCHOOL_ADMIN)).isFalse();
    }
}
