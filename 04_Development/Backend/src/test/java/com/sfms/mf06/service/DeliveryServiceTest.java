package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.Delivery;
import com.sfms.mf06.domain.DeliveryStatus;
import com.sfms.mf06.domain.PurchaseRequest;
import com.sfms.mf06.domain.PurchaseRequestStatus;
import com.sfms.mf06.dto.DeliveryDtos.CreateRequest;
import com.sfms.mf06.dto.DeliveryDtos.DeliveryResponse;
import com.sfms.mf06.repository.DeliveryRepository;
import com.sfms.mf06.repository.PurchaseRequestRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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
 * {@code DeliveryService.create()}/{@code updateStatus()} — các nhánh CHƯA có test riêng:
 * cửa sổ tối thiểu 24h trước giờ giao dự kiến (BR-071, chốt server-side 12/08/2026), cảnh báo
 * lệch giờ MSG-065 ở {@code updateStatus()} (ngưỡng cấu hình {@code delivery.deviation-alert-minutes},
 * CF-M06-6 — KHÔNG phải hằng số BR), mã đơn trùng (D5), và gate "đơn chưa duyệt" khi đẩy
 * DELIVERED (BR-070 mở rộng 11/08/2026). {@link DeliveryDriverRequiredTest} chỉ phủ nhánh
 * driverId bắt buộc — các nhánh dưới đây trước giờ không có test nào chạm tới.
 */
class DeliveryServiceTest {

    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID DRIVER_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-08-13T00:00:00Z");

    private DeliveryRepository deliveryRepo;
    private PurchaseRequestRepository prRepo;
    private ReferenceResolver refs;
    private DeliveryService service;

    private static AuthUser supplier() {
        return new AuthUser(UUID.randomUUID(), "U-0009", "ncc@partner.test",
                RoleKey.SUPPLIER, null, PARTNER_ID);
    }

    private static CreateRequest request(Instant scheduledAt) {
        return new CreateRequest("DLV-TEST-1", "PO-1", scheduledAt, "DP-01", "29C-123.45", List.of());
    }

