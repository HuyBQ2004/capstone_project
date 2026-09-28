package com.sfms.mf07.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.domain.InspectionEvidence;
import com.sfms.mf07.domain.InspectionSession;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceResponse;
import com.sfms.mf07.dto.EvidenceDtos.EvidenceSummary;
import com.sfms.mf07.error.InspExceptions;
import com.sfms.mf07.repository.InspectionEvidenceRepository;
import com.sfms.mf07.repository.InspectionSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/** Ảnh bằng chứng dùng chung (BR-083, BR-207, BR-211) — D10-BIS, ADR-M07-04. */
@Service
public class EvidenceServiceImpl implements EvidenceService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final InspectionEvidenceRepository evidenceRepository;
    private final InspectionSessionRepository sessionRepository;
    private final StorageService storageService;
    private final ReferenceResolver referenceResolver;
    private final Clock clock;

    public EvidenceServiceImpl(InspectionEvidenceRepository evidenceRepository,
                                InspectionSessionRepository sessionRepository, StorageService storageService,
                                ReferenceResolver referenceResolver, Clock clock) {
        this.evidenceRepository = evidenceRepository;
        this.sessionRepository = sessionRepository;
        this.storageService = storageService;
        this.referenceResolver = referenceResolver;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EvidenceResponse upload(String ownerType, String ownerId, MultipartFile file, String gps, AuthUser actor) {
        if (file == null || file.isEmpty()) {
            throw InspExceptions.validationFailed("file", "required");
        }
        String key = "mf07-evidence/" + UUID.randomUUID() + suffixOf(file.getOriginalFilename());
        storageService.store(key, file);   // D10-BIS mục 2 — KHÔNG dùng URL công khai trả về

        InspectionEvidence evidence = new InspectionEvidence();
        evidence.setOwnerType(ownerType);
        evidence.setOwnerId(parseUuid(ownerId));
        evidence.setStorageKey(key);
        evidence.setFileName(file.getOriginalFilename() == null ? "evidence" : file.getOriginalFilename());
        evidence.setSizeBytes(file.getSize());
        evidence.setSha256(sha256(file));   // ADR-M07-04 — chống sửa ảnh bằng chứng
        evidence.setGps(gps);
        evidence.setUploadedAt(clock.instant());
        evidence.setUploadedBy(actor.userId());

        evidenceRepository.saveAndFlush(evidence);
        return new EvidenceResponse(evidence.getId().toString(), evidence.getFileName(), evidence.getSizeBytes(),
                TIMESTAMP.format(evidence.getUploadedAt().atZone(java.time.ZoneOffset.UTC)));
    }

    @Override
    @Transactional
    public EvidenceResponse uploadForSession(String sessionCode, String ownerType, MultipartFile file, String gps,
                                              AuthUser actor) {
        InspectionSession session = loadSessionInScope(sessionCode, actor);
        return upload(ownerType, session.getId().toString(), file, gps, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile download(String sessionCode, String evidenceId, AuthUser actor) {
        loadSessionInScope(sessionCode, actor); // BR-099 — kiểm phạm vi đọc trước khi trả file
        InspectionEvidence evidence = evidenceRepository.findById(parseUuid(evidenceId))
                .orElseThrow(InspExceptions::notFound);
        return new DownloadedFile(storageService.load(evidence.getStorageKey()), evidence.getFileName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceSummary> listForSession(String sessionCode, AuthUser actor) {
        InspectionSession session = loadSessionInScope(sessionCode, actor); // BR-099
        return evidenceRepository.findByOwnerIdOrderByUploadedAtDesc(session.getId()).stream()
                .map(e -> new EvidenceSummary(e.getId().toString(), e.getOwnerType(), e.getFileName(),
                        TIMESTAMP.format(e.getUploadedAt().atZone(java.time.ZoneOffset.UTC))))
                .toList();
    }

    /**
     * BR-099 — Caterer chỉ đọc/ghi phiên của đúng đơn vị mình.
     *
     * <p>BUG tìm thấy 10/08/2026 (tái lập ổn định qua {@code POST
     * /inspections/{code}/evidences/{ownerType}} → 404 "Resource not found"): bản trước đọc
     * THẲNG {@code actor.schoolId()} — nhưng cột {@code school_id} trên {@code UserAccount} của
     * tài khoản SERVICE_CATERERS có thể {@code NULL} (chỉ {@code partnerId} được set khi tạo
     * tài khoản qua đối tác, xem {@code UserMapper.activeSchoolUuid()} — cùng khoảng trống dữ
     * liệu, xử lý bằng fallback qua {@code activeSchoolIdForPartner}). JWT phản ánh đúng cột đó
     * nên {@code actor.schoolId()} cũng null theo, khiến {@code findBySchoolIdAndCode(null,
     * code)} luôn rỗng — 404 với MỌI phiên, không phải lỗi hiếm. {@code InspectionServiceImpl}
     * đã tự vá bằng {@code resolveSchoolId()} (fallback qua partnerId) cho MỌI method khác;
     * lớp này khai lại logic scope riêng nên bị bỏ sót — nay dùng cùng fallback.
     */
    private InspectionSession loadSessionInScope(String sessionCode, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        InspectionSession session = sessionRepository.findBySchoolIdAndCode(schoolId, sessionCode)
                .orElseThrow(InspExceptions::notFound);
        // `actor.partnerId()` luôn null cho vai trò SERVICE_CATERERS (chỉ SUPPLIER có claim
        // này) — phải tra qua supplier_partnerships theo schoolId (bug tìm thấy 06/08/2026,
        // cùng lớp lỗi đã sửa ở InspectionServiceImpl).
        if (actor.role() == RoleKey.SERVICE_CATERERS) {
            UUID catererPartnerId = actor.partnerId() != null
                    ? actor.partnerId() : referenceResolver.approvedCatererPartnerId(schoolId);
            if (!session.getPartnerId().equals(catererPartnerId)) {
                throw InspExceptions.notFound();
            }
        }
        return session;
    }

    private UUID resolveSchoolId(AuthUser actor) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId()).orElse(null);
        }
        return null;
    }

    private static UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw InspExceptions.validationFailed("id", "invalid UUID: " + raw);
        }
    }

    private static String suffixOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot);
    }

    private static String sha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | java.io.IOException e) {
            throw new IllegalStateException("Failed to compute evidence sha256", e);
        }
    }
}
