package com.sfms.mf05.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.dto.MenuDtos.MenuPublicResponse;
import com.sfms.mf05.dto.MenuDtos.MenuResponse;
import com.sfms.mf05.error.MnuExceptions;
import com.sfms.mf05.mapper.MenuMapper;
import com.sfms.mf05.repository.MenuRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Trợ giúp dùng chung giữa {@link MenuService}/{@link MenuWorkflowService}: nạp bản ghi
 * trong phạm vi trường (404-not-403, §16 ghi chú cuối), suy {@code schoolId} khi JWT không
 * mang (Caterer đa trường), gán field dẫn xuất khi build response.
 */
@Component
public class MenuAssembler {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final AllergyConflictChecker allergyConflictChecker;
    private final ReferenceResolver referenceResolver;

    public MenuAssembler(MenuRepository menuRepository, MenuMapper menuMapper,
                          AllergyConflictChecker allergyConflictChecker, ReferenceResolver referenceResolver) {
        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
        this.allergyConflictChecker = allergyConflictChecker;
        this.referenceResolver = referenceResolver;
    }

    /**
     * §16 ghi chú cuối / MF01 {@code requireInScope()} idiom — ngoài phạm vi trường (Caterer
     * chạm thực đơn trường khác, v.v.) trả <b>404</b>, KHÔNG 403 (403 lộ sự tồn tại bản ghi).
     */
    public Menu loadInScope(String code, UUID schoolId) {
        return menuRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(MnuExceptions::notFound);
    }

    /**
     * §4.3 TDS — Caterer chỉ thao tác GHI thực đơn do đơn vị mình lập. Đây là "đúng phạm vi,
     * sai vai trò/chủ sở hữu" ⇒ <b>403</b> (TC-SEC-004), khác với {@link #loadInScope} (ngoài
     * phạm vi ⇒ 404). Bản ghi ĐÃ đọc được (cùng trường) nên tồn tại không còn là bí mật.
     */
    public void assertCatererOwnership(Menu menu, AuthUser actor) {
        if (actor.role() == RoleKey.SERVICE_CATERERS
                && !java.util.Objects.equals(menu.getCreatedBySupplierId(), actor.partnerId())) {
            throw MnuExceptions.accessDenied();
        }
    }

    /** JWT thường mang {@code schoolId}; Caterer đa trường (VD {@code U-0022}) thì tra qua đối tác. */
    public UUID resolveSchoolId(AuthUser actor) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId())
                    .orElseThrow(MnuExceptions::notFound);
        }
        throw MnuExceptions.accessDenied();
    }

    public MenuResponse toResponse(Menu menu) {
        int dishCount = menu.getMenuDishes().size();
        // Gọi checker MỘT lần: trước đây `check()` chỉ lấy `.size()` rồi vứt danh sách đi, nên
        // màn duyệt phải dựng lại cùng phép tính ở client bằng dữ liệu tải từ 2 endpoint khác.
        var conflicts = allergyConflictChecker.check(menu);
        int allergyConflicts = conflicts.size();
        // Chỉ món CHƯA có suất thay thế mới là thứ chặn duyệt (BR-063) — cùng ngữ nghĩa với
        // `unresolvedAllergyDishes()` mà frontend đang tự tính.
        java.util.List<String> allergyConflictDishes = conflicts.stream()
                .filter(c -> !c.resolvedBySubstitute())
                .map(com.sfms.mf05.dto.MenuDtos.AllergyConflict::dishName)
                .distinct()
                .toList();
        String createdByLabel = referenceResolver.partnerName(menu.getCreatedBySupplierId());
        String createdBySupplierCode = referenceResolver.partnerCode(menu.getCreatedBySupplierId());
        String approvedByLabel = approvedByLabel(menu);
        String gradeName = referenceResolver.gradeLabel(menu.getGradeId());
        return menuMapper.toResponse(menu, dishCount, allergyConflicts, allergyConflictDishes,
                createdByLabel, createdBySupplierCode, approvedByLabel, gradeName);
    }

    public MenuPublicResponse toPublicResponse(Menu menu) {
        int dishCount = menu.getMenuDishes().size();
        int allergyConflicts = allergyConflictChecker.check(menu).size();
        String createdByLabel = referenceResolver.partnerName(menu.getCreatedBySupplierId());
        String gradeName = referenceResolver.gradeLabel(menu.getGradeId());
        return menuMapper.toPublicResponse(menu, dishCount, allergyConflicts, createdByLabel, gradeName);
    }

    private String approvedByLabel(Menu menu) {
        if (menu.getApprovedByUserId() == null) {
            return null;
        }
        String[] user = referenceResolver.userById(menu.getApprovedByUserId());
        return user == null ? null : user[2];
    }
}
