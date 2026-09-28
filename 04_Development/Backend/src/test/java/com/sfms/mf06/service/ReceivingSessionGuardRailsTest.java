package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.Delivery;
import com.sfms.mf06.domain.DeliveryStatus;
import com.sfms.mf06.domain.InventoryBatch;
import com.sfms.mf06.domain.QuarantineCause;
import com.sfms.mf06.domain.QuarantineInfo;
import com.sfms.mf06.domain.StorageZone;
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
 * Các hàng rào KHÁC ở {@code ReceivingSessionService.create()} chưa có test riêng: trùng
 * lotNumber giữa HAI NCC khác nhau (chốt 11/08/2026), lô đang cách ly chặn nhận thêm
 * (BR-149, {@link InventoryBatch#blocksFurtherReceiving()}), và dòng từ chối thiếu lý do/ảnh
 * bằng chứng (BR-078). {@link ReceivingBatchOnceTest} chỉ phủ nhánh remaining_quantity;
 * {@link ReceivingSessionDeliveryGateTest} chỉ phủ gate trạng thái chuyến giao — ba nhánh dưới
 * đây chưa từng được test chạm tới.
 */
class ReceivingSessionGuardRailsTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final UUID OTHER_PARTNER_ID = UUID.randomUUID();
    private static final UUID INGREDIENT_ID = UUID.randomUUID();

    private InventoryBatchRepository inventoryRepo;
    private SupplierBatch batch;
    private InventoryBatch existingInventoryBatch;
    private ReceivingSessionService service;

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "bgh@truong.test",
                RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static ReceivingSessionCreateRequest acceptedLineRequest() {
        return new ReceivingSessionCreateRequest("DLV-1", null,
                new BigDecimal("21.0"), new BigDecimal("105.0"), 1, null, "test-device",
                List.of(new ReceivingLineRequest("SB-GAO-TE", "ING-GAO-TE", "DRY", false, null, null,
                        new BigDecimal("10"))));
    }

    private static ReceivingSessionCreateRequest rejectedLineRequest(String reason, List<String> docIds) {
        return new ReceivingSessionCreateRequest("DLV-1", null,
                new BigDecimal("21.0"), new BigDecimal("105.0"), 1, null, "test-device",
                List.of(new ReceivingLineRequest("SB-GAO-TE", "ING-GAO-TE", "DRY", true, reason, docIds, null)));
    }

    @BeforeEach
    void setUp() {
        SupplierBatchRepository batchRepo = mock(SupplierBatchRepository.class);
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
        delivery.setStatus(DeliveryStatus.DELIVERED);

        existingInventoryBatch = new InventoryBatch();
        existingInventoryBatch.setCode("SB-GAO-TE");
        existingInventoryBatch.setSchoolId(SCHOOL_ID);
        existingInventoryBatch.setIngredientId(INGREDIENT_ID);
        existingInventoryBatch.setIngredientLabel("Gạo tẻ");
        existingInventoryBatch.setSupplierId(PARTNER_ID);
        existingInventoryBatch.setInitialQuantity(new BigDecimal("10"));
        existingInventoryBatch.setUnit("kg");
        existingInventoryBatch.setStorageZone(StorageZone.DRY);
        existingInventoryBatch.setReceivedDate(LocalDate.now());
        existingInventoryBatch.setExpiryDate(LocalDate.now().plusDays(30));

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
        when(codes.next(any(), any(), org.mockito.ArgumentMatchers.anyInt())).thenReturn("RCV-1");

        service = new ReceivingSessionService(sessionRepo, deliveryRepo, purchaseRequestRepo, batchRepo, inventoryRepo,
                txRepo, catalogRepo, refs, geofence, codes,
                Clock.fixed(Instant.parse("2026-08-13T00:00:00Z"), ZoneOffset.UTC),
                mock(JdbcTemplate.class), mock(com.sfms.common.storage.StorageService.class),
                mock(FileValidationSupport.class), mock(com.sfms.mf10.port.NotificationPort.class));
    }

    @Test
    @DisplayName("Trùng mã lô (school_id, code) nhưng KHÁC nhà cung cấp ⇒ 409, không gộp nhầm tồn kho")
    void batchCodeCollisionAcrossDifferentSuppliersIsRejected() {
        existingInventoryBatch.setSupplierId(OTHER_PARTNER_ID);
        when(inventoryRepo.findBySchoolIdAndCode(SCHOOL_ID, "SB-GAO-TE")).thenReturn(Optional.of(existingInventoryBatch));

        assertThatThrownBy(() -> service.create(acceptedLineRequest(), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(409));

        verify(inventoryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Lô cách ly do LỖI TẠI NCC (SUPPLIER_LOT_DEFECT) ⇒ chặn vĩnh viễn, không cho nhận thêm")
    void quarantinedBatchWithSupplierDefectBlocksFurtherReceiving() {
        existingInventoryBatch.setQuarantine(new QuarantineInfo(Instant.now(), UUID.randomUUID(), "hỏng",
                UUID.randomUUID(), QuarantineCause.SUPPLIER_LOT_DEFECT));
        when(inventoryRepo.findBySchoolIdAndCode(SCHOOL_ID, "SB-GAO-TE")).thenReturn(Optional.of(existingInventoryBatch));

        assertThatThrownBy(() -> service.create(acceptedLineRequest(), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(409));

        verify(inventoryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Lô cách ly do LỖI BẢO QUẢN TRƯỜNG (SCHOOL_STORAGE) nhưng CHƯA xử lý xong (disposition null) ⇒ vẫn chặn")
    void quarantinedBatchWithSchoolStorageCauseUndisposedBlocksFurtherReceiving() {
        existingInventoryBatch.setQuarantine(new QuarantineInfo(Instant.now(), UUID.randomUUID(), "bảo quản sai",
                UUID.randomUUID(), QuarantineCause.SCHOOL_STORAGE));
        when(inventoryRepo.findBySchoolIdAndCode(SCHOOL_ID, "SB-GAO-TE")).thenReturn(Optional.of(existingInventoryBatch));

        assertThatThrownBy(() -> service.create(acceptedLineRequest(), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(409));

        verify(inventoryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("MF06-TC-INT-005 (nhánh BR-078) — Dòng TỪ CHỐI thiếu lý do ⇒ 400 BR-078, không tạo phiên")
    void rejectedLineWithoutReasonIsRejected() {
        assertThatThrownBy(() -> service.create(rejectedLineRequest(null, List.of("RCVDOC-1")), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(400));

        verify(inventoryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("MF06-TC-INT-005 (nhánh BR-078) — Dòng TỪ CHỐI thiếu ảnh bằng chứng ⇒ 400 BR-078, không tạo phiên")
    void rejectedLineWithoutEvidenceDocumentsIsRejected() {
        assertThatThrownBy(() -> service.create(rejectedLineRequest("Hàng hỏng", List.of()), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(400));

        verify(inventoryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("rejectDocumentIds là TÊN FILE/đường dẫn thay vì ID tài liệu đã upload ⇒ 422 D10-BIS.3")
    void rejectDocumentIdsAsFilenameIsRejected() {
        assertThatThrownBy(() -> service.create(rejectedLineRequest("Hàng hỏng", List.of("evidence.jpg")), schoolAdmin()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(422));

        verify(inventoryRepo, never()).saveAndFlush(any());
    }
}
