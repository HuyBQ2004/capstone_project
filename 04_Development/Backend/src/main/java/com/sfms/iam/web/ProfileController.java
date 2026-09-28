package com.sfms.iam.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.dto.UserDtos.ProfileResponse;
import com.sfms.iam.dto.UserDtos.UpdateProfileRequest;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.iam.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * MF01 — hồ sơ cá nhân (UC-059/097/138). §16: mọi vai trò, phạm vi <b>Own</b>.
 *
 * <p>Không có endpoint {@code /profile/{code}}: chủ thể luôn là chính người cầm JWT.
 * Đây là lý do MF01-TC-SEC-005 mục (a)/(f) không có đường nào để đọc/sửa hồ sơ người khác —
 * không phải nhờ kiểm tra thêm, mà nhờ <b>không tồn tại tham số</b> để tấn công.
 */
@Tag(name = "MF01 — User Management & Access Control")
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(
            summary = "Xem hồ sơ của chính mình",
            description = "UC-059, UC-138 · BR-012 — chủ thể lấy từ JWT; "
                    + "query param userId (nếu có) bị bỏ qua hoàn toàn")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "IAM-005 — MSG-004 thiếu / hết hạn token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/me")
    public ProfileResponse getMine(@CurrentUser AuthUser actor) {
        return profileService.getMine(actor);
    }

    @Operation(
            summary = "Cập nhật hồ sơ của chính mình",
            description = "UC-097 · BR-012 — chỉ sửa được name/phone; "
                    + "role, schoolId, status không nằm trong DTO")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "401", description = "IAM-005 — MSG-004",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "IAM-422 — MSG-138 SĐT sai định dạng (BR-012)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping("/me")
    public ProfileResponse updateMine(@CurrentUser AuthUser actor,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateMine(actor, request);
    }

    @Operation(
            summary = "Tải lên ảnh hồ sơ",
            description = "UC-097 — dung lượng tối đa 10MB (MSG-008) và định dạng PDF/JPG/PNG "
                    + "(MSG-009); kiểm theo magic bytes, không chỉ theo phần mở rộng")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "422", description = "IAM-010 — MSG-008 quá dung lượng "
            + "hoặc MSG-009 sai định dạng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse updatePhoto(@CurrentUser AuthUser actor,
                                       @RequestPart("file") MultipartFile file) {
        return profileService.updatePhoto(actor, file);
    }
}
