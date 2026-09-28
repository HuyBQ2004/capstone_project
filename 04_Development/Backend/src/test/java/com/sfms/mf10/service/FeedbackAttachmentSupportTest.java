package com.sfms.mf10.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.error.ApiException;
import com.sfms.common.storage.StorageService;
import com.sfms.mf10.domain.FeedbackAttachment;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.repository.FeedbackAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * MF10-TC-SEC-009 — ảnh đính kèm phản hồi đi qua {@code common/storage/StorageService} (D10),
 * bị giới hạn kích thước và whitelist theo MAGIC BYTES, không theo phần mở rộng file.
 */
class FeedbackAttachmentSupportTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID FEEDBACK_ID = UUID.randomUUID();
    private static final UUID UPLOADER_ID = UUID.randomUUID();

    private StorageService storageService;
    private FeedbackAttachmentRepository attachmentRepository;
    private FeedbackAttachmentSupport support;

    @BeforeEach
    void setUp() {
        storageService = mock(StorageService.class);
        attachmentRepository = mock(FeedbackAttachmentRepository.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        when(codeGenerator.next(anyString(), anyString(), eq(3))).thenReturn("FBATT-2026-001");
        support = new FeedbackAttachmentSupport(storageService, attachmentRepository, codeGenerator);
    }

    private static byte[] withHeader(byte[] magic, int totalSize) {
        byte[] content = new byte[Math.max(totalSize, magic.length)];
        System.arraycopy(magic, 0, content, 0, magic.length);
        return content;
    }

    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    @Test
    @DisplayName("MF10-TC-SEC-009 — ảnh JPEG hợp lệ được lưu QUA StorageService với khóa riêng tư, "
            + "response không trả storageKey/đường dẫn nội bộ")
    void validJpegIsStoredThroughStorageServiceWithoutLeakingTheStorageKey() {
        var file = new MockMultipartFile("file", "phan-anh-suat-an.jpg", "image/jpeg",
                withHeader(JPEG_MAGIC, 280 * 1024));

        FeedbackAttachment saved = support.store(SCHOOL_ID, FEEDBACK_ID, file, UPLOADER_ID);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(storageService).store(key.capture(), eq(file));
        // ⚠️ Lệch tài liệu: tiền tố thật là "mf10/feedbacks/…", không phải "feedbacks/…".
        assertThat(key.getValue()).startsWith("mf10/feedbacks/" + FEEDBACK_ID + "/");
        assertThat(saved.getContentType()).isEqualTo("image/jpeg");
        assertThat(saved.getSizeBytes()).isEqualTo(280L * 1024);
        verify(attachmentRepository).saveAndFlush(saved);

        // DTO trả về FE (§4.3) KHÔNG có storageKey/đường dẫn.
        assertThat(com.sfms.mf10.dto.FeedbackDtos.FeedbackAttachmentResponse.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("storageKey", "url", "path");
    }

    @Test
    @DisplayName("MF10-TC-SEC-009 — file vượt hạn mức 5 MB bị chặn (FBA-SIZE, HTTP 413) trước khi chạm storage")
    void oversizeFileIsRejectedBeforeReachingStorage() {
        var oversize = new MockMultipartFile("file", "oversize.jpg", "image/jpeg",
                withHeader(JPEG_MAGIC, 6 * 1024 * 1024));

        assertThatThrownBy(() -> support.store(SCHOOL_ID, FEEDBACK_ID, oversize, UPLOADER_ID))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(FbaError.ATTACHMENT_TOO_LARGE);
                    assertThat(ex.getStatus().value()).isEqualTo(413);
                });

        verifyNoInteractions(storageService);
        verify(attachmentRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("MF10-TC-SEC-009 — file .jpg nhưng magic bytes là 'MZ' (file thực thi) bị từ chối: "
            + "kiểm nội dung thật, KHÔNG tin phần mở rộng hay Content-Type client khai")
    void executableDisguisedAsJpegIsRejectedByMagicBytes() {
        // Header that cua file thuc thi Windows: M, Z roi phan con lai cua DOS stub.
        var fake = new MockMultipartFile("file", "fake.jpg", "image/jpeg",
                new byte[] {'M', 'Z', (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00});

        assertThatThrownBy(() -> support.store(SCHOOL_ID, FEEDBACK_ID, fake, UPLOADER_ID))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(FbaError.UNSUPPORTED_MEDIA);
                    assertThat(ex.getStatus().value()).isEqualTo(415);
                });

        verifyNoInteractions(storageService);
    }

    @Test
    @DisplayName("MF10-TC-SEC-009 — SVG nằm NGOÀI whitelist (định dạng vector nhúng được script) nên bị từ chối, "
            + "trong khi PNG hợp lệ vẫn qua")
    void svgIsOutsideTheWhitelistWhilePngPasses() {
        var svg = new MockMultipartFile("file", "logo.svg", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                        .getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> support.store(SCHOOL_ID, FEEDBACK_ID, svg, UPLOADER_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.UNSUPPORTED_MEDIA));

        var png = new MockMultipartFile("file", "anh.png", "image/png", withHeader(PNG_MAGIC, 1024));
        assertThat(support.store(SCHOOL_ID, FEEDBACK_ID, png, UPLOADER_ID).getContentType())
                .isEqualTo("image/png");
    }

    @Test
    @DisplayName("MF10-TC-SEC-009 — file rỗng bị chặn FBA-REQ (không tạo bản ghi đính kèm rỗng)")
    void emptyFileIsRejected() {
        var empty = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> support.store(SCHOOL_ID, FEEDBACK_ID, empty, UPLOADER_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("MF10-TC-SEC-009 — không lớp nào trong com.sfms.mf10 tự ghi file: 0 lần gọi "
            + "MultipartFile.transferTo / AWS SDK, tất cả đi qua StorageService")
    void noClassInTheModuleWritesFilesDirectly() throws Exception {
        try (var paths = Files.walk(Path.of("src/main/java/com/sfms/mf10"))) {
            var offenders = paths.filter(p -> p.toString().endsWith(".java")).filter(p -> {
                try {
                    String src = Files.readString(p);
                    return src.contains(".transferTo(") || src.contains("software.amazon.awssdk")
                            || src.contains("new FileOutputStream(");
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).toList();
            assertThat(offenders).isEmpty();
        }
    }
}
