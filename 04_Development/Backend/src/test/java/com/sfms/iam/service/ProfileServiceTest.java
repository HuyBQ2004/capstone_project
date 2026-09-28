package com.sfms.iam.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.mapper.UserMapper;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-023 · MF01-TC-024 — UC-097 ràng buộc ảnh hồ sơ.
 *
 * <p>
 * ⚠️ <b>CF-4 treo</b>: UC-097 narrative nói <b>5MB</b>, MSG-008 nói
 * <b>10MB</b>.
 * TC assert <b>10MB</b> theo <b>D7</b> (nội dung message lấy nguyên văn
 * {@code error_message.md}) — chặn ở 5MB trong khi message nói 10MB làm người
 * dùng không hiểu.
 *
 * <p>
 * ⚠️ MSG-009 cho phép <b>PDF, JPG, PNG</b> trong khi UC-097 bước 3 chỉ nói
 * JPG/JPEG/PNG.
 * TC theo MSG-009 (D7) và ghi nhận lệch để Huy đối chiếu — cùng nhóm CF-3/CF-4.
 */
class ProfileServiceTest {

    private static final long TEN_MB = 10L * 1024 * 1024;

    /** Magic bytes hợp lệ cho từng loại — kiểm nội dung, không chỉ phần mở rộng. */
    private static final byte[] JPEG_HEADER = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0 };
    private static final byte[] PNG_HEADER = { (byte) 0x89, 'P', 'N', 'G' };
    private static final byte[] PDF_HEADER = { '%', 'P', 'D', 'F' };
    private static final byte[] MZ_HEADER = { 'M', 'Z', 0x00, 0x00 }; // .exe

    private record Harness(ProfileService service,
            UserAccountRepository userRepo,
            UserAccount user,
            AuthUser actor) {
    }

    private Harness harness() {
        UserAccount user = UserFixture.base();
        UserAccountRepository userRepo = mock(UserAccountRepository.class);
        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));

        UserMapper mapper = mock(UserMapper.class);
        StorageService storageService = mock(StorageService.class);
        when(storageService.store(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn("/media/avatars/" + user.getCode() + ".jpg");
        ProfileService service = new ProfileService(userRepo, mapper, storageService, TEN_MB,
                "application/pdf,image/jpeg,image/png");

        AuthUser actor = new AuthUser(user.getId(), user.getCode(), user.getEmail(),
                RoleKey.SCHOOL_ADMIN, user.getSchoolId(), null);

        return new Harness(service, userRepo, user, actor);
    }

    private MockMultipartFile file(String name, String contentType, byte[] header, long totalSize) {
        byte[] content = new byte[(int) Math.max(totalSize, header.length)];
        System.arraycopy(header, 0, content, 0, header.length);
        return new MockMultipartFile("file", name, contentType, content);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-023 — Ảnh vượt dung lượng ⇒ MSG-008 nguyên văn
    // Oracle: MSG-008 · UC-097 Exceptions · TDS §10 IAM-010 (422) · ⚠️ CF-4 + §2
    // L14
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-023 — biên 10MB (MSG-008) ⚠️ CF-4 treo")
    class Tc023 {

        @Test
        @DisplayName("10MB − 1B ⇒ chấp nhận")
        void justUnderLimitIsAccepted() {
            Harness h = harness();
            assertThatCode(() -> h.service.updatePhoto(h.actor,
                    file("a.jpg", "image/jpeg", JPEG_HEADER, TEN_MB - 1)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Đúng 10MB ⇒ chấp nhận (\"exceeds the maximum limit\" ⇒ ≤ là hợp lệ)")
        void exactlyAtLimitIsAccepted() {
            Harness h = harness();
            assertThatCode(() -> h.service.updatePhoto(h.actor,
                    file("a.jpg", "image/jpeg", JPEG_HEADER, TEN_MB)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("10MB + 1B ⇒ IAM-010 (422) + MSG-008 NGUYÊN VĂN (không dùng chuỗi gộp của TDS §10)")
        void overLimitIsRejectedWithVerbatimMsg008() {
            Harness h = harness();

            assertThatThrownBy(() -> h.service.updatePhoto(h.actor,
                    file("a.jpg", "image/jpeg", JPEG_HEADER, TEN_MB + 1)))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-010");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                        // D7 — nguyên văn MSG-008, KHÔNG phải paraphrase gộp
                        // "size exceeds 10MB / Unsupported file type" của TDS §10
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_008);
                    });

            verify(h.userRepo, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
            assertThat(h.user.getAvatarUrl()).isNull();
        }

        /**
         * CF-4: UC-097 nói 5MB nhưng MSG-008 nói 10MB ⇒ file 6MB PHẢI được chấp nhận.
         */
        @Test
        @DisplayName("File 6MB ⇒ chấp nhận (ngưỡng theo MSG-008 = 10MB, không theo UC-097 = 5MB)")
        void sixMegabyteFileIsAcceptedBecauseThresholdIsTen() {
            Harness h = harness();
            assertThatCode(() -> h.service.updatePhoto(h.actor,
                    file("a.jpg", "image/jpeg", JPEG_HEADER, 6L * 1024 * 1024)))
                    .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-024 — Ảnh sai định dạng ⇒ MSG-009 nguyên văn
    // Severity: LOW · CWE-434 Unrestricted Upload of File with Dangerous Type
    // Oracle: MSG-009 · UC-097 Exceptions · TDS §10 IAM-010 (422)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-024 — định dạng cho phép (MSG-009)")
    class Tc024 {

        static Stream<Arguments> fileTypes() {
            return Stream.of(
                    Arguments.of("a.jpg", "image/jpeg", JPEG_HEADER, true),
                    Arguments.of("a.jpeg", "image/jpeg", JPEG_HEADER, true),
                    Arguments.of("a.png", "image/png", PNG_HEADER, true),
                    // ⚠️ Theo MSG-009 (D7): PDF được phép, dù UC-097 bước 3 chỉ nói JPG/JPEG/PNG
                    Arguments.of("a.pdf", "application/pdf", PDF_HEADER, true),
                    Arguments.of("a.gif", "image/gif", new byte[] { 'G', 'I', 'F', '8' }, false),
                    Arguments.of("a.svg", "image/svg+xml", new byte[] { '<', 's', 'v', 'g' }, false));
        }

        @ParameterizedTest(name = "{0} ({1}) ⇒ {3}")
        @MethodSource("fileTypes")
        @DisplayName("PDF/JPG/PNG được phép; GIF/SVG bị chặn với MSG-009 nguyên văn")
        void allowedAndBlockedTypes(String name, String contentType, byte[] header, boolean allowed) {
            Harness h = harness();
            MockMultipartFile upload = file(name, contentType, header, 1024);

            if (allowed) {
                assertThatCode(() -> h.service.updatePhoto(h.actor, upload))
                        .doesNotThrowAnyException();
            } else {
                assertThatThrownBy(() -> h.service.updatePhoto(h.actor, upload))
                        .isInstanceOf(ApiException.class)
                        .satisfies(ex -> {
                            ApiException api = (ApiException) ex;
                            assertThat(api.getCode()).isEqualTo("IAM-010");
                            assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                            assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_009);
                        });
            }
        }

        /**
         * Điểm quan trọng nhất của TC: quyết định theo <b>magic bytes</b>, không theo
         * phần
         * mở rộng hay {@code Content-Type} do client khai.
         */
        @Test
        @DisplayName(".exe đổi tên thành .jpg (magic bytes MZ) ⇒ BỊ CHẶN")
        void renamedExecutableIsBlockedByMagicBytes() {
            Harness h = harness();
            MockMultipartFile disguised = file("payload.jpg", "image/jpeg", MZ_HEADER, 2048);

            assertThatThrownBy(() -> h.service.updatePhoto(h.actor, disguised))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getMessage())
                            .isEqualTo(ExpectedMessages.MSG_009));
        }

        @Test
        @DisplayName("SVG khai là image/png (chống XSS qua SVG) ⇒ BỊ CHẶN")
        void svgDisguisedAsPngIsBlocked() {
            Harness h = harness();
            MockMultipartFile svg = new MockMultipartFile("file", "x.png", "image/png",
                    "<svg onload=\"alert(1)\"></svg>".getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> h.service.updatePhoto(h.actor, svg))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-010"));
        }

        @Test
        @DisplayName("File rỗng ⇒ IAM-010 + MSG-009, không 500")
        void emptyFileIsRejected() {
            Harness h = harness();
            MockMultipartFile empty = new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[0]);

            assertThatThrownBy(() -> h.service.updatePhoto(h.actor, empty))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getMessage())
                            .isEqualTo(ExpectedMessages.MSG_009));
        }
    }
}
