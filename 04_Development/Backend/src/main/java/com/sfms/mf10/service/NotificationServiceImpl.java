package com.sfms.mf10.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.NotificationType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.domain.NotificationAudience;
import com.sfms.mf10.domain.NotificationPreference;
import com.sfms.mf10.domain.NotificationRead;
import com.sfms.mf10.domain.UserNotification;
import com.sfms.mf10.dto.NotificationDtos.NotificationPreferenceResponse;
import com.sfms.mf10.dto.NotificationDtos.NotificationResponse;
import com.sfms.mf10.dto.NotificationDtos.UpdatePreferenceRequest;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.repository.NotificationPreferenceRepository;
import com.sfms.mf10.repository.NotificationReadRepository;
import com.sfms.mf10.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Hiện thực {@link NotificationService}. TDS-MF10_Backend.md §8.1, UC-058/136/088. */
@Service
public class NotificationServiceImpl implements NotificationService {

    /** M10-D07 — 3 loại KHÔNG tắt được (BR-138). */
    private static final List<String> CRITICAL_TYPES = List.of(
            NotificationType.INCIDENT_ALERT.name(), NotificationType.FOOD_RECALL.name(),
            NotificationType.ALLERGY_ALERT.name());

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository readRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final ReferenceResolver referenceResolver;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                    NotificationReadRepository readRepository,
                                    NotificationPreferenceRepository preferenceRepository,
                                    ReferenceResolver referenceResolver) {
        this.notificationRepository = notificationRepository;
        this.readRepository = readRepository;
        this.preferenceRepository = preferenceRepository;
        this.referenceResolver = referenceResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> listForCurrentUser(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        List<UserNotification> visible = notificationRepository.findVisibleToSchool(schoolId).stream()
                .filter(n -> isVisibleToRole(n, actor.role())).toList();

        Set<UUID> ids = visible.stream().map(UserNotification::getId).collect(Collectors.toSet());
        Set<UUID> readIds = readRepository.findByUserIdAndNotificationIdIn(actor.userId(), ids).stream()
                .map(NotificationRead::getNotificationId).collect(Collectors.toSet());

        int total = visible.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) total / query.pageSize()));
        int safePage = Math.min(Math.max(1, query.page()), pageCount);
        int from = Math.min((safePage - 1) * query.pageSize(), total);
        int to = Math.min(from + query.pageSize(), total);

        List<NotificationResponse> items = visible.subList(from, to).stream()
                .map(n -> toResponse(n, readIds.contains(n.getId()))).toList();
        return new PagedResponse<>(items, total, safePage, query.pageSize(), pageCount);
        // ⚠️ Simplification MVP — cùng lý do NotificationRepository.findVisibleToSchool: lọc
        // theo vai + phân trang thực hiện ở tầng Java, không đẩy xuống DB (JSONB containment).
    }

    @Override
    @Transactional
    public void markRead(String code, AuthUser actor) {
        UserNotification n = notificationRepository.findByCode(code).orElseThrow(FbaExceptions::notFound);
        if (!isVisibleToRole(n, actor.role())) {
            throw FbaExceptions.accessDenied();
        }
        if (readRepository.existsByNotificationIdAndUserId(n.getId(), actor.userId())) {
            return;   // idempotent
        }
        NotificationRead read = new NotificationRead();
        read.setNotificationId(n.getId());
        read.setUserId(actor.userId());
        read.setReadAt(Instant.now());
        readRepository.saveAndFlush(read);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getMyPreference(AuthUser actor) {
        return preferenceRepository.findById(actor.userId())
                .map(this::toPreferenceResponse)
                .orElseGet(() -> new NotificationPreferenceResponse(defaultTypeEnabled(), true,
                        LocalTime.of(22, 0), LocalTime.of(6, 0), CRITICAL_TYPES));
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updateMyPreference(UpdatePreferenceRequest req, AuthUser actor) {
        NotificationPreference pref = preferenceRepository.findById(actor.userId()).orElseGet(() -> {
            NotificationPreference created = new NotificationPreference();
            created.setUserId(actor.userId());
            created.setTypeEnabled(defaultTypeEnabled());
            created.setCreatedAt(Instant.now());
            return created;
        });
        if (req.typeEnabled() != null) {
            pref.setTypeEnabled(req.typeEnabled());
        }
        if (req.emailFallback() != null) {
            pref.setEmailFallback(req.emailFallback());
        }
        if (req.quietStart() != null) {
            pref.setQuietStart(req.quietStart());
        }
        if (req.quietEnd() != null) {
            pref.setQuietEnd(req.quietEnd());
        }
        pref.setUpdatedAt(Instant.now());
        preferenceRepository.saveAndFlush(pref);
        return toPreferenceResponse(pref);
    }

    // ═════════════════════════ hỗ trợ ═════════════════════════

    private NotificationResponse toResponse(UserNotification n, boolean read) {
        return new NotificationResponse(n.getCode(), n.getAudience().name(), n.getRecipientRoles(),
                n.getType().name(), n.getTitle(), n.getContent(), n.getSentAt(), read, n.isCritical(),
                n.getLink(), n.getLinkLabel());
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreference pref) {
        return new NotificationPreferenceResponse(pref.getTypeEnabled(), pref.isEmailFallback(),
                pref.getQuietStart(), pref.getQuietEnd(), CRITICAL_TYPES);
    }

    private static boolean isVisibleToRole(UserNotification n, RoleKey role) {
        if (n.getAudience() == NotificationAudience.ALL) {
            return true;
        }
        return n.getRecipientRoles() != null && n.getRecipientRoles().contains(role.key());
    }

    private static Map<String, Boolean> defaultTypeEnabled() {
        Map<String, Boolean> defaults = new LinkedHashMap<>();
        for (NotificationType type : NotificationType.values()) {
            defaults.put(type.name(), type != NotificationType.EXPIRY_ALERT);
        }
        return defaults;
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
