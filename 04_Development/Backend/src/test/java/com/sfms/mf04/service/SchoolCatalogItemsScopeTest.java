package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.repository.CatalogChangeRequestRepository;
import com.sfms.mf04.repository.PartnershipCatalogItemRepository;
import com.sfms.mf04.repository.SupplierPartnershipRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code GET /catalog-items} — danh mục hợp đồng đã duyệt CỦA TRƯỜNG.
 *
 * <p><b>Vì sao có bộ test này.</b> Endpoint sinh ra để trả lời một câu hỏi đo được ngày
 * 09/08/2026: gợi ý thực đơn bằng AI đang ràng buộc theo danh mục nguyên liệu TOÀN HỆ THỐNG
 * (6 mục) trong khi trường chỉ được phép mua 4 — hai mặt hàng dư ra tạo ra món không mua nổi,
 * chỉ vỡ ở cổng ING-077 rất lâu sau, lúc kế hoạch đã lập xong.
 *
 * <p>Hai quyết định bị khoá ở đây:
 * <ul>
 *   <li>vai <b>đơn vị suất ăn</b> đọc được danh mục của trường mình phục vụ dù JWT không có
 *       {@code schoolId} (lớp lỗi đã lặp 5 lần — xem {@code ActorScope});</li>
 *   <li>vai <b>nhà cung cấp</b> phục vụ NHIỀU trường ⇒ không suy ra được một trường nào, phải
 *       trả rỗng chứ TUYỆT ĐỐI không đoán bừa một trường rồi lộ danh mục trường khác.</li>
 * </ul>
 */
class SchoolCatalogItemsScopeTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();

    private final SupplierPartnershipRepository partnerships = mock(SupplierPartnershipRepository.class);
    private final PartnershipCatalogItemRepository items = mock(PartnershipCatalogItemRepository.class);
    private final CatalogChangeRequestRepository changes = mock(CatalogChangeRequestRepository.class);
    private final ReferenceResolver refs = mock(ReferenceResolver.class);
    private final CodeGenerator codes = mock(CodeGenerator.class);

    private final CatalogChangeRequestServiceImpl service = new CatalogChangeRequestServiceImpl(
            partnerships, items, changes, refs, codes,
            Clock.fixed(Instant.parse("2026-08-09T03:00:00Z"), ZoneOffset.UTC));

    private static AuthUser actor(RoleKey role, UUID schoolId, UUID partnerId) {
        return new AuthUser(UUID.randomUUID(), "U-0001", "a@b.test", role, schoolId, partnerId);
    }

    private static ListQuery query() {
        return ListQuery.from(Map.of());
    }

    @Test
    @DisplayName("Đơn vị suất ăn (chỉ có partnerId) vẫn đọc được danh mục của trường đang phục vụ")
    @SuppressWarnings("unchecked")
    void catererResolvesSchoolFromPartnership() {
        when(refs.activeSchoolIdForPartner(PARTNER_ID)).thenReturn(Optional.of(SCHOOL_ID));
        when(items.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.listSchoolCatalogItems(query(), actor(RoleKey.SERVICE_CATERERS, null, PARTNER_ID));

        // Có suy ra được trường thì PHẢI thật sự truy vấn — trả rỗng mà không hỏi DB là đúng
        // hình dạng của lỗi "danh sách rỗng im lặng" đã gặp ở MF11.
        verify(items).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Nhà cung cấp phục vụ nhiều trường ⇒ trả rỗng, KHÔNG đoán một trường")
    @SuppressWarnings("unchecked")
    void supplierGetsEmptyInsteadOfGuessedSchool() {
        var result = service.listSchoolCatalogItems(query(), actor(RoleKey.SUPPLIER, null, PARTNER_ID));

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
        verify(items, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Tài khoản của trường ⇒ dùng thẳng schoolId trong JWT")
    @SuppressWarnings("unchecked")
    void schoolAdminUsesJwtSchoolId() {
        when(items.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.listSchoolCatalogItems(query(), actor(RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null));

        verify(refs, never()).activeSchoolIdForPartner(any());
        verify(items).findAll(any(Specification.class), any(Pageable.class));
    }
}
