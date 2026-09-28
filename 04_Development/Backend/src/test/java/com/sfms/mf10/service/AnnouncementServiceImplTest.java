package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Announcement;
import com.sfms.mf10.domain.AnnouncementRevision;
import com.sfms.mf10.domain.AnnouncementStatus;
import com.sfms.mf10.dto.AnnouncementDtos.AnnouncementDetailResponse;
import com.sfms.mf10.dto.AnnouncementDtos.CreateAnnouncementRequest;
import com.sfms.mf10.dto.AnnouncementDtos.UpdateAnnouncementRequest;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.repository.AnnouncementRepository;
import com.sfms.mf10.repository.AnnouncementRevisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code AnnouncementServiceImpl} — UC-017/089/105, BR-138/144, TDS-MF10 TC-010/011/012/013 +
 * TC-SEC-005. Trước bản này KHÔNG có unit test nào cho lớp này (chỉ phủ gián tiếp qua HTTP trong
 * {@code FeedbackAnnouncementFlowIT}, vốn không chạm role guard của {@code create()}, validation
 * chi tiết, {@code enumInvalid} khi status sai, hay nhánh DRAFT bị ẩn ở {@code getDetail()}).
 */
class AnnouncementServiceImplTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();

    private AnnouncementRepository announcementRepository;
    private AnnouncementRevisionRepository revisionRepository;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private AnnouncementServiceImpl service;

    private static AuthUser schoolAdmin(UUID userId) {
        return new AuthUser(userId, "U-9902", "admin@sfms.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static AuthUser teacher() {
        return new AuthUser(UUID.randomUUID(), "U-9910", "teacher@sfms.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static AuthUser parent() {
        return new AuthUser(UUID.randomUUID(), "U-9905", "parent@sfms.vn", RoleKey.PARENT, null, null);
    }

    @BeforeEach
    void setUp() {
        announcementRepository = mock(AnnouncementRepository.class);
        revisionRepository = mock(AnnouncementRevisionRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(CodeGenerator.class);
        service = new AnnouncementServiceImpl(announcementRepository, revisionRepository, referenceResolver,
                codeGenerator);

        when(codeGenerator.next(anyString(), anyString(), eq(3))).thenReturn("ANN-2026-001");
        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-9902", "", "Nguyễn Văn Nam"});
    }

    @Test
    void createByNonSchoolAdminIsDenied() {
        var req = new CreateAnnouncementRequest("Tiêu đề", "Nội dung", List.of("TEACHER"), false, null);

        assertThatThrownBy(() -> service.create(req, teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ACCESS_DENIED));
    }

    @Test
    void createWithBlankTitleIsRejected() {
        var req = new CreateAnnouncementRequest("  ", "Nội dung", List.of("TEACHER"), false, null);

        assertThatThrownBy(() -> service.create(req, schoolAdmin(UUID.randomUUID())))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.VALIDATION_FAILED));
    }

    @Test
    void createWithEmptyAudienceCodesIsRejected() {
        var req = new CreateAnnouncementRequest("Tiêu đề", "Nội dung", List.of(), false, null);

        assertThatThrownBy(() -> service.create(req, schoolAdmin(UUID.randomUUID())))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.VALIDATION_FAILED));
    }

    @Test
    void createWithInvalidStatusStringIsRejectedAsEnumInvalid() {
        var req = new CreateAnnouncementRequest("Tiêu đề", "Nội dung", List.of("TEACHER"), false, "ARCHIVED");

        assertThatThrownBy(() -> service.create(req, schoolAdmin(UUID.randomUUID())))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.ENUM_INVALID));
    }

    @Test
    void createDefaultsToDraftWithNullPublishedAtAndFirstRevisionRecordsActor() {
        UUID actorId = UUID.randomUUID();
        var req = new CreateAnnouncementRequest("Tiêu đề", "Nội dung", List.of("TEACHER"), false, null);

        AnnouncementDetailResponse detail = service.create(req, schoolAdmin(actorId));

        assertThat(detail.status()).isEqualTo("DRAFT");
        assertThat(detail.publishedAt()).isNull();

        ArgumentCaptor<AnnouncementRevision> revisionCaptor = ArgumentCaptor.forClass(AnnouncementRevision.class);
        verify(revisionRepository).saveAndFlush(revisionCaptor.capture());
        assertThat(revisionCaptor.getValue().getRevisedByUserId()).isEqualTo(actorId);
        assertThat(revisionCaptor.getValue().getNote()).isEqualTo("Công bố lần đầu");
    }

    @Test
    void updateFromPublishedToDraftIsRejectedAsInvalidState() {
        Announcement published = new Announcement();
        published.setCode("ANN-2026-050");
        published.setSchoolId(SCHOOL_ID);
        published.setStatus(AnnouncementStatus.PUBLISHED);
        published.setAudienceCodes(List.of("TEACHER"));
        when(announcementRepository.findBySchoolIdAndCode(SCHOOL_ID, "ANN-2026-050")).thenReturn(Optional.of(published));

        var req = new UpdateAnnouncementRequest(null, null, null, null, "DRAFT", null);

        assertThatThrownBy(() -> service.update("ANN-2026-050", req, schoolAdmin(UUID.randomUUID())))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.INVALID_STATE));
    }

    @Test
    void updateWithBlankRevisionNoteFallsBackToDefaultLabel() {
        Announcement draft = new Announcement();
        draft.setCode("ANN-2026-051");
        draft.setSchoolId(SCHOOL_ID);
        draft.setStatus(AnnouncementStatus.DRAFT);
        draft.setAudienceCodes(List.of("TEACHER"));
        when(announcementRepository.findBySchoolIdAndCode(SCHOOL_ID, "ANN-2026-051")).thenReturn(Optional.of(draft));

        var req = new UpdateAnnouncementRequest("Tiêu đề mới", null, null, null, null, "   ");
        service.update("ANN-2026-051", req, schoolAdmin(UUID.randomUUID()));

        ArgumentCaptor<AnnouncementRevision> revisionCaptor = ArgumentCaptor.forClass(AnnouncementRevision.class);
        verify(revisionRepository).saveAndFlush(revisionCaptor.capture());
        assertThat(revisionCaptor.getValue().getNote()).isEqualTo("Cập nhật thông báo");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-SEC-004 — thông báo DRAFT chỉ School Administration thấy: vai khác "
            + "nhận 404 ngay cả khi audience KHỚP vai mình (trạng thái chặn trước audience)")
    void getDetailHidesDraftFromNonAdminAsNotFound() {
        Announcement draft = new Announcement();
        draft.setCode("ANN-2026-052");
        draft.setSchoolId(SCHOOL_ID);
        draft.setStatus(AnnouncementStatus.DRAFT);
        draft.setAudienceCodes(List.of("PARENT_ALL"));
        when(announcementRepository.findBySchoolIdAndCode(SCHOOL_ID, "ANN-2026-052")).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.getDetail("ANN-2026-052", teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-SEC-005 — School Administration xem chi tiết thì CÓ revisions[]")
    void getDetailShowsDraftToSchoolAdminWithRevisions() {
        Announcement draft = new Announcement();
        draft.setCode("ANN-2026-053");
        draft.setSchoolId(SCHOOL_ID);
        draft.setStatus(AnnouncementStatus.DRAFT);
        draft.setAudienceCodes(List.of("PARENT_ALL"));
        when(announcementRepository.findBySchoolIdAndCode(SCHOOL_ID, "ANN-2026-053")).thenReturn(Optional.of(draft));
        when(revisionRepository.findByAnnouncementIdOrderByRevisedAtAsc(any())).thenReturn(List.of());

        AnnouncementDetailResponse detail = service.getDetail("ANN-2026-053", schoolAdmin(UUID.randomUUID()));

        assertThat(detail.status()).isEqualTo("DRAFT");
        assertThat(detail.revisions()).isNotNull();
    }

    /** Thông báo dựng sẵn để kiểm luật hiển thị (SEC-004/005). */
    private static Announcement announcement(String code, AnnouncementStatus status, java.util.List<String> audience) {
        Announcement ann = new Announcement();
        ann.setId(UUID.randomUUID());
        ann.setCode(code);
        ann.setSchoolId(SCHOOL_ID);
        ann.setTitle("Thông báo " + code);
        ann.setContent("Nội dung " + code);
        ann.setStatus(status);
        ann.setAudienceCodes(audience);
        ann.setAudienceLabel(String.join(", ", audience));
        return ann;
    }

    private void stubList(Announcement... rows) {
        when(announcementRepository.findAll(
                org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Announcement>>any(),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(rows)));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-SEC-004 — danh sách PUBLISHED lọc NGHIÊM theo audienceCodes: "
            + "Parent chỉ thấy thông báo gửi phụ huynh, Teacher chỉ thấy thông báo gửi giáo viên, Supplier thấy 0")
    void publishedAnnouncementsAreFilteredStrictlyByAudiencePerRole() {
        Announcement toParents = announcement("ANN-2026-112", AnnouncementStatus.PUBLISHED, List.of("PARENT_ALL"));
        Announcement toTeachers = announcement("ANN-2026-113", AnnouncementStatus.PUBLISHED, List.of("TEACHER"));
        stubList(toParents, toTeachers);

        var query = com.sfms.common.query.ListQuery.from(java.util.Map.of("page", "1", "pageSize", "8"));

        assertThat(service.list(query, parent()).items())
                .extracting("id").containsExactly("ANN-2026-112");
        assertThat(service.list(query, teacher()).items())
                .extracting("id").containsExactly("ANN-2026-113");

        AuthUser supplier = new AuthUser(UUID.randomUUID(), "U-9920", "ncc@sfms.vn", RoleKey.SUPPLIER, SCHOOL_ID, null);
        assertThat(service.list(query, supplier).items()).isEmpty();

        // School Administration thấy TẤT CẢ, không lọc theo audience.
        assertThat(service.list(query, schoolAdmin(UUID.randomUUID())).items()).hasSize(2);
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-SEC-005 — danh sách: chỉ School Administration có revisionCount; "
            + "vai khác dùng DTO không hề có field đó (không phải null)")
    void revisionCountIsExposedOnlyToSchoolAdminInTheListShape() {
        stubList(announcement("ANN-2026-111", AnnouncementStatus.PUBLISHED, List.of("PARENT_ALL")));
        var query = com.sfms.common.query.ListQuery.from(java.util.Map.of("page", "1", "pageSize", "8"));

        Object adminItem = service.list(query, schoolAdmin(UUID.randomUUID())).items().get(0);
        Object parentItem = service.list(query, parent()).items().get(0);

        assertThat(adminItem).isInstanceOf(com.sfms.mf10.dto.AnnouncementDtos.AnnouncementAdminListItem.class);
        assertThat(parentItem).isInstanceOf(com.sfms.mf10.dto.AnnouncementDtos.AnnouncementAudienceListItem.class);
        assertThat(com.sfms.mf10.dto.AnnouncementDtos.AnnouncementAudienceListItem.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("revisionCount", "revisions");
        assertThat(com.sfms.mf10.dto.AnnouncementDtos.AnnouncementAdminListItem.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .contains("revisionCount");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-SEC-005 — chi tiết: vai khác School Administration vẫn đọc được "
            + "title/content (tiền đề) nhưng KHÔNG nhận nội dung lịch sử sửa đổi")
    void revisionHistoryIsNeverReturnedToNonAdminRoles() {
        Announcement published = announcement("ANN-2026-111", AnnouncementStatus.PUBLISHED, List.of("TEACHER"));
        when(announcementRepository.findBySchoolIdAndCode(SCHOOL_ID, "ANN-2026-111"))
                .thenReturn(Optional.of(published));
        AnnouncementRevision rev = new AnnouncementRevision();
        rev.setNote("Sửa lần 1");
        rev.setRevisedByName("Nguyễn Văn Nam");
        when(revisionRepository.findByAnnouncementIdOrderByRevisedAtAsc(published.getId()))
                .thenReturn(List.of(rev, rev, rev));

        var forTeacher = service.getDetail("ANN-2026-111", teacher());
        assertThat(forTeacher.title()).isNotBlank();
        assertThat(forTeacher.content()).isNotBlank();
        // ⚠️ Lệch tài liệu: code THẬT trả revisions = mảng RỖNG cho vai khác (tài liệu mong "vắng
        // hẳn khóa"). Nội dung lịch sử không rò rỉ — đó là điều quan trọng — nhưng khóa vẫn có mặt.
        assertThat(forTeacher.revisions()).isEmpty();

        var forAdmin = service.getDetail("ANN-2026-111", schoolAdmin(UUID.randomUUID()));
        assertThat(forAdmin.revisions()).hasSize(3);
        assertThat(forAdmin.revisions().get(0).note()).isEqualTo("Sửa lần 1");
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-SEC-003 — thông báo của trường KHÁC không đọc/sửa được: truy vấn "
            + "luôn dùng schoolId của JWT nên trả 404, không lộ sự tồn tại")
    void crossSchoolAnnouncementAccessIsNotFound() {
        when(announcementRepository.findBySchoolIdAndCode(any(), anyString())).thenReturn(Optional.empty());
        AuthUser admin = schoolAdmin(UUID.randomUUID());

        assertThatThrownBy(() -> service.getDetail("ANN-2026-112", admin))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND));
        assertThatThrownBy(() -> service.update("ANN-2026-112",
                new UpdateAnnouncementRequest("Đổi trộm", null, null, null, null, null), admin))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND));

        ArgumentCaptor<UUID> schoolCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(announcementRepository, org.mockito.Mockito.times(2))
                .findBySchoolIdAndCode(schoolCaptor.capture(), eq("ANN-2026-112"));
        assertThat(schoolCaptor.getAllValues()).containsOnly(SCHOOL_ID);
        verify(announcementRepository, org.mockito.Mockito.never()).saveAndFlush(any());
    }

    @Test
    void getDetailUnknownCodeIs404() {
        when(announcementRepository.findBySchoolIdAndCode(any(), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail("ANN-2026-999", schoolAdmin(UUID.randomUUID())))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.NOT_FOUND));
    }
}
