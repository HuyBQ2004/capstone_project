package com.sfms.iam.mapper;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.UserDtos.ProfileResponse;
import com.sfms.iam.dto.UserDtos.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * MapStruct — {@link UserAccount} → DTO.
 *
 * <p>Đây là <b>ranh giới bảo mật</b>: {@code passwordHash}, {@code googleSub}, và UUID nội bộ
 * dừng lại ở đây. MF01-TC-SEC-010 quét mọi response và assert 0 lần khớp
 * {@code passwordHash|googleSub|tokenHash} + 0 field {@code id} dạng UUID. Map thẳng entity ra
 * JSON (bỏ qua lớp này) là cách rò rỉ toàn bộ hash mật khẩu điển hình nhất.
 *
 * <p>Dùng abstract class thay interface để inject {@link ReferenceResolver} — cần cho việc
 * dịch khóa ngoại UUID → mã nghiệp vụ (D1).
 */
@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected ReferenceResolver refs;

    @Mapping(target = "id", source = "code")
    @Mapping(target = "role", expression = "java(entity.roleKey())")
    @Mapping(target = "schoolId", source = "schoolId", qualifiedByName = "schoolCode")
    @Mapping(target = "school", source = "schoolId", qualifiedByName = "schoolName")
    @Mapping(target = "partnerId", source = "partnerId", qualifiedByName = "partnerCode")
    public abstract UserResponse toResponse(UserAccount entity);

    public abstract List<UserResponse> toResponseList(List<UserAccount> entities);

    @Mapping(target = "id", source = "code")
    @Mapping(target = "role", expression = "java(entity.roleKey())")
    @Mapping(target = "schoolId", source = "schoolId", qualifiedByName = "schoolCode")
    @Mapping(target = "school", source = "schoolId", qualifiedByName = "schoolName")
    @Mapping(target = "partnerId", source = "partnerId", qualifiedByName = "partnerCode")
    @Mapping(target = "activeSchoolId", source = ".", qualifiedByName = "activeSchoolCode")
    @Mapping(target = "activeSchool", source = ".", qualifiedByName = "activeSchoolName")
    public abstract ProfileResponse toProfile(UserAccount entity);

    /**
     * "Trường mà cấu hình vận hành áp cho tài khoản này" — xem Javadoc field
     * {@code ProfileResponse.activeSchoolId}.
     *
     * <p>Chỉ suy cho SERVICE_CATERERS: BR-EXCLUSIVE-CATERER cho quan hệ 1-1 trường ↔ đơn vị
     * suất ăn nên "trường của tôi" là xác định. NCC phục vụ NHIỀU trường ⇒ trả {@code null},
     * không chọn bừa một trường (chọn bừa sẽ cho NCC đọc cấu hình của trường không liên quan).
     */
    @Named("activeSchoolCode")
    protected String activeSchoolCode(UserAccount entity) {
        UUID id = activeSchoolUuid(entity);
        return id == null ? null : refs.schoolCode(id);
    }

    /** Nhãn hiển thị của trường ở {@code activeSchoolId} — đi cặp, xem Javadoc DTO. */
    @Named("activeSchoolName")
    protected String activeSchoolName(UserAccount entity) {
        UUID id = activeSchoolUuid(entity);
        return id == null ? null : refs.schoolName(id);
    }

    private UUID activeSchoolUuid(UserAccount entity) {
        if (entity.getSchoolId() != null) {
            return entity.getSchoolId();
        }
        if (entity.roleKey() == RoleKey.SERVICE_CATERERS && entity.getPartnerId() != null) {
            return refs.activeSchoolIdForPartner(entity.getPartnerId()).orElse(null);
        }
        return null;
    }

    /** D1 — JSON trả mã nghiệp vụ của trường, không phải UUID. */
    @Named("schoolCode")
    protected String schoolCode(UUID schoolId) {
        return refs.schoolCode(schoolId);
    }

    /** Review B2 — trả kèm nhãn hiển thị để FE khỏi phải nối bằng tên. */
    @Named("schoolName")
    protected String schoolName(UUID schoolId) {
        return refs.schoolName(schoolId);
    }

    /** D1 — khóa cách ly đa-tenant, giữ nguyên khuôn {@code SUP-001}. */
    @Named("partnerCode")
    protected String partnerCode(UUID partnerId) {
        return refs.partnerCode(partnerId);
    }
}
