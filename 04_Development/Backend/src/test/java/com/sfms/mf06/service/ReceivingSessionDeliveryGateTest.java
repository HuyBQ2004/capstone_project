package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.Delivery;
import com.sfms.mf06.domain.DeliveryStatus;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
 * Cổng CHỈ MỞ PHIÊN NHẬN HÀNG KHI CHUYẾN GIAO ĐÃ "ĐÃ GIAO" (gate bổ sung 13/08/2026 ở
 * {@code ReceivingSessionService.create()}).
 *
 * <p><b>Lỗ hổng cổng này bịt.</b> Trước đây {@code create()} không hề đọc
 * {@code delivery.getStatus()} — chỉ FE lọc dropdown theo {@code status=DELIVERED}
 * ({@code receiving/new/page.tsx}), nên gọi thẳng API với {@code deliveryId} của một chuyến
 * còn {@code SCHEDULED}/{@code DISPATCHED}/{@code IN_TRANSIT}/{@code DELAYED}, hoặc đã
 * {@code COMPLETED}/{@code CANCELLED}, vẫn mở được phiên và trừ tiếp {@code remaining_quantity}
 * của lô — không có hàng rào thật ở server.
 *
 * <p>Các test khác trong module (vd. {@link ReceivingBatchOnceTest}) đã né gate này bằng cách
 * set sẵn {@code delivery.status = DELIVERED} trong fixture — nên bản thân gate CHƯA từng được
 * test trực tiếp. File này lấp đúng lỗ đó: mọi trạng thái KHÁC DELIVERED phải bị chặn 409, và
 * DELIVERED phải cho qua.
 */
class ReceivingSessionDeliveryGateTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final UUID INGREDIENT_ID = UUID.randomUUID();

    private DeliveryRepository deliveryRepo;
    private InventoryBatchRepository inventoryRepo;
    private ReceivingSessionRepository sessionRepo;
    private Delivery delivery;
    private ReceivingSessionService service;

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "bgh@truong.test",
                RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static ReceivingSessionCreateRequest request() {
        return new ReceivingSessionCreateRequest("DLV-1", null,
                new BigDecimal("21.0"), new BigDecimal("105.0"), 1, null, "test-device",
                List.of(new ReceivingLineRequest("SB-GAO-TE", "ING-GAO-TE", "DRY", false, null, null,
                        new BigDecimal("10"))));
    }

    @BeforeEach
    void setUp() {
        SupplierBatchRepository batchRepo = mock(SupplierBatchRepository.class);
        sessionRepo = mock(ReceivingSessionRepository.class);
        inventoryRepo = mock(InventoryBatchRepository.class);
        deliveryRepo = mock(DeliveryRepository.class);
        PurchaseRequestRepository purchaseRequestRepo = mock(PurchaseRequestRepository.class);
        InventoryTransactionRepository txRepo = mock(InventoryTransactionRepository.class);
        CatalogIngredientRepository catalogRepo = mock(CatalogIngredientRepository.class);
        ReferenceResolver refs = mock(ReferenceResolver.class);
        GeofenceValidator geofence = mock(GeofenceValidator.class);
        CodeGenerator codes = mock(CodeGenerator.class);

        SupplierBatch batch = new SupplierBatch();
        batch.setCode("SB-GAO-TE");
        batch.setPartnerId(PARTNER_ID);
        batch.setQuantity(new BigDecimal("50"));
        batch.setRemainingQuantity(new BigDecimal("50"));
        batch.setUnit("kg");
        batch.setExpirationDate(LocalDate.now().plusDays(30));
        batch.setStatus(SupplierBatchStatus.ACTIVE);

        delivery = new Delivery();
        delivery.setCode("DLV-1");
        delivery.setStatus(DeliveryStatus.SCHEDULED);

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
                Clock.fixed(Instant.parse("2026-08-13T00:00:00Z"), ZoneOffset.UTC),
                mock(org.springframework.jdbc.core.JdbcTemplate.class),
                mock(com.sfms.common.storage.StorageService.class), mock(FileValidationSupport.class),
                mock(com.sfms.mf10.port.NotificationPort.class));
    }

    @ParameterizedTest(name = "delivery.status={0} ⇒ 409, không mở được phiên nhận hàng")
    @EnumSource(value = DeliveryStatus.class, names = {"DELIVERED"}, mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Chuyến giao CHƯA 'Đã giao' (mọi trạng thái khác DELIVERED) ⇒ chặn mở phiên nhận, 0 dòng ghi")
    void deliveryNotDeliveredBlocksSessionCreation(DeliveryStatus status) {
        delivery.setStatus(status);

        assertThatThrownBy(() -> service.create(request(), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(409));

        verify(sessionRepo, never()).saveAndFlush(any());
        verify(inventoryRepo, never()).saveAndFlush(any());
    }

    @DisplayName("Chuyến giao ĐÃ 'Đã giao' (DELIVERED) ⇒ cổng cho qua, phiên được tạo")
    @org.junit.jupiter.api.Test
    void deliveryDeliveredAllowsSessionCreation() {
        delivery.setStatus(DeliveryStatus.DELIVERED);

        service.create(request(), schoolAdmin());

        verify(sessionRepo).saveAndFlush(any());
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    }
}
