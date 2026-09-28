package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.NotificationType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.Feedback;
import com.sfms.mf10.domain.FeedbackScope;
import com.sfms.mf10.domain.PortionSize;
import com.sfms.mf10.domain.SubmitterRole;
import com.sfms.mf10.dto.FeedbackDtos.EligibleMealDishRef;
import com.sfms.mf10.dto.FeedbackDtos.EligibleMealResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackAnonymizedListItem;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackAnonymizedResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackAttachmentResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackOwnerListItem;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackOwnerResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackResponseView;
import com.sfms.mf10.dto.FeedbackDtos.SubmitFeedbackRequest;
import com.sfms.mf10.dto.FeedbackDtos.UpdateFeedbackRequest;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.error.FbaMessages;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf10.repository.FeedbackAttachmentRepository;
import com.sfms.mf10.repository.FeedbackRepository;
import com.sfms.mf10.repository.FeedbackResponseRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/** Hiện thực {@link FeedbackService}. TDS-MF10_Backend.md §8.1, ADR-M10-1/2/5. */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackResponseRepository feedbackResponseRepository;
    private final FeedbackAttachmentRepository attachmentRepository;
    private final FeedbackAttachmentSupport attachmentSupport;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final FeedbackWindowPolicy windowPolicy;
    private final IllnessKeywordDetector illnessKeywordDetector;
    private final NotificationPort notificationPort;
    private final StorageService storageService;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                                FeedbackResponseRepository feedbackResponseRepository,
                                FeedbackAttachmentRepository attachmentRepository,
                                FeedbackAttachmentSupport attachmentSupport,
                                ReferenceResolver referenceResolver,
                                CodeGenerator codeGenerator,
                                FeedbackWindowPolicy windowPolicy,
                                IllnessKeywordDetector illnessKeywordDetector,
                                NotificationPort notificationPort,
                                StorageService storageService) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackResponseRepository = feedbackResponseRepository;
        this.attachmentRepository = attachmentRepository;
        this.attachmentSupport = attachmentSupport;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.windowPolicy = windowPolicy;
        this.illnessKeywordDetector = illnessKeywordDetector;
        this.notificationPort = notificationPort;
        this.storageService = storageService;
    }

    // ═════════════════════════ Đọc (D11 — ẩn danh với School Admin/Caterers, BR-145) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<?> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        boolean owner = actor.role() == RoleKey.PARENT || actor.role() == RoleKey.TEACHER;
        UUID submitterUserId = owner ? actor.userId() : null;

        var spec = FeedbackSpec.build(query, schoolId, submitterUserId);
        Page<Feedback> page = feedbackRepository.findAll(spec,
                query.toPageable(Sort.by(Sort.Direction.DESC, "submittedAt")));

        if (owner) {
            List<FeedbackOwnerListItem> items = page.getContent().stream().map(this::toOwnerListItem).toList();
            return PagedResponse.of(page, items, query.page(), query.pageSize());
        }
        List<FeedbackAnonymizedListItem> items = page.getContent().stream().map(this::toAnonymizedListItem).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Object getDetail(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        Feedback fb = feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
                .orElseThrow(FbaExceptions::notFound);
        if (fb.getSubmitterUserId().equals(actor.userId())) {
            return toOwnerResponse(fb);
        }
        boolean canRead = actor.role() == RoleKey.SCHOOL_ADMIN || actor.role() == RoleKey.SERVICE_CATERERS
                || actor.role() == RoleKey.MEDICAL_STAFF;
        if (!canRead) {
            throw FbaExceptions.accessDenied();
        }
        // BR-142 ngoại lệ: Medical Staff xem được danh tính CHỈ khi illnessKeyword=true.
        boolean revealIdentity = actor.role() == RoleKey.MEDICAL_STAFF && fb.isIllnessKeyword();
        return toAnonymizedResponse(fb, revealIdentity);
    }

    // ═════════════════════════ Gửi phản hồi (UC-085/086) ═════════════════════════

    @Override
    @Transactional
    public FeedbackOwnerResponse submit(SubmitFeedbackRequest req, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        SubmitterRole submitterRole = switch (actor.role()) {
            case PARENT -> SubmitterRole.PARENT;
            case TEACHER -> SubmitterRole.TEACHER;
            default -> throw FbaExceptions.accessDenied();
        };

        UUID mealSessionId = referenceResolver.distributionIdByCode(req.mealSessionId(), schoolId)
                .orElseThrow(FbaExceptions::notFound);
        // BR-139 — cả ĐANG mở phiên phản hồi VÀ ĐÃ phục vụ (servedAtOf rỗng ⇒ 409, M10-D06).
        windowPolicy.assertOpen(mealSessionId);

        FeedbackScope scope = parseScope(req.scope());
        UUID studentId = null;
        UUID classroomId = null;
        String className = req.classroomId();

        if (scope == FeedbackScope.CLASS) {
            if (submitterRole != SubmitterRole.TEACHER) {
                throw FbaExceptions.validationFailed("scope", "CLASS scope is only allowed for teachers");
            }
            classroomId = referenceResolver.classroomIdByCode(req.classroomId(), schoolId)
                    .orElseThrow(FbaExceptions::notFound);
            if (!referenceResolver.isHomeroomTeacher(classroomId, actor.userId())) {
                throw FbaExceptions.accessDenied();
            }
            className = req.classroomId();
        } else {
            if (req.studentId() == null || req.studentId().isBlank()) {
                throw FbaExceptions.validationFailed("studentId", "must not be blank for DISH/SESSION scope");
            }
            var studentRef = referenceResolver.studentByCode(req.studentId(), schoolId)
                    .orElseThrow(FbaExceptions::notFound);
            studentId = studentRef.id();
            classroomId = studentRef.classroomId();
            className = referenceResolver.classroomCode(classroomId);
            if (submitterRole == SubmitterRole.PARENT
                    && !referenceResolver.isParentLinkedToStudent(actor.userId(), studentId)) {
                throw FbaExceptions.accessDenied();
            }
        }

        UUID dishId = null;
        String dishName = null;
        if (scope == FeedbackScope.DISH) {
            if (req.dishId() == null || req.dishId().isBlank()) {
                throw FbaExceptions.validationFailed("dishId", "must not be blank for DISH scope");
            }
            dishId = referenceResolver.dishIdByCode(req.dishId()).orElseThrow(FbaExceptions::notFound);
            dishName = referenceResolver.dishName(dishId);
        }

        List<String> tags = req.tags() == null ? List.of() : req.tags();
        // BR-140 — Parent, rating<3 bắt buộc có tag.
        if (submitterRole == SubmitterRole.PARENT && req.rating() < 3 && tags.isEmpty()) {
            throw FbaExceptions.lowRatingNeedsTag();
        }

        if (feedbackRepository.existsBySubmitterUserIdAndMealSessionIdAndDishIdAndStudentIdAndDeletedAtIsNull(
                actor.userId(), mealSessionId, dishId, studentId)) {
            throw FbaExceptions.duplicateFeedback();
        }

        Feedback fb = new Feedback();
        fb.setCode(codeGenerator.next("mf10_feedback:" + Year.now().getValue(), "FB-" + Year.now().getValue(), 4));
        fb.setSchoolId(schoolId);
        fb.setSubmitterUserId(actor.userId());
        fb.setSubmitterRole(submitterRole);
        fb.setScope(scope);
        fb.setStudentId(studentId);
        fb.setClassroomId(classroomId);
        fb.setClassName(className);
        fb.setMealDate(req.mealDate());
        fb.setMealType(parseMealType(req.mealType()));
        fb.setMealSessionId(mealSessionId);
        fb.setDishId(dishId);
        fb.setDishName(dishName);
        fb.setRating(req.rating());
        fb.setTags(tags);
        fb.setComment(req.comment());
        fb.setPortionSize(parsePortionSize(req.portionSize()));
        fb.setIllnessKeyword(illnessKeywordDetector.detect(req.comment()));
        fb.setSubmittedAt(Instant.now());
        feedbackRepository.saveAndFlush(fb);

        notifyAfterSubmit(fb);

        return toOwnerResponse(fb);
    }

    /** BR-142/CF-M10-9 — thông báo sau khi lưu, KHÔNG chặn giao dịch nếu publish lỗi (best-effort). */
    private void notifyAfterSubmit(Feedback fb) {
        if (fb.isIllnessKeyword()) {
            notificationPort.publish(new NotificationCommand(fb.getSchoolId(), false,
                    List.of(RoleKey.MEDICAL_STAFF.key(), RoleKey.SCHOOL_ADMIN.key()),
                    NotificationType.ALLERGY_ALERT, "Phản hồi có dấu hiệu bệnh lý", FbaMessages.MSG_058,
                    "/school-admin/feedback", "Xem phản hồi"));
        }
        // CF-M10-9 (MSG-057, rating<3): KHÔNG có giá trị NotificationType nào trong 7 giá trị D6
        // khớp ngữ nghĩa "phản hồi tiêu cực" — phát hiện MỚI ở Bước 2 (D6 chỉ phủ menu/inspection/
        // capa/expiry/incident/recall/allergy). Không tự bịa giá trị enum thứ 8 (vi phạm D6) — để
        // TREO chờ Huy quyết định type nào dùng tạm, hoặc xin bổ sung D6. Ghi vào TDS §2.5.
    }

    // ═════════════════════════ Sửa / Xóa (UC-046 AF1/AF2) ═════════════════════════

    @Override
    @Transactional
    public FeedbackOwnerResponse update(String code, UpdateFeedbackRequest req, AuthUser actor) {
        Feedback fb = loadOwnedFeedback(code, actor);
        windowPolicy.assertOpen(fb.getMealSessionId());

        List<String> tags = req.tags() != null ? req.tags() : fb.getTags();
        int rating = req.rating() != null ? req.rating() : fb.getRating();
        if (fb.getSubmitterRole() == SubmitterRole.PARENT && rating < 3 && tags.isEmpty()) {
            throw FbaExceptions.lowRatingNeedsTag();
        }

        if (req.rating() != null) {
            fb.setRating(req.rating());
        }
        if (req.tags() != null) {
            fb.setTags(req.tags());
        }
        if (req.comment() != null) {
            fb.setComment(req.comment());
            fb.setIllnessKeyword(illnessKeywordDetector.detect(req.comment()));
        }
        feedbackRepository.saveAndFlush(fb);
        return toOwnerResponse(fb);
    }

    @Override
    @Transactional
    public void retract(String code, AuthUser actor) {
        Feedback fb = loadOwnedFeedback(code, actor);
        windowPolicy.assertOpen(fb.getMealSessionId());
        fb.setDeletedAt(Instant.now());
        feedbackRepository.saveAndFlush(fb);
    }

    private Feedback loadOwnedFeedback(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        Feedback fb = feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
                .orElseThrow(FbaExceptions::notFound);
        if (!fb.getSubmitterUserId().equals(actor.userId())) {
            throw FbaExceptions.accessDenied();
        }
        return fb;
    }

    // ═════════════════════════ BR-139 — bữa còn trong cửa sổ phản hồi ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<EligibleMealResponse> eligibleMeals(AuthUser actor, String studentCode) {
        UUID schoolId = resolveSchoolId(actor);
        UUID studentId;
        if (studentCode != null && !studentCode.isBlank()) {
            var ref = referenceResolver.studentByCode(studentCode, schoolId).orElseThrow(FbaExceptions::notFound);
            studentId = ref.id();
            if (actor.role() == RoleKey.PARENT && !referenceResolver.isParentLinkedToStudent(actor.userId(), studentId)) {
                throw FbaExceptions.accessDenied();
            }
        } else {
            throw FbaExceptions.validationFailed("studentId", "must not be blank");
        }
        return referenceResolver.servedMealsWithin(studentId, FeedbackWindowPolicy.WINDOW).stream()
                .map(m -> new EligibleMealResponse(m.code(), m.date(), m.mealType(), m.servedAt(),
                        windowPolicy.editableUntil(m.servedAt()), List.<EligibleMealDishRef>of()))
                // ⚠️ Simplification MVP: dishes[] rỗng — MF09 chưa expose tra cứu "món trong 1 phiên
                // phát suất" qua ReferenceResolver (serving_dishes là sự kiện đóng băng của MF09,
                // ADR-M09-1). Ghi nợ TDS §2.5, thêm khi cần.
                .toList();
    }

    @Override
    @Transactional
    public FeedbackAttachmentResponse uploadAttachment(String code, MultipartFile file, AuthUser actor) {
        Feedback fb = loadOwnedFeedback(code, actor);
        var attachment = attachmentSupport.store(fb.getSchoolId(), fb.getId(), file, actor.userId());
        return new FeedbackAttachmentResponse(attachment.getCode(), fb.getCode(), attachment.getOriginalName(),
                attachment.getContentType(), attachment.getSizeBytes(), attachment.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(String code, String attachmentCode, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        Feedback fb = feedbackRepository.findBySchoolIdAndCodeAndDeletedAtIsNull(schoolId, code)
                .orElseThrow(FbaExceptions::notFound);
        boolean allowed = fb.getSubmitterUserId().equals(actor.userId())
                || actor.role() == RoleKey.SCHOOL_ADMIN
                || (actor.role() == RoleKey.MEDICAL_STAFF && fb.isIllnessKeyword());
        if (!allowed) {
            throw FbaExceptions.accessDenied();
        }
        var attachment = attachmentRepository.findBySchoolIdAndFeedbackIdAndCode(schoolId, fb.getId(), attachmentCode)
                .orElseThrow(FbaExceptions::notFound);
        return storageService.load(attachment.getStorageKey());
    }

    // ═════════════════════════ Mapping ═════════════════════════

    FeedbackOwnerResponse toOwnerResponse(Feedback fb) {
        Instant servedAt = referenceResolver.servedAtOf(fb.getMealSessionId()).orElse(null);
        List<FeedbackResponseView> responses = feedbackResponseRepository
                .findByFeedbackIdOrderByRespondedAtAsc(fb.getId()).stream()
                .map(r -> new FeedbackResponseView(r.getCode(), fb.getCode(), r.getContent(), r.getRespondedAt(),
                        r.isLateResponse(), userDisplayName(r.getResponderUserId())))
                .toList();
        List<FeedbackAttachmentResponse> attachments = attachmentRepository
                .findByFeedbackIdOrderByCreatedAtAsc(fb.getId()).stream()
                .map(a -> new FeedbackAttachmentResponse(a.getCode(), fb.getCode(), a.getOriginalName(),
                        a.getContentType(), a.getSizeBytes(), a.getCreatedAt()))
                .toList();
        return new FeedbackOwnerResponse(fb.getCode(),
                fb.getStudentId() == null ? null : referenceResolver.studentCode(fb.getStudentId()),
                fb.getStudentId() == null ? null : referenceResolver.studentName(fb.getStudentId()),
                fb.getClassName(),
                fb.getClassroomId() == null ? null : referenceResolver.classroomCode(fb.getClassroomId()),
                referenceResolver.distributionCode(fb.getMealSessionId()),
                fb.getMealDate(), fb.getMealType().name(),
                fb.getDishId() == null ? null : referenceResolver.dishCode(fb.getDishId()),
                fb.getDishName(), fb.getScope().name(), fb.getRating(), fb.getTags(), fb.getComment(),
                fb.getPortionSize() == null ? null : fb.getPortionSize().name(), fb.getSubmittedAt(),
                fb.isIllnessKeyword(), windowPolicy.editableUntil(servedAt), !responses.isEmpty(),
                responses, attachments);
    }

    private FeedbackOwnerListItem toOwnerListItem(Feedback fb) {
        boolean responded = feedbackResponseRepository.existsByFeedbackId(fb.getId());
        return new FeedbackOwnerListItem(fb.getCode(),
                fb.getStudentId() == null ? null : referenceResolver.studentCode(fb.getStudentId()),
                fb.getStudentId() == null ? null : referenceResolver.studentName(fb.getStudentId()),
                fb.getClassName(), fb.getMealDate(), fb.getMealType().name(),
                fb.getDishId() == null ? null : referenceResolver.dishCode(fb.getDishId()),
                fb.getDishName(), fb.getRating(), fb.getTags(), fb.getComment(), fb.isIllnessKeyword(),
                responded, fb.getSubmittedAt());
    }

    private FeedbackAnonymizedListItem toAnonymizedListItem(Feedback fb) {
        boolean responded = feedbackResponseRepository.existsByFeedbackId(fb.getId());
        return new FeedbackAnonymizedListItem(fb.getCode(), fb.getClassName(), fb.getMealDate(),
                fb.getMealType().name(), fb.getDishId() == null ? null : referenceResolver.dishCode(fb.getDishId()),
                fb.getDishName(), fb.getRating(), fb.getTags(), fb.getComment(), fb.isIllnessKeyword(),
                responded, fb.getSubmittedAt());
    }

    private FeedbackAnonymizedResponse toAnonymizedResponse(Feedback fb, boolean revealIdentity) {
        List<FeedbackResponseView> responses = feedbackResponseRepository
                .findByFeedbackIdOrderByRespondedAtAsc(fb.getId()).stream()
                .map(r -> new FeedbackResponseView(r.getCode(), fb.getCode(), r.getContent(), r.getRespondedAt(),
                        r.isLateResponse(), userDisplayName(r.getResponderUserId())))
                .toList();
        return new FeedbackAnonymizedResponse(fb.getCode(),
                revealIdentity && fb.getStudentId() != null ? referenceResolver.studentCode(fb.getStudentId()) : null,
                revealIdentity && fb.getStudentId() != null ? referenceResolver.studentName(fb.getStudentId()) : null,
                fb.getClassName(), fb.getMealDate(), fb.getMealType().name(),
                fb.getDishId() == null ? null : referenceResolver.dishCode(fb.getDishId()),
                fb.getDishName(), fb.getRating(), fb.getTags(), fb.getComment(), fb.isIllnessKeyword(),
                !responses.isEmpty(), fb.getSubmittedAt(), responses);
    }

    private String userDisplayName(UUID userId) {
        String[] user = referenceResolver.userById(userId);
        return user == null ? null : user[2];
    }

    private static FeedbackScope parseScope(String raw) {
        try {
            return FeedbackScope.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw FbaExceptions.enumInvalid("scope", "must be one of DISH, SESSION, CLASS");
        }
    }

    private static com.sfms.common.enums.MealType parseMealType(String raw) {
        try {
            return com.sfms.common.enums.MealType.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw FbaExceptions.enumInvalid("mealType", "must be one of BREAKFAST, LUNCH, SNACK");
        }
    }

    private static PortionSize parsePortionSize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return PortionSize.valueOf(raw);
        } catch (IllegalArgumentException e) {
            throw FbaExceptions.enumInvalid("portionSize", "must be one of TOO_SMALL, ADEQUATE, TOO_LARGE");
        }
    }

    // ═════════════════════════ Phạm vi trường ═════════════════════════

    @Override
    public UUID resolveSchoolId(AuthUser actor) {
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
