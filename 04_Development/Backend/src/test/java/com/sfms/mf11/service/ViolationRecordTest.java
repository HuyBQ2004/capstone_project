package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.dto.ViolationDtos.CreateViolationRequest;
import com.sfms.mf11.dto.ViolationDtos.ViolationResponse;
import com.sfms.mf11.domain.ViolationRecord;
import com.sfms.mf11.domain.ViolationSeverity;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.ViolationRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-022/023 — {@code ViolationServiceImpl.create()}.
 *
 * <p><b>GAP thật vs TC-022</b>: BR-183/BR-211 liệt kê 5 field bắt buộc (
 * {@code locationOfIncident}, {@code occurredAt}, {@code caterersRepresentativeId},
 * {@code description}, ảnh bằng chứng). Code thật ({@code create()}, dòng đầu) chỉ tự kiểm tra
 * {@code subject}/{@code description} rỗng → {@code incompleteViolation()} (IEM-085/MSG-085).
 * {@code caterersRepresentativeCode} thiếu → ném lỗi RIÊNG qua
 * {@code referenceResolver.userIdByCode(...).orElseThrow(validationFailed(...))} = IEM-001, KHÔNG
 * phải IEM-085. {@code locationOfIncident}/{@code occurredAt} là {@code @NotBlank}/{@code @NotNull}
 * ở tầng DTO (Bean Validation, chặn TRƯỚC khi vào service — ngoài phạm vi unit test service,
 * MockMvc IT mới thấy được) chứ KHÔNG bị {@code create()} tự kiểm tra bên trong thân hàm. Không có
 * validate "evidence photo" nào ở {@code create()} — ảnh bằng chứng nộp qua endpoint riêng
 * ({@code uploadEvidence()}), không phải field của {@code CreateViolationRequest}.
 *
 * <p><b>GAP thật vs TC-023</b>: không có {@code RepeatViolationDetector} riêng — logic nằm
 * thẳng trong {@code create()}: {@code repeat = severity != LOW && repository
 * .findByCaterersRepresentativeIdAndDateBetween(rep, date-90, date)} không rỗng. Điều kiện
 * THẬT chỉ xét severity CỦA BIÊN BẢN MỚI, KHÔNG xét severity của biên bản CŨ — nghĩa là một
 * biên bản {@code LOW} cũ trong 90 ngày VẪN kích {@code repeatSuspect = true} nếu biên bản mới
 * là MEDIUM/HIGH (khác giả định TC-023 dòng #3, vốn kỳ vọng biên bản cũ LOW ⇒ false). Response
 * cũng không có field {@code warnings[]} — chỉ có {@code repeatSuspect: boolean}; không có
 * message MSG-088 nào được trả trong response (constant tồn tại ở {@code IemMessages.MSG_088}
 * nhưng KHÔNG được service nào tham chiếu — grep xác nhận, xem test cuối file).
 */
class ViolationRecordTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID REP_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private ViolationRecordRepository repository;
    private ReferenceResolver referenceResolver;
    private ViolationServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static CreateViolationRequest validRequest(String severity, LocalDate date) {
        return new CreateViolationRequest("Vi phạm vệ sinh", date, severity, "HYGIENE_VIOLATION", "Bếp bẩn",
                Instant.parse("2026-06-30T02:00:00Z"), "Khu bếp trường", "CAT-001-REP", null, false, null, null);
    }

    @BeforeEach
    void setUp() {
        repository = mock(ViolationRecordRepository.class);
        IncidentRepository incidentRepository = mock(IncidentRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        StorageService storageService = mock(StorageService.class);

        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("VIO-2026-008");
        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế"});
        when(referenceResolver.userIdByCode("CAT-001-REP")).thenReturn(Optional.of(REP_ID));
        when(referenceResolver.userById(REP_ID)).thenReturn(new String[]{"CAT-001-REP", "rep@cat.vn", "Đại diện A"});
        when(repository.findByCaterersRepresentativeIdAndDateBetween(any(), any(), any())).thenReturn(List.of());

        service = new ViolationServiceImpl(repository, incidentRepository, referenceResolver, codeGenerator,
                storageService, CLOCK);
    }

    // ───────────────────────── TC-022 ─────────────────────────

    @Test
    void blankSubjectOrDescriptionIsRejectedWith400IemValidationFailedAndMsg085() {
        CreateViolationRequest bad = new CreateViolationRequest("", LocalDate.of(2026, 6, 30), "LOW", null, "",
                null, null, "CAT-001-REP", null, false, null, null);

        assertThatThrownBy(() -> service.create(bad, medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED);
                    assertThat(ex.getStatus().value()).isEqualTo(400);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_085);
                });
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void unknownCaterersRepresentativeCodeIsRejectedButWithGenericValidationCodeNotMsg085() {
        when(referenceResolver.userIdByCode("PTN-0007")).thenReturn(Optional.empty());
        CreateViolationRequest bad = new CreateViolationRequest("Vi phạm", LocalDate.of(2026, 6, 30), "LOW", null,
                "Mô tả", Instant.now(CLOCK), "Bếp", "PTN-0007", null, false, null, null);

        assertThatThrownBy(() -> service.create(bad, medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        verify(repository, never()).saveAndFlush(any());
    }

    /** GAP thật (xem javadoc lớp) — locationOfIncident null KHÔNG bị create() tự chặn. */
    @Test
    void nullLocationOfIncidentIsNotRejectedByTheServiceMethodItself() {
        CreateViolationRequest req = new CreateViolationRequest("Vi phạm", LocalDate.of(2026, 6, 30), "LOW", null,
                "Mô tả đầy đủ", Instant.now(CLOCK), null, "CAT-001-REP", null, false, null, null);

        ViolationResponse response = service.create(req, medicalStaff());

        assertThat(response.locationOfIncident()).isNull();
        verify(repository).saveAndFlush(any());
    }

    @Test
    void invalidSeverityIsRejectedWith400() {
        CreateViolationRequest bad = validRequest("CRITICAL", LocalDate.of(2026, 6, 30));
        assertThatThrownBy(() -> service.create(bad, medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
    }

    @Test
    void happyPathCreatesViolationAtOpenStatus() {
        ViolationResponse response = service.create(validRequest("LOW", LocalDate.of(2026, 6, 30)), medicalStaff());
        assertThat(response.status()).isEqualTo("OPEN");
        assertThat(response.resolution()).isEqualTo("PENDING");
    }

    /** BR-184 Deprecated (L13/CF-M11-3) — DTO không có field cấp cá nhân staffName/staffPosition/disciplinaryLevel. */
    @Test
    void createViolationRequestHasNoIndividualStaffFields() {
        RecordComponent[] components = CreateViolationRequest.class.getRecordComponents();
        List<String> names = Arrays.stream(components).map(RecordComponent::getName).toList();
        assertThat(names).doesNotContain("staffName", "staffPosition", "disciplinaryLevel");
    }

    // ───────────────────────── TC-023 — nghi tái diễn ─────────────────────────

    @Test
    void twoViolationsAtLeastMediumWithinNinetyDaysForTheSameRepresentativeFlagsRepeatSuspect() {
        var prior = new ViolationRecord();
        prior.setSeverity(ViolationSeverity.MEDIUM);
        when(repository.findByCaterersRepresentativeIdAndDateBetween(REP_ID,
                LocalDate.of(2026, 6, 30).minusDays(90), LocalDate.of(2026, 6, 30)))
                .thenReturn(List.of(prior));

        ViolationResponse response = service.create(validRequest("MEDIUM", LocalDate.of(2026, 6, 30)), medicalStaff());

        assertThat(response.repeatSuspect()).isTrue();
    }

    @Test
    void aLowSeverityNewViolationIsNeverFlaggedAsRepeatEvenWithPriorHistory() {
        var prior = new ViolationRecord();
        prior.setSeverity(ViolationSeverity.HIGH);
        when(repository.findByCaterersRepresentativeIdAndDateBetween(any(), any(), any())).thenReturn(List.of(prior));

        ViolationResponse response = service.create(validRequest("LOW", LocalDate.of(2026, 6, 30)), medicalStaff());

        assertThat(response.repeatSuspect()).isFalse();
    }

    /**
     * GAP thật (xem javadoc lớp) — {@code create()} KHÔNG lọc theo severity của biên bản CŨ,
     * chỉ gọi {@code findByCaterersRepresentativeIdAndDateBetween} (không severity trong tên
     * method/predicate) rồi kiểm {@code isEmpty()}. Một biên bản LOW cũ trong 90 ngày VẪN khiến
     * {@code repeatSuspect = true} nếu biên bản MỚI là MEDIUM/HIGH — khác giả định TC-023.
     */
    @Test
    void repeatSuspectIgnoresThePriorViolationsSeverityInTheRealImplementation() {
        var priorLow = new ViolationRecord();
        priorLow.setSeverity(ViolationSeverity.LOW);
        when(repository.findByCaterersRepresentativeIdAndDateBetween(any(), any(), any())).thenReturn(List.of(priorLow));

        ViolationResponse response = service.create(validRequest("HIGH", LocalDate.of(2026, 6, 30)), medicalStaff());

        assertThat(response.repeatSuspect()).isTrue();
    }

    @Test
    void windowIsExactlyNinetyDaysBeforeTheNewViolationsDate() {
        LocalDate date = LocalDate.of(2026, 6, 30);
        service.create(validRequest("HIGH", date), medicalStaff());
        verify(repository).findByCaterersRepresentativeIdAndDateBetween(REP_ID, date.minusDays(90), date);
    }

    /**
     * Response mang {@code repeatSuspect: boolean} — không có mảng {@code warnings[]} nào chứa
     * MSG-088 (constant tồn tại ở {@code IemMessages.MSG_088} nhưng {@code ViolationServiceImpl}
     * không có bất kỳ tham chiếu nào tới nó — xác nhận bằng shape DTO thật).
     */
    @Test
    void responseShapeCarriesOnlyABooleanFlagNotAWarningsArray() {
        List<String> names = Arrays.stream(ViolationResponse.class.getRecordComponents())
                .map(RecordComponent::getName).toList();
        assertThat(names).contains("repeatSuspect").doesNotContain("warnings");
    }
}
