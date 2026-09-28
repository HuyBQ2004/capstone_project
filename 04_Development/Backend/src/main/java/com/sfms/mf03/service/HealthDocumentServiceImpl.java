package com.sfms.mf03.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.domain.Student;
import com.sfms.mf03.dto.HealthDocumentDtos.HealthDocumentResponse;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.HealthDocumentRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.List;
import java.util.Set;

/**
 * Quyết định nhóm 04/08/2026 — copy khuôn {@code ProfileService.updatePhoto} (MF01, đã chạy
 * production) cho evidence upload của MF03: cùng ngưỡng 10MB/MSG-008, cùng danh sách định dạng
 * PDF/JPG/PNG/MSG-009, cùng kiểm magic bytes (chặn SVG đổi đuôi — XSS). Khác MF01 ở 2 điểm:
 * (1) tài liệu RIÊNG TƯ (D10-BIS mục 2) — lưu {@code key} tham số vào DB, KHÔNG lưu URL công khai
 * {@code storageService.store()} trả về, đúng javadoc {@link StorageService#store}; (2) RBAC theo
 * PHẠM VI học sinh (C2), không phải "own resource" đơn giản như ảnh hồ sơ.
 */
@Service
public class HealthDocumentServiceImpl implements HealthDocumentService {

    private static final byte[] MAGIC_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] MAGIC_PNG = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] MAGIC_PDF = {'%', 'P', 'D', 'F'};

    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final HealthDocumentRepository healthDocumentRepository;
    private final StorageService storageService;
    private final CodeGenerator codeGenerator;
    private final long maxBytes;
    private final Set<String> allowedTypes;

    public HealthDocumentServiceImpl(StudentRepository studentRepository,
                                      StudentParentRepository studentParentRepository,
                                      HealthDocumentRepository healthDocumentRepository,
                                      StorageService storageService,
                                      CodeGenerator codeGenerator,
                                      @Value("${sfms.health-document.max-bytes:10485760}") long maxBytes,
                                      @Value("${sfms.health-document.allowed-types:"
                                              + "application/pdf,image/jpeg,image/png}") String allowedTypes) {
        this.studentRepository = studentRepository;
        this.studentParentRepository = studentParentRepository;
        this.healthDocumentRepository = healthDocumentRepository;
        this.storageService = storageService;
        this.codeGenerator = codeGenerator;
        this.maxBytes = maxBytes;
        this.allowedTypes = Set.copyOf(List.of(allowedTypes.split(",")));
    }

    @Override
    @Transactional
    public HealthDocumentResponse upload(String studentCode, MultipartFile file, AuthUser actor) {
        Student student = studentRepository.findByCode(studentCode).orElseThrow(StuExceptions::notFound);
        enforceScope(student, actor);

        if (file == null || file.isEmpty()) {
            throw StuExceptions.unsupportedFileType();
        }
        if (file.getSize() > maxBytes) {
            throw StuExceptions.fileTooLarge();
        }
        if (!allowedTypes.contains(file.getContentType()) || !hasAllowedMagicBytes(file)) {
            throw StuExceptions.unsupportedFileType();
        }

        String key = "health-documents/" + student.getCode() + "/" + java.util.UUID.randomUUID()
                + suffixOf(file.getContentType());
        // Tài liệu RIÊNG TƯ (D10-BIS mục 2) — lưu key tham số, KHÔNG lưu URL trả về của store().
        storageService.store(key, file);

        int year = Year.now().getValue();
        HealthDocument document = new HealthDocument();
        document.setCode(codeGenerator.next("mf03_health_document:" + year, "HDOC-" + year, 4));
        document.setStudentId(student.getId());
        document.setFileName(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        document.setStorageKey(key);
        document.setContentType(file.getContentType());
        document.setSizeBytes(file.getSize());
        document.setUploadedByUserId(actor.userId());
        healthDocumentRepository.saveAndFlush(document);

        // D1 — trả MÃ NGHIỆP VỤ; khoá UUID không rời khỏi tầng persistence.
        return new HealthDocumentResponse(document.getCode(), document.getFileName(),
                document.getContentType(), document.getSizeBytes(), document.getUploadedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthDocument metadata(String studentCode, String documentCode, AuthUser actor) {
        Student student = studentRepository.findByCode(studentCode).orElseThrow(StuExceptions::notFound);
        enforceScope(student, actor);
        HealthDocument document = healthDocumentRepository.findByCode(documentCode)
                .orElseThrow(StuExceptions::notFound);
        // Tài liệu phải thuộc ĐÚNG học sinh trên URL — nếu chỉ kiểm phạm vi của `studentCode`
        // thì ai cũng đọc được tài liệu của học sinh khác bằng cách ghép mã tài liệu vào đường dẫn
        // của một học sinh mình có quyền (C2).
        if (!document.getStudentId().equals(student.getId())) {
            throw StuExceptions.notFound();
        }
        return document;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource download(String studentCode, String documentCode, AuthUser actor) {
        return storageService.load(metadata(studentCode, documentCode, actor).getStorageKey());
    }

    /** C2 — cùng luật {@code StudentServiceImpl.enforceScope}: Parent chỉ HS liên kết; School/Medical chỉ trường mình. */
    private void enforceScope(Student student, AuthUser actor) {
        if (actor.isSystemAdmin()) {
            return;
        }
        if (!actor.belongsToSchool(student.getSchoolId())) {
            throw StuExceptions.accessDenied();
        }
        if (actor.role() == RoleKey.PARENT
                && !studentParentRepository.existsByStudentIdAndParentUserId(student.getId(), actor.userId())) {
            throw StuExceptions.accessDenied();
        }
    }

    private boolean hasAllowedMagicBytes(MultipartFile file) {
        try {
            byte[] head = new byte[8];
            int read = file.getInputStream().read(head);
            if (read < 4) {
                return false;
            }
            return startsWith(head, MAGIC_JPEG) || startsWith(head, MAGIC_PNG) || startsWith(head, MAGIC_PDF);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String suffixOf(String contentType) {
        return switch (contentType == null ? "" : contentType) {
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }
}
