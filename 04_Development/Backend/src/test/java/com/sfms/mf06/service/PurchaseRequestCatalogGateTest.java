package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.dto.PurchaseRequestDtos.CreateRequest;
import com.sfms.mf06.dto.PurchaseRequestDtos.ItemRequest;
import com.sfms.mf06.repository.PurchaseItemRepository;
import com.sfms.mf06.repository.PurchaseRequestRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import com.sfms.mf10.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cổng DANH MỤC HỢP ĐỒNG ở {@code POST /purchase-requests} — ING-077 (Nam chốt 08/08/2026).
 *
 * <p><b>Lỗ hổng cổng này bịt.</b> Trước đó {@code create()} chỉ kiểm nguyên liệu có TỒN TẠI
 * trong danh mục toàn hệ thống, nên đặt được <b>bất kỳ nguyên liệu nào từ bất kỳ NCC nào</b> —
 * kể cả thứ họ chưa đăng ký bán và chưa ai duyệt giá. Đo thật 08/08/2026:
 * {@code partnership_catalog_items} <b>rỗng hoàn toàn</b> mà đơn vẫn tạo → giao → nhập kho trót
 * lọt, tức toàn bộ luồng {@code catalog-change-request} → trường duyệt <b>chưa từng có tác
 * dụng gì</b>.
 *
 * <p><b>Đừng nới cổng này cho "chạy được".</b> Nguồn sự thật là danh mục HỢP ĐỒNG đã duyệt,
 * KHÔNG phải {@code supplier_products} — bảng đó là bảng giá chung do NCC tự khai, không có
 * cổng duyệt nào.
 */
class PurchaseRequestCatalogGateTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID SUPPLIER_ID = UUID.randomUUID();
    private static final UUID INGREDIENT_ID = UUID.randomUUID();

    private PurchaseRequestRepository requestRepo;
    private PurchaseItemRepository itemRepo;
    private ReferenceResolver refs;
    private PurchaseRequestService service;

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "bgh@truong.test",
                RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static CreateRequest request() {
        return new CreateRequest("PO-TEST-1", "SUP-202", LocalDate.now().plusDays(2),
                List.of(new ItemRequest("ING-GAO-TE", new BigDecimal("5"), "kg", null)));
    }

    @BeforeEach
    void setUp() {
        requestRepo = mock(PurchaseRequestRepository.class);
        itemRepo = mock(PurchaseItemRepository.class);
        SupplierBatchRepository batchRepo = mock(SupplierBatchRepository.class);
        SupplierProductRepository productRepo = mock(SupplierProductRepository.class);
        com.sfms.mf06.repository.InventoryBatchRepository inventoryBatchRepo =
                mock(com.sfms.mf06.repository.InventoryBatchRepository.class);
        refs = mock(ReferenceResolver.class);
        CodeGenerator codes = mock(CodeGenerator.class);

        when(refs.partnerIdByCode("SUP-202")).thenReturn(Optional.of(SUPPLIER_ID));
        when(refs.ingredientIdByCode("ING-GAO-TE")).thenReturn(Optional.of(INGREDIENT_ID));
        when(refs.ingredientName(INGREDIENT_ID)).thenReturn("Gạo tẻ");
        when(requestRepo.existsBySchoolIdAndCode(any(), anyString())).thenReturn(false);
        when(codes.next(anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn("PO-TEST-1");

        service = new PurchaseRequestService(requestRepo, itemRepo, batchRepo, productRepo, inventoryBatchRepo, refs,
                codes, mock(NotificationPort.class), Clock.fixed(Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Nguyên liệu NGOÀI danh mục đã duyệt ⇒ 422 ING-077, KHÔNG ghi dòng đơn nào")
    void ingredientOutsideApprovedCatalogIsRejected() {
        when(refs.isIngredientInApprovedCatalog(SUPPLIER_ID, SCHOOL_ID, INGREDIENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.create(request(), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("ING-077");
                    // Trả kèm mã nguyên liệu để FE chỉ đúng dòng hỏng, không báo chung chung.
                    assertThat(api.getDetails()).isNotEmpty();
                });

        verify(itemRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Nguyên liệu ĐÃ ĐƯỢC TRƯỜNG DUYỆT vào danh mục hợp đồng ⇒ tạo đơn bình thường")
    void ingredientInApprovedCatalogIsAccepted() {
        when(refs.isIngredientInApprovedCatalog(SUPPLIER_ID, SCHOOL_ID, INGREDIENT_ID)).thenReturn(true);
        when(refs.partnershipCatalogPackaging(any(), any(), any())).thenReturn(Optional.empty());

        service.create(request(), schoolAdmin());

        verify(itemRepo).saveAndFlush(any());
    }

    /*
     * Đơn KHÔNG chỉ định NCC thì không có danh mục hợp đồng nào để đối chiếu — cổng bỏ qua,
     * đúng như code. Test này ghi lại giới hạn đó cho rõ ràng, không phải để tán thành nó:
     * nếu sau này bắt buộc mọi đơn phải có NCC thì đây là chỗ sửa.
     */
    @Test
    @DisplayName("Đơn không có NCC ⇒ cổng không áp (giới hạn đã biết)")
    void requestWithoutSupplierSkipsGate() {
        CreateRequest noSupplier = new CreateRequest("PO-TEST-2", null, LocalDate.now().plusDays(2),
                List.of(new ItemRequest("ING-GAO-TE", new BigDecimal("5"), "kg", null)));

        service.create(noSupplier, schoolAdmin());

        verify(itemRepo).saveAndFlush(any());
        verify(refs, never()).isIngredientInApprovedCatalog(any(), any(), any());
    }
}
