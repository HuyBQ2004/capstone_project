package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.BiometricStatus;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.DeliveryPerson;
import com.sfms.mf04.dto.DeliveryPersonDtos.CreateDeliveryPersonRequest;
import com.sfms.mf04.dto.DeliveryPersonDtos.DeliveryPersonResponse;
import com.sfms.mf04.fixture.Mf04Fixtures;
import com.sfms.mf04.mapper.DeliveryPersonMapper;
import com.sfms.mf04.port.FaceRecognitionException;
import com.sfms.mf04.port.FaceRecognitionPort;
import com.sfms.mf04.repository.DeliveryPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF04-TC-001/007/008/009 — BR-050/051, ADR-M04-3/M04-5, M04-D03. Oracle:
 * TDS-MF04_Backend.md §2.5/§10, TestSpec-MF04 §3 TDS-05 FX-006/FX-015.
 */
class DeliveryPersonServiceTest {

    private static final AuthUser SUPPLIER_A = new AuthUser(UUID.randomUUID(), "U-0006", "sonlt@greenfarm.vn",
            RoleKey.SUPPLIER, null, Mf04Fixtures.PARTNER_A_ID);

    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};

    private record Harness(DeliveryPersonServiceImpl service, DeliveryPersonRepository repo,
                           FaceRecognitionPort frs) {}

    private Harness harness() {
        DeliveryPersonRepository repo = mock(DeliveryPersonRepository.class);
        FaceRecognitionPort frs = mock(FaceRecognitionPort.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        SchoolPartnerLinkResolver schoolPartnerLinkResolver = mock(SchoolPartnerLinkResolver.class);
        when(referenceResolver.partnerCode(any())).thenReturn("SUP-001");
        when(codeGenerator.next(eq("delivery_persons"), eq("DP"), eq(3))).thenReturn("DP-09");

        DeliveryPersonServiceImpl service = new DeliveryPersonServiceImpl(repo, new StubMapper(),
                new NationalIdCipher("c2Ztcy1tZjA0LWRldi1vbmx5LTMyLWJ5dGUta2V5ISE="), frs,
                new FileValidationSupport(), codeGenerator, referenceResolver, schoolPartnerLinkResolver, Mf04Fixtures.FIXED_CLOCK,
                5 * 1024 * 1024L);
        return new Harness(service, repo, frs);
    }

    @Nested
    @DisplayName("create() — UC-019, BR-050")
    class Create {

        @Test
        @DisplayName("CCCD đã đăng ký cho người khác ⇒ 409 SUP-050/MSG-117 (BR-050, UC-019 AF1)")
        void duplicateNationalIdRejected() {
            Harness h = harness();
            when(h.repo.existsByNationalIdHash(any())).thenReturn(true);
            var req = new CreateDeliveryPersonRequest(null, "Lê Thị Hòa", "001099212345",
                    "0905112233", "29E-246.80");

            assertThatThrownBy(() -> h.service.create(req, SUPPLIER_A))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-050");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        @DisplayName("D5 — client gửi id trùng ⇒ 409 SUP-139, KHÁC SUP-050")
        void duplicateClientIdRejected() {
            Harness h = harness();
            when(h.repo.existsByNationalIdHash(any())).thenReturn(false);
            when(h.repo.existsByCode("DP-03")).thenReturn(true);
            var req = new CreateDeliveryPersonRequest("DP-03", "Lê Thị Hòa", "001099212345",
                    "0905112233", "29E-246.80");

            assertThatThrownBy(() -> h.service.create(req, SUPPLIER_A))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-139"));
        }

        @Test
        @DisplayName("Hợp lệ ⇒ tạo NOT_REGISTERED, gắn đúng partnerId từ JWT (không tin body)")
        void createSucceedsWithPartnerIdFromJwt() {
            Harness h = harness();
            when(h.repo.existsByNationalIdHash(any())).thenReturn(false);
            var req = new CreateDeliveryPersonRequest(null, "Lê Thị Hòa", "001099212345",
                    "0905112233", "29E-246.80");

            DeliveryPersonResponse response = h.service.create(req, SUPPLIER_A);

            assertThat(response.id()).isEqualTo("DP-09");
            assertThat(response.biometricStatus()).isEqualTo("NOT_REGISTERED");
            assertThat(response.nationalId()).isEqualTo("0010992xxxxx");     // PDPD — 7 số đầu + 5 x (UC-110)
        }
    }

    @Nested
    @DisplayName("registerFace() — UC-068, BR-051/060, M04-D03")
    class RegisterFace {

        @Test
        @DisplayName("Ngoài phạm vi đối tác (partner khác) ⇒ 404 SUP-404, KHÔNG gọi FRS")
        void crossPartnerIsNotFound() {
            Harness h = harness();
            when(h.repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID)).thenReturn(Optional.empty());
            var image = new MockMultipartFile("image", "face.jpg", "image/jpeg", JPEG_MAGIC);

            assertThatThrownBy(() -> h.service.registerFace("DP-01", image, SUPPLIER_A))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-404"));
            verify(h.frs, never()).enroll(any());
        }

        @Test
        @DisplayName("FRS thành công ⇒ REGISTERED + faceId + biometricRegisteredAt (MSG-038)")
        void successfulEnrollTransitionsToRegistered() {
            Harness h = harness();
            DeliveryPerson dp = Mf04Fixtures.aDeliveryPerson(d -> {});
            when(h.repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID)).thenReturn(Optional.of(dp));
            when(h.frs.enroll(any())).thenReturn("mock-uuid-123");
            var image = new MockMultipartFile("image", "face.jpg", "image/jpeg", JPEG_MAGIC);

            var response = h.service.registerFace("DP-01", image, SUPPLIER_A);

            assertThat(response.biometricStatus()).isEqualTo("REGISTERED");
            assertThat(dp.getFaceId()).isEqualTo("mock-uuid-123");
            assertThat(dp.getBiometricRegisteredAt()).isEqualTo(Mf04Fixtures.FIXED_CLOCK.instant());
        }

        @Test
        @DisplayName("FRS lỗi (UC-068 AF1) ⇒ 502 SUP-014, KHÔNG đổi biometricStatus/faceId")
        void frsFailureDoesNotChangeState() {
            Harness h = harness();
            DeliveryPerson dp = Mf04Fixtures.aDeliveryPerson(d -> {});
            when(h.repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID)).thenReturn(Optional.of(dp));
            when(h.frs.enroll(any())).thenThrow(new FaceRecognitionException("ảnh không đạt chất lượng"));
            var image = new MockMultipartFile("image", "face.jpg", "image/jpeg", JPEG_MAGIC);

            assertThatThrownBy(() -> h.service.registerFace("DP-01", image, SUPPLIER_A))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-014");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    });
            assertThat(dp.getBiometricStatus()).isEqualTo(BiometricStatus.NOT_REGISTERED);
            assertThat(dp.getFaceId()).isNull();
        }
    }

    @Nested
    @DisplayName("deleteFace() — UC-028, BR-051 (không hoàn tác)")
    class DeleteFace {

        @Test
        @DisplayName("MF04-TC-INT-011b — FRS xác nhận xóa ⇒ NOT_REGISTERED, "
                + "faceId/biometricRegisteredAt = null (ck_dp_face vẫn thỏa)")
        void successfulDeleteResetsState() {
            Harness h = harness();
            DeliveryPerson dp = Mf04Fixtures.aDeliveryPerson(d -> {
                d.setBiometricStatus(BiometricStatus.REGISTERED);
                d.setFaceId("mock-uuid-old");
                d.setBiometricRegisteredAt(Mf04Fixtures.FIXED_CLOCK.instant());
            });
            when(h.repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID)).thenReturn(Optional.of(dp));

            var response = h.service.deleteFace("DP-01", SUPPLIER_A);

            assertThat(response.biometricStatus()).isEqualTo("NOT_REGISTERED");
            assertThat(dp.getFaceId()).isNull();
            assertThat(dp.getBiometricRegisteredAt()).isNull();
            verify(h.frs).delete("mock-uuid-old");
        }

        @Test
        @DisplayName("FRS lỗi (UC-028 AF2) ⇒ 502 SUP-014, GIỮ NGUYÊN REGISTERED + faceId cũ")
        void frsFailureKeepsRegisteredState() {
            Harness h = harness();
            DeliveryPerson dp = Mf04Fixtures.aDeliveryPerson(d -> {
                d.setBiometricStatus(BiometricStatus.REGISTERED);
                d.setFaceId("mock-uuid-old");
                d.setBiometricRegisteredAt(Mf04Fixtures.FIXED_CLOCK.instant());
            });
            when(h.repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID)).thenReturn(Optional.of(dp));
            org.mockito.Mockito.doThrow(new FaceRecognitionException("FRS timeout"))
                    .when(h.frs).delete("mock-uuid-old");

            assertThatThrownBy(() -> h.service.deleteFace("DP-01", SUPPLIER_A))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-014"));
            assertThat(dp.getBiometricStatus()).isEqualTo(BiometricStatus.REGISTERED);
            assertThat(dp.getFaceId()).isEqualTo("mock-uuid-old");
        }

        @Test
        @DisplayName("MF04-TC-INT-011b — gọi DELETE lần 2 khi đã NOT_REGISTERED ⇒ KHÔNG 500: "
                + "idempotent, trả 200 với trạng thái giữ nguyên, KHÔNG gọi lại FRS. "
                + "⚠️ LỆCH tài liệu (kỳ vọng 409/404) — code thật chọn idempotent, ck_dp_face vẫn thỏa")
        void repeatDeleteIsIdempotentAndNeverFails() {
            Harness h = harness();
            DeliveryPerson dp = Mf04Fixtures.aDeliveryPerson(d -> {
                d.setBiometricStatus(BiometricStatus.NOT_REGISTERED);
                d.setFaceId(null);
                d.setBiometricRegisteredAt(null);
            });
            when(h.repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID)).thenReturn(Optional.of(dp));

            var response = h.service.deleteFace("DP-01", SUPPLIER_A);

            assertThat(response.biometricStatus()).isEqualTo("NOT_REGISTERED");
            assertThat(dp.getFaceId()).isNull();
            assertThat(dp.getBiometricRegisteredAt()).isNull();
            verify(h.frs, never()).delete(any());       // không còn faceId ⇒ không gọi FRS lần nữa
        }

        @Test
        @DisplayName("MF04-TC-INT-011b — xóa dữ liệu khuôn mặt của nhân viên thuộc đối tác KHÁC "
                + "⇒ 404 SUP-404, KHÔNG chạm FRS (ADR-M04-3)")
        void deleteFaceOutOfPartnerScopeIsNotFound() {
            Harness h = harness();
            when(h.repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> h.service.deleteFace("DP-01", SUPPLIER_A))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-404"));
            verify(h.frs, never()).delete(any());
        }
    }

    /** Stub thay MapStruct-generated mapper — test Unit không nạp Spring context. */
    private static final class StubMapper implements DeliveryPersonMapper {
        @Override
        public DeliveryPersonResponse toResponse(DeliveryPerson dp, String partnerCode, String maskedNationalId) {
            return new DeliveryPersonResponse(dp.getCode(), partnerCode, dp.getFullName(), maskedNationalId,
                    dp.getPhone(), dp.getVehiclePlate(), dp.getBiometricStatus().name(),
                    dp.getBiometricRegisteredAt());
        }
    }
}
