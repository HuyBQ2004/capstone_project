package com.sfms.mf10.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Announcement;
import com.sfms.mf10.domain.AnnouncementRevision;
import com.sfms.mf10.domain.AnnouncementStatus;
import com.sfms.mf10.dto.AnnouncementDtos.AnnouncementAdminListItem;
import com.sfms.mf10.dto.AnnouncementDtos.AnnouncementAudienceListItem;
import com.sfms.mf10.dto.AnnouncementDtos.AnnouncementDetailResponse;
import com.sfms.mf10.dto.AnnouncementDtos.AnnouncementRevisionView;
import com.sfms.mf10.dto.AnnouncementDtos.CreateAnnouncementRequest;
import com.sfms.mf10.dto.AnnouncementDtos.UpdateAnnouncementRequest;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.repository.AnnouncementRepository;
import com.sfms.mf10.repository.AnnouncementRevisionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Hiện thực {@link AnnouncementService}. TDS-MF10_Backend.md §8.1, UC-017/089/105/125/126, ADR-M10-3. */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    /** CF-M10-7 — nhãn hiển thị của mã đối tượng chuẩn hoá; khối chưa liệt kê dùng khuôn "Phụ huynh khối N". */
    private static final Map<String, String> AUDIENCE_LABELS = Map.of(
            "PARENT_ALL", "Toàn thể phụ huynh",
            "TEACHER", "Giáo viên",
            "MEDICAL_STAFF", "Y tế",
            "CATERER", "Đơn vị suất ăn",
            "SUPPLIER", "Nhà cung cấp");

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementRevisionRepository revisionRepository;
    private final ReferenceResolver referenceResolver;
    private final com.sfms.common.code.CodeGenerator codeGenerator;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                    AnnouncementRevisionRepository revisionRepository,
                                    ReferenceResolver referenceResolver,
                                    com.sfms.common.code.CodeGenerator codeGenerator) {
        this.announcementRepository = announcementRepository;
        this.revisionRepository = revisionRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional
    public AnnouncementDetailResponse create(CreateAnnouncementRequest req, AuthUser actor) {
        requireSchoolAdmin(actor);
        if (isBlank(req.title()) || isBlank(req.content()) || req.audienceCodes() == null || req.audienceCodes().isEmpty()) {
            throw FbaExceptions.validationFailed("audienceCodes", "title, content and target group are required");
        }
        AnnouncementStatus status = req.status() == null || req.status().isBlank()
                ? AnnouncementStatus.DRAFT : parseStatus(req.status());

        Announcement ann = new Announcement();
        ann.setCode(codeGenerator.next("mf10_announcement:" + Year.now().getValue(),
                "ANN-" + Year.now().getValue(), 3));
        ann.setSchoolId(actor.schoolId());
        ann.setTitle(req.title());
        ann.setContent(req.content());
        ann.setAudienceCodes(req.audienceCodes());
        ann.setAudienceLabel(labelOf(req.audienceCodes()));
        ann.setStatus(status);
        ann.setUrgent(req.urgent());
        ann.setAuthorUserId(actor.userId());
        ann.setAuthorName(displayName(actor.userId()));
        ann.setPublishedAt(status == AnnouncementStatus.PUBLISHED ? Instant.now() : null);
        announcementRepository.saveAndFlush(ann);

        addRevision(ann, actor, "Công bố lần đầu");
        // BR-138/047 — TREO (cùng lớp gap với CF-M10-9): common/enums/NotificationType (D6, 7
        // giá trị) KHÔNG có mục "thông báo trường" ⇒ phát hiện MỚI ở Bước 2, chưa tự chọn type
        // sai để gửi. Ghi vào TDS §2.5.

        return toDetail(ann, true);
    }

    @Override
    @Transactional
    public AnnouncementDetailResponse update(String code, UpdateAnnouncementRequest req, AuthUser actor) {
        requireSchoolAdmin(actor);
        Announcement ann = announcementRepository.findBySchoolIdAndCode(actor.schoolId(), code)
                .orElseThrow(FbaExceptions::notFound);

        AnnouncementStatus newStatus = ann.getStatus();
        if (req.status() != null && !req.status().isBlank()) {
            newStatus = parseStatus(req.status());
            if (ann.getStatus() == AnnouncementStatus.PUBLISHED && newStatus == AnnouncementStatus.DRAFT) {
                throw FbaExceptions.invalidState();
            }
        }
        if (req.title() != null) {
            ann.setTitle(req.title());
        }
        if (req.content() != null) {
            ann.setContent(req.content());
        }
        if (req.audienceCodes() != null) {
            ann.setAudienceCodes(req.audienceCodes());
            ann.setAudienceLabel(labelOf(req.audienceCodes()));
        }
        if (req.urgent() != null) {
            ann.setUrgent(req.urgent());
        }
        if (newStatus == AnnouncementStatus.PUBLISHED && ann.getPublishedAt() == null) {
            ann.setPublishedAt(Instant.now());
        }
        ann.setStatus(newStatus);
        announcementRepository.saveAndFlush(ann);

        String note = req.revisionNote() == null || req.revisionNote().isBlank()
                ? "Cập nhật thông báo" : req.revisionNote();
        addRevision(ann, actor, note);

        return toDetail(ann, true);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementDetailResponse getDetail(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        Announcement ann = announcementRepository.findBySchoolIdAndCode(schoolId, code)
                .orElseThrow(FbaExceptions::notFound);
        boolean isAdmin = actor.role() == RoleKey.SCHOOL_ADMIN;
        if (ann.getStatus() == AnnouncementStatus.DRAFT && !isAdmin) {
            throw FbaExceptions.notFound();
        }
        return toDetail(ann, isAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<?> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        boolean isAdmin = actor.role() == RoleKey.SCHOOL_ADMIN;
        var spec = AnnouncementSpec.build(query, schoolId, !isAdmin);
        Page<Announcement> page = announcementRepository.findAll(spec,
                query.toPageable(Sort.by(Sort.Direction.DESC, "createdAt")));

        List<Announcement> visible = isAdmin ? page.getContent()
                : page.getContent().stream().filter(a -> audienceMatches(a, actor)).toList();

        if (isAdmin) {
            List<AnnouncementAdminListItem> items = visible.stream().map(this::toAdminListItem).toList();
            return PagedResponse.of(page, items, query.page(), query.pageSize());
        }
        List<AnnouncementAudienceListItem> items = visible.stream().map(this::toAudienceListItem).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
        // ⚠️ Simplification MVP: lọc audience thực hiện SAU khi đã phân trang ở DB (giống
        // NotificationServiceImpl) ⇒ `total`/`pageCount` có thể lớn hơn số dòng thực sự thấy
        // được nếu một trang lẫn nhiều audience khác. Chấp nhận được ở MVP (khối lượng nhỏ,
        // §4.4); ghi nợ TDS §2.5 nếu cần lọc chính xác ở DB.
    }

    // ═════════════════════════ Mapping / hỗ trợ ═════════════════════════

    private void addRevision(Announcement ann, AuthUser actor, String note) {
        AnnouncementRevision rev = new AnnouncementRevision();
        rev.setAnnouncementId(ann.getId());
        rev.setRevisedByUserId(actor.userId());
        rev.setRevisedByName(displayName(actor.userId()));
        rev.setNote(note);
        rev.setRevisedAt(Instant.now());
        revisionRepository.saveAndFlush(rev);
    }

    private AnnouncementDetailResponse toDetail(Announcement ann, boolean includeRevisions) {
        List<AnnouncementRevisionView> revisions = includeRevisions
                ? revisionRepository.findByAnnouncementIdOrderByRevisedAtAsc(ann.getId()).stream()
                        .map(r -> new AnnouncementRevisionView(r.getRevisedAt(), r.getRevisedByName(), r.getNote()))
                        .toList()
                : List.of();
        return new AnnouncementDetailResponse(ann.getCode(), ann.getTitle(), ann.getContent(),
                ann.getAudienceLabel(), ann.getAudienceCodes(), ann.getStatus().name(), ann.getPublishedAt(),
                ann.getAuthorName(), ann.isUrgent(), revisions);
    }

    private AnnouncementAdminListItem toAdminListItem(Announcement ann) {
        long revisionCount = revisionRepository.countByAnnouncementId(ann.getId());
        return new AnnouncementAdminListItem(ann.getCode(), ann.getTitle(), ann.getAudienceLabel(),
                ann.getAudienceCodes(), ann.getStatus().name(), ann.getPublishedAt(), ann.getAuthorName(),
                ann.isUrgent(), (int) revisionCount);
    }

    private AnnouncementAudienceListItem toAudienceListItem(Announcement ann) {
        return new AnnouncementAudienceListItem(ann.getCode(), ann.getTitle(), ann.getAudienceLabel(),
                ann.getAudienceCodes(), ann.getStatus().name(), ann.getPublishedAt(), ann.getAuthorName(),
                ann.isUrgent());
    }

    /** CF-M10-6 — vai ≠ School Administration chỉ thấy audience khớp vai mình. Simplification:
     *  Parent thấy mọi PARENT_ALL/PARENT_GRADE_* (không lọc theo khối con — ghi nợ TDS §2.5). */
    private boolean audienceMatches(Announcement ann, AuthUser actor) {
        List<String> codes = ann.getAudienceCodes();
        return switch (actor.role()) {
            case PARENT -> codes.stream().anyMatch(c -> c.equals("PARENT_ALL") || c.startsWith("PARENT_GRADE_"));
            case TEACHER -> codes.contains("TEACHER");
            case MEDICAL_STAFF -> codes.contains("MEDICAL_STAFF");
            case SERVICE_CATERERS -> codes.contains("CATERER");
            case SUPPLIER -> codes.contains("SUPPLIER");
            default -> false;
        };
    }

    private String labelOf(List<String> codes) {
        return codes.stream().map(c -> AUDIENCE_LABELS.getOrDefault(c, gradeLabel(c))).reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private static String gradeLabel(String code) {
        if (code != null && code.startsWith("PARENT_GRADE_")) {
            return "Phụ huynh khối " + code.substring("PARENT_GRADE_".length());
        }
        return code;
    }

    private String displayName(UUID userId) {
        String[] user = referenceResolver.userById(userId);
        return user == null ? null : user[2];
    }

    private static AnnouncementStatus parseStatus(String raw) {
        try {
            return AnnouncementStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw FbaExceptions.enumInvalid("status", "must be one of DRAFT, PUBLISHED");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void requireSchoolAdmin(AuthUser actor) {
        if (actor.role() != RoleKey.SCHOOL_ADMIN) {
            throw FbaExceptions.accessDenied();
        }
    }

    private UUID resolveSchoolId(AuthUser actor) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId()).orElse(null);
        }
        if (actor.role() == RoleKey.PARENT) {
            return referenceResolver.firstSchoolIdForParent(actor.userId()).orElse(null);
        }
        return null;
    }
}
