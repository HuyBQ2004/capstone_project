package com.sfms.mf08.service;

import com.sfms.common.storage.StorageService;
import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.AttachmentOwner;
import com.sfms.mf08.domain.SampleAttachment;
import com.sfms.mf08.repository.SampleAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF08-TC-019 — upload đi qua {@code common/storage/StorageService} (D10), không tự
 * {@code MultipartFile.transferTo()}.
 *
 * <p>⚠️ Ground-truth: class thật là {@code AttachmentSupport} (package-private, dùng chung
 * giữa {@code FoodSampleService} và {@code SampleLabRequestService}) — spec gọi nó
 * "SampleAttachmentService", không tồn tại tên đó trong code.
 *
 * <p>⚠️ GAP thật (không bịa test cho tính năng không tồn tại — lesson 9): {@code AttachmentSupport}
 * KHÔNG kiểm kích thước file (MSG-008, >10MB) và KHÔNG kiểm magic bytes / whitelist định dạng
 * (MSG-009) — nó gọi thẳng {@code storageService.store(key, file)} cho MỌI {@code MultipartFile}
 * không rỗng, bất kể phần mở rộng/kích thước. Test dưới xác nhận ĐÚNG hiện trạng (mọi file hợp
 * lệ về mặt "không rỗng" đều được lưu, không có nhánh 422 MSG-008/009 nào để test) thay vì giả
 * vờ validate hoạt động. Xem thêm {@code security/SampleUploadSecurityIT} cho nhánh HTTP.
 */
@ExtendWith(MockitoExtension.class)
class SampleAttachmentServiceTest {

    @Mock private StorageService storageService;
    @Mock private SampleAttachmentRepository attachmentRepository;

    private AttachmentSupport support;
    private final UUID ownerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        support = new AttachmentSupport(storageService, attachmentRepository) {};
    }

    @Test
    void validPhotoIsStoredThroughStorageServiceWithMf08KeyPrefix() {
        MultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3});

        support.store(AttachmentOwner.SAMPLE, ownerId, AttachmentKind.EVIDENCE_PHOTO, List.of(photo));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).store(keyCaptor.capture(), eq(photo));
        // Khuôn key thật: "mf08/<owner>/<ownerId>/<kind>/<uuid>.<ext>" — KHÔNG phải
        // "samples/<code>.<ext>" mà TestSpec giả định (spec không biết ownerId là UUID nội bộ,
        // không phải mã nghiệp vụ — service chỉ có UUID lúc gọi store(), trước khi có response).
        assertThat(keyCaptor.getValue()).startsWith("mf08/sample/" + ownerId + "/evidence_photo/");
        assertThat(keyCaptor.getValue()).endsWith(".jpg");
    }

    @Test
    void storedAttachmentRecordHasChecksumAndSizeAndOriginalFileName() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3};
        MultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg", bytes);

        support.store(AttachmentOwner.SAMPLE, ownerId, AttachmentKind.EVIDENCE_PHOTO, List.of(photo));

        ArgumentCaptor<SampleAttachment> captor = ArgumentCaptor.forClass(SampleAttachment.class);
        verify(attachmentRepository).saveAndFlush(captor.capture());
        SampleAttachment saved = captor.getValue();
        assertThat(saved.getFileName()).isEqualTo("mau.jpg");
        assertThat(saved.getSizeBytes()).isEqualTo(bytes.length);
        assertThat(saved.getChecksumSha256()).hasSize(64);   // SHA-256 hex = 64 ký tự
        assertThat(saved.getKind()).isEqualTo(AttachmentKind.EVIDENCE_PHOTO);
        assertThat(saved.getOwnerType()).isEqualTo(AttachmentOwner.SAMPLE);
    }

    @Test
    void nullOrEmptyFilesInListAreSkippedNotStored() {
        MultipartFile empty = new MockMultipartFile("evidencePhotos", "empty.jpg", "image/jpeg", new byte[0]);

        support.store(AttachmentOwner.SAMPLE, ownerId, AttachmentKind.EVIDENCE_PHOTO,
                java.util.Arrays.asList(empty, null));

        verify(storageService, never()).store(any(), any());
        verify(attachmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void nullFileListDoesNothing() {
        support.store(AttachmentOwner.SAMPLE, ownerId, AttachmentKind.EVIDENCE_PHOTO, null);

        verify(storageService, never()).store(any(), any());
    }

    /**
     * GAP THẬT — ghi lại đúng hiện trạng: file 12MB không bị chặn (không có kiểm kích thước
     * nào trong {@code AttachmentSupport}). KHÔNG assert 422 MSG-008 vì nhánh đó không tồn tại.
     */
    @Test
    void oversizedFileIsCurrentlyAcceptedNoSizeLimitEnforced() {
        byte[] bigButFakeContent = new byte[1024];   // đủ để test, không cần 12MB thật cho unit test
        MultipartFile oversize = new MockMultipartFile("evidencePhotos", "oversize.jpg", "image/jpeg", bigButFakeContent);

        support.store(AttachmentOwner.SAMPLE, ownerId, AttachmentKind.EVIDENCE_PHOTO, List.of(oversize));

        verify(storageService).store(startsWith("mf08/sample/"), eq(oversize));
    }

    /**
     * GAP THẬT — {@code note.txt} với content-type/đuôi bất kỳ vẫn được lưu; không có whitelist
     * loại file hay kiểm magic bytes ở tầng {@code AttachmentSupport}.
     */
    @Test
    void arbitraryFileExtensionIsCurrentlyAcceptedNoTypeWhitelistEnforced() {
        MultipartFile textFile = new MockMultipartFile("evidencePhotos", "note.txt", "text/plain",
                "not an image".getBytes());

        support.store(AttachmentOwner.SAMPLE, ownerId, AttachmentKind.EVIDENCE_PHOTO, List.of(textFile));

        verify(storageService).store(any(), eq(textFile));
    }

    @Test
    void storageKeyNeverEmbedsClientSuppliedFileNameOnlyRandomUuid() {
        // Bảo vệ CẤU TRÚC chống path traversal: key được sinh từ UUID.randomUUID(), KHÔNG
        // ghép tên file gốc — dù client gửi filename "../../etc/passwd.jpg" thì key vẫn an
        // toàn (khác hẳn cơ chế filename-based mà TestSpec SEC-005 giả định).
        MultipartFile malicious = new MockMultipartFile("evidencePhotos", "../../etc/passwd.jpg", "image/jpeg",
                new byte[]{1, 2, 3});

        support.store(AttachmentOwner.SAMPLE, ownerId, AttachmentKind.EVIDENCE_PHOTO, List.of(malicious));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).store(keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).doesNotContain("..").doesNotContain("etc/passwd");
    }
}
