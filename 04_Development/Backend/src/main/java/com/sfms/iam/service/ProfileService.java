package com.sfms.iam.service;

import com.sfms.common.storage.StorageService;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.UserDtos.ProfileResponse;
import com.sfms.iam.dto.UserDtos.UpdateProfileRequest;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.mapper.UserMapper;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * UC-059 Profile Manage · UC-097 Update Profile · UC-138 View Profile.
 *
 * <p>Chủ thể <b>luôn</b> là chính người cầm JWT ({@code AuthUser}). Không có phương thức nào
 * nhận {@code userId} tùy ý — MF01-TC-SEC-005 mục (a)/(f): {@code PATCH /profile/me} kèm
 * {@code {"userId":"U-0001"}} hay {@code ?userId=U-0001} vẫn chỉ chạm hồ sơ của chính mình.
 */
@Service
public class ProfileService {

    private final UserAccountRepository userRepository;
    private final UserMapper userMapper;
    private final StorageService storageService;
    private final long maxPhotoBytes;
    private final Set<String> allowedTypes;

    /**
     * Magic bytes — kiểm <b>nội dung</b>, không chỉ phần mở rộng.
     * MF01-TC-024: file {@code .exe} đổi tên thành {@code .jpg} phải bị chặn.
     */
    private static final byte[] MAGIC_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] MAGIC_PNG = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] MAGIC_PDF = {'%', 'P', 'D', 'F'};

    public ProfileService(UserAccountRepository userRepository,
                          UserMapper userMapper,
                          StorageService storageService,
                          @Value("${sfms.profile-photo.max-bytes:10485760}") long maxPhotoBytes,
                          @Value("${sfms.profile-photo.allowed-types}") String allowedTypes) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.storageService = storageService;
        this.maxPhotoBytes = maxPhotoBytes;
        this.allowedTypes = Set.copyOf(List.of(allowedTypes.split(",")));
    }

    /** UC-059 / UC-138. */
    @Transactional(readOnly = true)
    public ProfileResponse getMine(AuthUser actor) {
        return userMapper.toProfile(require(actor));
    }

    /** UC-097 — chỉ sửa {@code name}/{@code phone}; role/school/status không nằm trong tầm với. */
    @Transactional
    public ProfileResponse updateMine(AuthUser actor, UpdateProfileRequest request) {
        UserAccount user = require(actor);
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());       // BR-012 đã validate ở @Pattern của DTO
        }
        userRepository.saveAndFlush(user);
        return userMapper.toProfile(user);
    }

    /**
     * UC-097 — ảnh hồ sơ.
     *
     * <p>⚠️ <b>CF-4 treo</b>: UC-097 narrative nói tối đa <b>5MB</b>, MSG-008 nói <b>10MB</b>.
     * Theo <b>D7</b> (nội dung message lấy nguyên văn {@code error_message.md}) ⇒ ngưỡng thực thi
     * là <b>10MB</b>, khớp đúng câu chữ mà người dùng đọc được. Chặn ở 5MB trong khi message
     * nói 10MB làm người dùng không hiểu vì sao (MF01-TC-023).
     *
     * <p>⚠️ MSG-009 cho phép <b>PDF, JPG, PNG</b> trong khi UC-097 bước 3 chỉ nói JPG/JPEG/PNG.
     * Theo MSG-009 (D7) và ghi nhận lệch để Huy đối chiếu — cùng nhóm CF-3/CF-4.
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-010} (422) MSG-008 hoặc MSG-009
     */
    @Transactional
    public ProfileResponse updatePhoto(AuthUser actor, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw IamExceptions.unsupportedFileType();
        }
        if (file.getSize() > maxPhotoBytes) {
            throw IamExceptions.fileTooLarge();                 // MSG-008 nguyên văn
        }
        if (!allowedTypes.contains(file.getContentType()) || !hasAllowedMagicBytes(file)) {
            throw IamExceptions.unsupportedFileType();          // MSG-009 nguyên văn
        }

        UserAccount user = require(actor);
        String key = "avatars/" + user.getCode() + suffixOf(file.getContentType());
        user.setAvatarUrl(storageService.store(key, file));
        userRepository.saveAndFlush(user);
        return userMapper.toProfile(user);
    }

    /** Chặn SVG (XSS) và file đổi tên: quyết định theo nội dung thật của file. */
    private boolean hasAllowedMagicBytes(MultipartFile file) {
        try {
            byte[] head = new byte[8];
            int read = file.getInputStream().read(head);
            if (read < 4) {
                return false;
            }
            return startsWith(head, MAGIC_JPEG) || startsWith(head, MAGIC_PNG)
                    || startsWith(head, MAGIC_PDF);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String suffixOf(String contentType) {
        return switch (contentType == null ? "" : contentType) {
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }

    private UserAccount require(AuthUser actor) {
        return userRepository.findById(actor.userId()).orElseThrow(IamExceptions::notFound);
    }
}
