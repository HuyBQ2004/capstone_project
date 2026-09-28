package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.Delivery;
import com.sfms.mf06.domain.SupplierBatch;
import com.sfms.mf06.domain.SupplierBatchStatus;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingLineRequest;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingSessionCreateRequest;
import com.sfms.mf06.repository.CatalogIngredientRepository;
import com.sfms.mf06.repository.DeliveryRepository;
import com.sfms.mf06.repository.InventoryBatchRepository;
import com.sfms.mf06.repository.InventoryTransactionRepository;
import com.sfms.mf06.repository.PurchaseRequestRepository;
import com.sfms.mf06.repository.ReceivingSessionRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MỘT LÔ NCC CÓ THỂ NHẬN NHIỀU ĐỢT / NHIỀU TRƯỜNG, KHÔNG VƯỢT SỐ LƯỢNG ĐĂNG KÝ
 * (sửa 10/08/2026, tiếp nối mục A8 backlog 08/08/2026).
 *
 * <p><b>Bối cảnh:</b> chốt chặn 08/08/2026 ("một lô chỉ nhận một lần") đúng khi phiếu nhận
 * chưa có ô số lượng — nó chặn cả bug cộng khống LẪN nghiệp vụ hợp lệ (một lô sản xuất
 * thường được NCC chia giao cho nhiều trường, hoặc giao nhiều đợt cho cùng một trường).
 * Nay `receiving_lines.received_quantity` (V61) ghi đúng lượng nhận từng lần, và
 * `supplier_batches.remaining_quantity` là nguồn sự thật cho luật mới: tổng đã nhận (kể cả
 * lần này) không được vượt số lượng lô NCC đăng ký.
 *
 * <p><b>⚠️ ĐỪNG NỚI LUẬT NÀY THÊM NỮA:</b> nhận vượt {@code remainingQuantity} vẫn phải bị
 * chặn 409 — nới ra là quay lại đúng lỗi cộng khống tồn kho mà bản 08/08/2026 đã sửa.
 */
class ReceivingBatchOnceTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final UUID INGREDIENT_ID = UUID.randomUUID();

    private SupplierBatchRepository batchRepo;
    private InventoryBatchRepository inventoryRepo;
    private SupplierBatch batch;
    private ReceivingSessionService service;

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "bgh@truong.test",
                RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static ReceivingSessionCreateRequest request(BigDecimal receivedQuantity) {
        return new ReceivingSessionCreateRequest("DLV-1", null,
                new BigDecimal("21.0"), new BigDecimal("105.0"), 1, null, "test-device",
                List.of(new ReceivingLineRequest("SB-GAO-TE", "ING-GAO-TE", "DRY", false, null, null,
                        receivedQuantity)));
    }

    @BeforeEach
    void setUp() {
        batchRepo = mock(SupplierBatchRepository.class);
        ReceivingSessionRepository sessionRepo = mock(ReceivingSessionRepository.class);
        inventoryRepo = mock(InventoryBatchRepository.class);
        DeliveryRepository deliveryRepo = mock(DeliveryRepository.class);
        PurchaseRequestRepository purchaseRequestRepo = mock(PurchaseRequestRepository.class);
        InventoryTransactionRepository txRepo = mock(InventoryTransactionRepository.class);
        CatalogIngredientRepository catalogRepo = mock(CatalogIngredientRepository.class);
        ReferenceResolver refs = mock(ReferenceResolver.class);
        GeofenceValidator geofence = mock(GeofenceValidator.class);
        CodeGenerator codes = mock(CodeGenerator.class);

        batch = new SupplierBatch();
        batch.setCode("SB-GAO-TE");
        batch.setPartnerId(PARTNER_ID);
        batch.setQuantity(new BigDecimal("50"));
        batch.setRemainingQuantity(new BigDecimal("50"));
        batch.setUnit("kg");
        batch.setExpirationDate(LocalDate.now().plusDays(30));
        batch.setStatus(SupplierBatchStatus.ACTIVE);

        Delivery delivery = new Delivery();
        delivery.setCode("DLV-1");
        // ReceivingSessionService.create() (gate bổ sung 13/08/2026) giờ chặn mở phiên nhận
        // hàng nếu chuyến giao chưa DELIVERED — set thẳng ở đây để các ca dưới thực sự chạm
        // tới logic remaining_quantity đang kiểm, không bị chặn sớm bởi gate trạng thái.
        delivery.setStatus(com.sfms.mf06.domain.DeliveryStatus.DELIVERED);

        when(deliveryRepo.findBySchoolIdAndCode(SCHOOL_ID, "DLV-1")).thenReturn(Optional.of(delivery));
        when(batchRepo.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(batch));
        when(refs.partnerStatus(PARTNER_ID)).thenReturn(Optional.of("APPROVED"));
        when(refs.ingredientIdByCode("ING-GAO-TE")).thenReturn(Optional.of(INGREDIENT_ID));
        var ingredient = new com.sfms.mf06.domain.CatalogIngredient();
        ingredient.setName("Gạo tẻ");
        when(catalogRepo.findById(INGREDIENT_ID)).thenReturn(Optional.of(ingredient));
        when(geofence.verify(any(), any(), any())).thenReturn(true);
        when(geofence.isEnforced()).thenReturn(false);
        when(inventoryRepo.findBySchoolIdAndCode(any(), any())).thenReturn(Optional.empty());
        when(codes.next(any(), any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn("RCV-1");

        service = new ReceivingSessionService(sessionRepo, deliveryRepo, purchaseRequestRepo, batchRepo, inventoryRepo,
                txRepo, catalogRepo, refs, geofence, codes,
                Clock.fixed(Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC),
                mock(JdbcTemplate.class), mock(com.sfms.common.storage.StorageService.class),
                mock(FileValidationSupport.class), mock(com.sfms.mf10.port.NotificationPort.class));
    }

    @Test
    @DisplayName("Lô đã HẾT số lượng còn lại ⇒ 409, KHÔNG cộng tồn thêm")
    void receivingBatchWithNoRemainingIsRejected() {
        batch.setRemainingQuantity(BigDecimal.ZERO);
        batch.setStatus(SupplierBatchStatus.DELIVERED);

        assertThatThrownBy(() -> service.create(request(new BigDecimal("10")), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("SB-GAO-TE");

        verify(inventoryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Nhận VƯỢT số lượng còn lại của lô ⇒ 409, KHÔNG cộng tồn khống")
    void receivingMoreThanRemainingIsRejected() {
        assertThatThrownBy(() -> service.create(request(new BigDecimal("999")), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("SB-GAO-TE");

        verify(inventoryRepo, never()).saveAndFlush(any());
        assertThat(batch.getRemainingQuantity()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("Nhận MỘT PHẦN lô (30/50kg) ⇒ trừ đúng lượng còn lại, lô vẫn ACTIVE để giao tiếp")
    void receivingPartialQuantityLeavesBatchActiveWithReducedRemaining() {
        service.create(request(new BigDecimal("30")), schoolAdmin());

        assertThat(batch.getRemainingQuantity()).isEqualByComparingTo("20");
        assertThat(batch.getStatus()).isEqualTo(SupplierBatchStatus.ACTIVE);
        verify(inventoryRepo).saveAndFlush(any());
    }

    @Test
    @DisplayName("Nhận ĐỦ lượng còn lại (50/50kg) ⇒ lô chuyển DELIVERED, hết sạch để giao tiếp")
    void receivingExactRemainingClosesBatch() {
        service.create(request(new BigDecimal("50")), schoolAdmin());

        assertThat(batch.getRemainingQuantity()).isEqualByComparingTo("0");
        assertThat(batch.getStatus()).isEqualTo(SupplierBatchStatus.DELIVERED);
        verify(inventoryRepo).saveAndFlush(any());
    }

    @Test
    @DisplayName("Không gửi receivedQuantity (hoặc <= 0) ở dòng KHÔNG bị từ chối ⇒ 422")
    void missingReceivedQuantityOnAcceptedLineIsRejected() {
        assertThatThrownBy(() -> service.create(request(null), schoolAdmin()))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> service.create(request(BigDecimal.ZERO), schoolAdmin()))
                .isInstanceOf(ApiException.class);

        verify(inventoryRepo, never()).saveAndFlush(any());
    }
}
