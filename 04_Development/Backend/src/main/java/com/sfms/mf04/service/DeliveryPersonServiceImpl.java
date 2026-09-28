package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.BiometricStatus;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.DeliveryPerson;
import com.sfms.mf04.dto.DeliveryPersonDtos.CreateDeliveryPersonRequest;
import com.sfms.mf04.dto.DeliveryPersonDtos.DeliveryPersonResponse;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.mapper.DeliveryPersonMapper;
import com.sfms.mf04.port.FaceRecognitionException;
import com.sfms.mf04.port.FaceRecognitionPort;
import com.sfms.mf04.port.NoFaceDetectedException;
import com.sfms.mf04.repository.DeliveryPersonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.scope.ActorScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/** UC-019/110/068/028 — BR-050/051, ADR-M04-3/M04-5. */
@Service
public class DeliveryPersonServiceImpl implements DeliveryPersonService {

    private final DeliveryPersonRepository deliveryPersonRepository;
    private final DeliveryPersonMapper deliveryPersonMapper;
    private final NationalIdCipher cipher;
    private final FaceRecognitionPort faceRecognitionPort;
    private final FileValidationSupport fileValidation;
    private final CodeGenerator codeGenerator;
    private final ReferenceResolver referenceResolver;
    private final SchoolPartnerLinkResolver schoolPartnerLinkResolver;
    private final Clock clock;
    private final long faceImageMaxBytes;

