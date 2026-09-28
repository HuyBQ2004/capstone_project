package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.PurchaseRequest;
import com.sfms.mf06.dto.DeliveryDtos.CreateRequest;
import com.sfms.mf06.repository.DeliveryRepository;
import com.sfms.mf06.repository.PurchaseRequestRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;
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
 * NGƯỜI GIAO LÀ BẮT BUỘC khi tạo chuyến giao (sửa 08/08/2026).
 *
 * <p><b>Vì sao.</b> {@code driverId} vốn là tuỳ chọn, nên tạo được chuyến giao thực phẩm cho
 * trường mà KHÔNG có người giao đích danh — đo thật: NCC chưa khai nhân viên giao nào vẫn giao
 * hàng bình thường. Khi có sự cố ATTP thì "ai mang lô hàng này tới trường" là mắt xích truy vết
 * không thay thế được, và phiên nhận hàng còn có bước xác minh khuôn mặt tài xế
 * ({@code driverVerificationId}) — không có người giao thì bước đó vô nghĩa.
 */
class DeliveryDriverRequiredTest {

    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID DRIVER_ID = UUID.randomUUID();

    private DeliveryRepository deliveryRepo;
    private ReferenceResolver refs;
    private DeliveryService service;

    private static AuthUser supplier() {
        return new AuthUser(UUID.randomUUID(), "U-0009", "ncc@partner.test",
                RoleKey.SUPPLIER, null, PARTNER_ID);
    }

    private static CreateRequest request(String driverId) {
        return new CreateRequest("DLV-TEST-1", "PO-1", Instant.parse("2026-08-12T05:30:00Z"),
                driverId, "29C-123.45", List.of());
    }

    @BeforeEach
    void setUp() {
        deliveryRepo = mock(DeliveryRepository.class);
        PurchaseRequestRepository prRepo = mock(PurchaseRequestRepository.class);
        refs = mock(ReferenceResolver.class);
        CodeGenerator codes = mock(CodeGenerator.class);

        PurchaseRequest pr = new PurchaseRequest();
        pr.setSchoolId(SCHOOL_ID);
        when(prRepo.findByCode("PO-1")).thenReturn(Optional.of(pr));
        when(deliveryRepo.existsBySchoolIdAndCode(any(), anyString())).thenReturn(false);
        when(codes.next(anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn("DLV-TEST-1");

        service = new DeliveryService(deliveryRepo, prRepo, mock(SupplierBatchRepository.class),
                mock(SupplierProductRepository.class), refs, codes, mock(JdbcTemplate.class),
                Clock.fixed(Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC), 30);
    }

    @Test
    @DisplayName("Thiếu driverId ⇒ 422, KHÔNG lưu chuyến giao nào")
    void missingDriverIsRejected() {
        assertThatThrownBy(() -> service.create(request(null), supplier()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getDetails()).isNotEmpty());

        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("driverId rỗng/khoảng trắng cũng bị chặn — không lách bằng chuỗi trống")
    void blankDriverIsRejected() {
        assertThatThrownBy(() -> service.create(request("   "), supplier()))
                .isInstanceOf(ApiException.class);

        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("driverId không thuộc NCC này ⇒ 422 'not found'")
    void driverOfAnotherPartnerIsRejected() {
        when(refs.deliveryPersonIdByCode("DP-KHAC", PARTNER_ID)).thenReturn(null);

        assertThatThrownBy(() -> service.create(request("DP-KHAC"), supplier()))
                .isInstanceOf(ApiException.class);

        verify(deliveryRepo, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("driverId hợp lệ ⇒ tạo chuyến giao và GẮN đúng người giao")
    void validDriverIsAccepted() {
        when(refs.deliveryPersonIdByCode("DP-01", PARTNER_ID)).thenReturn(DRIVER_ID);

        service.create(request("DP-01"), supplier());

        verify(deliveryRepo).saveAndFlush(org.mockito.ArgumentMatchers.argThat(
                d -> DRIVER_ID.equals(d.getDriverId())));
    }
}
