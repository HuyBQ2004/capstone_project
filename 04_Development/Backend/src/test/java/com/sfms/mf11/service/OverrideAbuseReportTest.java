package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.domain.ViolationRecord;
import com.sfms.mf11.domain.ViolationType;
import com.sfms.mf11.dto.ViolationDtos.OverrideAbuseRow;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.ViolationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-026 — BR-190, {@code ViolationServiceImpl.overrideAbuseReport()} — dẫn xuất runtime,
 * gom theo tuần ISO × đại diện đơn vị (ADR-M11-6).
 */
class OverrideAbuseReportTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID REP_A = UUID.randomUUID();
    private static final UUID REP_B = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-05T10:00:00Z"), ZoneOffset.UTC);

    private ViolationRecordRepository repository;
    private ViolationServiceImpl service;

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "bgh@sch.edu.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static ViolationRecord overrideAbuseAt(UUID repId, String repLabel, LocalDate date) {
        ViolationRecord v = new ViolationRecord();
        v.setSchoolId(SCHOOL_ID);
        v.setViolationType(ViolationType.OVERRIDE_ABUSE);
        v.setCaterersRepresentativeId(repId);
        v.setCaterersRepresentativeLabel(repLabel);
        v.setDate(date);
        return v;
    }

    @SuppressWarnings("unchecked")
    private void stubAll(List<ViolationRecord> rows) {
        when(repository.findAll(any(Specification.class))).thenReturn(rows);
    }

    @BeforeEach
    void setUp() {
        repository = mock(ViolationRecordRepository.class);
        IncidentRepository incidentRepository = mock(IncidentRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        StorageService storageService = mock(StorageService.class);
        service = new ViolationServiceImpl(repository, incidentRepository, referenceResolver, codeGenerator,
                storageService, CLOCK);
    }

    @Test
    void twoOverrideAbusesForTheSameRepresentativeInTheSameIsoWeekCollapseIntoOneRowWithCount2() {
        // 2026-W27: 29/06 - 05/07 (ISO tuần bắt đầu thứ 2).
        stubAll(List.of(
                overrideAbuseAt(REP_A, "Đại diện A", LocalDate.of(2026, 6, 30)),
                overrideAbuseAt(REP_A, "Đại diện A", LocalDate.of(2026, 7, 2))));

        List<OverrideAbuseRow> rows = service.overrideAbuseReport("2026-W27", schoolAdmin());

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).week()).isEqualTo("2026-W27");
        assertThat(rows.get(0).overrideCount()).isEqualTo(2);
        assertThat(rows.get(0).threshold()).isEqualTo(3);
        assertThat(rows.get(0).caterersRepresentativeLabel()).isEqualTo("Đại diện A");
    }

    @Test
    void differentRepresentativesInTheSameWeekProduceSeparateRowsGroupedByRepresentativeNotStaffName() {
        stubAll(List.of(
                overrideAbuseAt(REP_A, "Đại diện A", LocalDate.of(2026, 6, 30)),
                overrideAbuseAt(REP_B, "Đại diện B", LocalDate.of(2026, 7, 1))));

        List<OverrideAbuseRow> rows = service.overrideAbuseReport("2026-W27", schoolAdmin());

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(OverrideAbuseRow::overrideCount).containsExactly(1L, 1L);
    }

    @Test
    void recordsFromADifferentWeekAreExcludedWhenFilteringByWeek() {
        stubAll(List.of(
                overrideAbuseAt(REP_A, "Đại diện A", LocalDate.of(2026, 6, 30)), // W27
                overrideAbuseAt(REP_A, "Đại diện A", LocalDate.of(2026, 7, 10)))); // W28

        List<OverrideAbuseRow> rows = service.overrideAbuseReport("2026-W27", schoolAdmin());

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).overrideCount()).isEqualTo(1);
    }

    @Test
    void noWeekFilterReturnsAllWeeksSeparately() {
        stubAll(List.of(
                overrideAbuseAt(REP_A, "Đại diện A", LocalDate.of(2026, 6, 30)),
                overrideAbuseAt(REP_A, "Đại diện A", LocalDate.of(2026, 7, 10))));

        List<OverrideAbuseRow> rows = service.overrideAbuseReport(null, schoolAdmin());

        assertThat(rows).hasSize(2);
    }
}