    @BeforeEach
    void setUp() {
        deliveryRepo = mock(DeliveryRepository.class);
        prRepo = mock(PurchaseRequestRepository.class);
        refs = mock(ReferenceResolver.class);
        CodeGenerator codes = mock(CodeGenerator.class);

        PurchaseRequest pr = new PurchaseRequest();
        pr.setSchoolId(SCHOOL_ID);
        pr.setStatus(PurchaseRequestStatus.APPROVED);
        when(prRepo.findByCode("PO-1")).thenReturn(Optional.of(pr));
        when(deliveryRepo.existsBySchoolIdAndCode(any(), anyString())).thenReturn(false);
        when(codes.next(anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn("DLV-TEST-1");
        when(refs.deliveryPersonIdByCode("DP-01", PARTNER_ID)).thenReturn(DRIVER_ID);

        service = new DeliveryService(deliveryRepo, prRepo, mock(SupplierBatchRepository.class),
                mock(SupplierProductRepository.class), refs, codes, mock(JdbcTemplate.class),
                Clock.fixed(NOW, ZoneOffset.UTC), 30);
    }

    @Test
    @DisplayName("BR-071 — scheduledAt CÁCH hiện tại DƯỚI 24h ⇒ 422, không lưu chuyến giao")
    void scheduledAtLessThan24hAheadIsRejected() {
        assertThatThrownBy(() -> service.create(request(NOW.plus(Duration.ofHours(23))), supplier()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(422));

        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("BR-071 — scheduledAt ĐÚNG BIÊN 24h ⇒ chấp nhận (không phải > 24h)")
    void scheduledAtExactly24hAheadIsAccepted() {
        service.create(request(NOW.plus(Duration.ofHours(24))), supplier());

        verify(deliveryRepo).saveAndFlush(any());
    }

    @Test
    @DisplayName("BR-071 — scheduledAt CÁCH hiện tại TRÊN 24h ⇒ chấp nhận, tạo chuyến giao")
    void scheduledAtMoreThan24hAheadIsAccepted() {
        service.create(request(NOW.plus(Duration.ofHours(48))), supplier());

        verify(deliveryRepo).saveAndFlush(any());
    }

    @Test
    @DisplayName("D5 — mã chuyến giao đã tồn tại trong trường ⇒ 409, không lưu chuyến giao trùng")
    void duplicateDeliveryCodeIsRejected() {
        when(deliveryRepo.existsBySchoolIdAndCode(SCHOOL_ID, "DLV-TEST-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(NOW.plus(Duration.ofHours(48))), supplier()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(409));

        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("purchaseRequestId rỗng/không gửi ⇒ 422, chuyến giao PHẢI gắn với một đơn mua hàng")
    void missingPurchaseRequestIdIsRejected() {
        CreateRequest noPr = new CreateRequest("DLV-TEST-1", null, NOW.plus(Duration.ofHours(48)),
                "DP-01", "29C-123.45", List.of());

        assertThatThrownBy(() -> service.create(noPr, supplier()))
                .isInstanceOf(ApiException.class);

        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    // ---- updateStatus(): gate BR-070 mở rộng + cảnh báo lệch giờ MSG-065 (CF-M06-6) ----

    private Delivery scheduledDelivery(UUID prId) {
        Delivery d = new Delivery();
        d.setCode("DLV-1");
        d.setStatus(DeliveryStatus.IN_TRANSIT);
        d.setScheduledAt(NOW);
        d.setPurchaseRequestId(prId);
        when(deliveryRepo.findByCode("DLV-1")).thenReturn(Optional.of(d));
        return d;
    }

    @Test
    @DisplayName("BR-070 mở rộng — đẩy DELIVERED khi đơn mua hàng liên kết CHƯA APPROVED ⇒ 409, không đổi trạng thái")
    void updateStatusToDeliveredWithUnapprovedOrderIsRejected() {
        UUID prId = UUID.randomUUID();
        Delivery d = scheduledDelivery(prId);
        PurchaseRequest pr = new PurchaseRequest();
        pr.setStatus(PurchaseRequestStatus.PENDING);
        when(prRepo.findById(prId)).thenReturn(Optional.of(pr));

        assertThatThrownBy(() -> service.updateStatus("DLV-1", "DELIVERED", supplier()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(409));

        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Đẩy trạng thái KHÔNG PHẢI DELIVERED (vd. DISPATCHED) ⇒ không kiểm tra đơn mua hàng, luôn cho qua")
    void updateStatusToNonDeliveredSkipsOrderApprovalGate() {
        UUID prId = UUID.randomUUID();
        scheduledDelivery(prId);
        PurchaseRequest pr = new PurchaseRequest();
        pr.setStatus(PurchaseRequestStatus.PENDING);
        when(prRepo.findById(prId)).thenReturn(Optional.of(pr));

        DeliveryResponse res = service.updateStatus("DLV-1", "DISPATCHED", supplier());

        assertThat(res.status()).isEqualTo("DISPATCHED");
        verify(deliveryRepo).saveAndFlush(any());
    }

    @Test
    @DisplayName("Trạng thái không hợp lệ (không thuộc enum DeliveryStatus) ⇒ 422")
    void updateStatusWithUnknownEnumValueIsRejected() {
        scheduledDelivery(null);

        assertThatThrownBy(() -> service.updateStatus("DLV-1", "NOT_A_STATUS", supplier()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(422));

        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("MSG-065 — lệch giờ THỰC HIỆN so với scheduledAt VƯỢT ngưỡng cấu hình (30′) ⇒ có cảnh báo")
    void updateStatusBeyondDeviationThresholdRaisesAlert() {
        Delivery d = scheduledDelivery(null);
        d.setScheduledAt(NOW.minus(Duration.ofMinutes(45))); // lệch 45' > ngưỡng 30'

        DeliveryResponse res = service.updateStatus("DLV-1", "DELAYED", supplier());

        assertThat(res.alert()).isNotNull();
        assertThat(res.alert().code()).isEqualTo("MSG-065");
    }

    @Test
    @DisplayName("Lệch giờ TRONG ngưỡng cấu hình (25′ < 30′) ⇒ KHÔNG phát cảnh báo")
    void updateStatusWithinDeviationThresholdRaisesNoAlert() {
        Delivery d = scheduledDelivery(null);
        d.setScheduledAt(NOW.minus(Duration.ofMinutes(25))); // lệch 25' < ngưỡng 30'

        DeliveryResponse res = service.updateStatus("DLV-1", "DELAYED", supplier());

        assertThat(res.alert()).isNull();
    }
}