    public DeliveryPersonServiceImpl(DeliveryPersonRepository deliveryPersonRepository,
                                     DeliveryPersonMapper deliveryPersonMapper,
                                     NationalIdCipher cipher,
                                     FaceRecognitionPort faceRecognitionPort,
                                     FileValidationSupport fileValidation,
                                     CodeGenerator codeGenerator,
                                     ReferenceResolver referenceResolver,
                                     SchoolPartnerLinkResolver schoolPartnerLinkResolver,
                                     Clock clock,
                                     @Value("${sfms.mf04.face-image.max-bytes:5242880}") long faceImageMaxBytes) {
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.deliveryPersonMapper = deliveryPersonMapper;
        this.cipher = cipher;
        this.faceRecognitionPort = faceRecognitionPort;
        this.fileValidation = fileValidation;
        this.codeGenerator = codeGenerator;
        this.referenceResolver = referenceResolver;
        this.schoolPartnerLinkResolver = schoolPartnerLinkResolver;
        this.clock = clock;
        this.faceImageMaxBytes = faceImageMaxBytes;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DeliveryPersonResponse> list(ListQuery query, AuthUser actor) {
        List<UUID> allowedPartnerIds = null;
        if (!actor.isSystemAdmin()) {
            if (actor.role() == RoleKey.SUPPLIER) {
                allowedPartnerIds = List.of(actor.partnerId());
            } else if (actor.role() == RoleKey.SERVICE_CATERERS || actor.role() == RoleKey.SCHOOL_ADMIN) {
                UUID schoolId = null;
                if (actor.role() == RoleKey.SCHOOL_ADMIN) {
                    schoolId = actor.schoolId();
                } else {
                    schoolId = ActorScope.schoolIdOf(actor, referenceResolver);
                }
                if (schoolId != null) {
                    allowedPartnerIds = schoolPartnerLinkResolver.partnerIdsConnectedToSchool(schoolId);
                } else {
                    allowedPartnerIds = List.of();
                }
            } else {
                allowedPartnerIds = List.of();
            }
        }
        Page<DeliveryPerson> page = deliveryPersonRepository.findAll(
                DeliveryPersonSpec.build(query, allowedPartnerIds), query.toPageable());
        List<DeliveryPersonResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public DeliveryPersonResponse create(CreateDeliveryPersonRequest req, AuthUser actor) {
        String hash = cipher.hash(req.nationalId());
        if (deliveryPersonRepository.existsByNationalIdHash(hash)) {
            throw SupExceptions.nationalIdDuplicate();                          // BR-050, UC-019 AF1
        }

        String code;
        if (req.id() != null && !req.id().isBlank()) {
            if (deliveryPersonRepository.existsByCode(req.id())) {
                throw SupExceptions.duplicateValue("id", req.id());             // D5
            }
            code = req.id();
        } else {
            code = codeGenerator.next("delivery_persons", "DP", 3);
        }

        DeliveryPerson dp = new DeliveryPerson();
        dp.setCode(code);
        dp.setPartnerId(actor.partnerId());
        dp.setFullName(req.fullName());
        dp.setNationalIdEnc(cipher.encrypt(req.nationalId()));
        dp.setNationalIdHash(hash);
        dp.setPhone(req.phone());
        dp.setVehiclePlate(req.vehiclePlate());
        dp.setBiometricStatus(BiometricStatus.NOT_REGISTERED);
        deliveryPersonRepository.saveAndFlush(dp);

        return toResponse(dp);
    }

    /**
     * UC-068 — M04-D03: đăng ký 2 bước (PENDING_REGISTRATION → REGISTERED). Ở dev,
     * {@code FaceRecognitionPortStub} trả đồng bộ nên gộp làm một request (200 trực tiếp);
     * lỗi FRS KHÔNG đổi trạng thái (UC-068 AF1).
     */
    @Override
    @Transactional
    public DeliveryPersonResponse registerFace(String code, MultipartFile image, AuthUser actor) {
        DeliveryPerson dp = requireInScope(code, actor);
        fileValidation.validate(image, faceImageMaxBytes, FileValidationSupport.IMAGE_CONTENT_TYPES);

        String faceId;
        try {
            faceId = faceRecognitionPort.enroll(image.getBytes());
        } catch (NoFaceDetectedException e) {
            // 422 — lỗi ẢNH của người dùng (FRS không thấy mặt), KHÁC lỗi hạ tầng dưới đây.
            // Dùng lại validationFailed() có sẵn (SUP-422) thay vì tự đặt mã lỗi mới.
            throw SupExceptions.validationFailed("image", "no_face_detected");
        } catch (FaceRecognitionException | java.io.IOException e) {
            throw SupExceptions.faceRecognitionFailed();                        // UC-068 AF1 — KHÔNG đổi trạng thái
        }

        dp.setFaceId(faceId);
        dp.setBiometricStatus(BiometricStatus.REGISTERED);
        dp.setBiometricRegisteredAt(clock.instant());
        deliveryPersonRepository.saveAndFlush(dp);

        return toResponse(dp);
    }

    /** UC-028 — gọi FRS xóa TRƯỚC; chỉ đổi DB khi FRS xác nhận OK (BR-051, không hoàn tác). */
    @Override
    @Transactional
    public DeliveryPersonResponse deleteFace(String code, AuthUser actor) {
        DeliveryPerson dp = requireInScope(code, actor);

        if (dp.getFaceId() != null) {
            try {
                faceRecognitionPort.delete(dp.getFaceId());
            } catch (FaceRecognitionException e) {
                throw SupExceptions.faceRecognitionFailed();                    // UC-028 AF2 — giữ nguyên trạng thái
            }
        }

        dp.setFaceId(null);
        dp.setBiometricStatus(BiometricStatus.NOT_REGISTERED);
        dp.setBiometricRegisteredAt(null);
        deliveryPersonRepository.saveAndFlush(dp);

        return toResponse(dp);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    /** ADR-M04-3/M04-D10 — ngoài phạm vi dữ liệu ⇒ 404, không 403. */
    private DeliveryPerson requireInScope(String code, AuthUser actor) {
        return deliveryPersonRepository.findByCodeAndPartnerId(code, actor.partnerId())
                .orElseThrow(SupExceptions::notFound);
    }

    /** PDPD/UC-110 — luôn trả CCCD dạng che: 7 số đầu + 5 ký tự {@code x}. */
    private String mask(String plainNationalId) {
        if (plainNationalId == null || plainNationalId.length() < 12) {
            return "xxxxxxxxxxxx";
        }
        return plainNationalId.substring(0, 7) + "xxxxx";
    }

    /**
     * Giải mã nội bộ CHỈ để tính tiền tố hiển thị dạng che (UC-110) — giá trị đầy đủ
     * KHÔNG BAO GIỜ rời khỏi phương thức này ra ngoài API (PDPD, ADR-M04-5).
     *
     * <p>⚠️ Bản ghi seed bằng SQL thô ({@code db/testseed}, {@code V7__seed_mf04.sql}) chỉ có
     * ciphertext PLACEHOLDER (không mã hóa thật qua {@link NationalIdCipher}) — giải mã sẽ ném
     * lỗi GCM tag. Fallback về chuỗi che chung thay vì làm vỡ toàn bộ danh sách.
     */
    private DeliveryPersonResponse toResponse(DeliveryPerson dp) {
        String maskedNationalId;
        try {
            maskedNationalId = mask(cipher.decrypt(dp.getNationalIdEnc()));
        } catch (RuntimeException e) {
            maskedNationalId = "xxxxxxxxxxxx";
        }
        return deliveryPersonMapper.toResponse(dp, referenceResolver.partnerCode(dp.getPartnerId()), maskedNationalId);
    }
}
