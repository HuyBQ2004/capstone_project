package com.sfms.mf03.service;

import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.domain.StudentClinicalFile;
import com.sfms.mf03.domain.StudentHealthRecord;
import com.sfms.mf03.dto.HealthFileDtos.HealthDocumentRef;
import com.sfms.mf03.dto.HealthFileDtos.HealthFileResponse;
import com.sfms.mf03.dto.HealthFileDtos.UpdateHealthFileRequest;
import com.sfms.mf03.dto.HealthRecordDtos.HealthRecordRequest;
import com.sfms.mf03.dto.HealthRecordDtos.HealthRecordResponse;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.AllergyDeclarationRepository;
import com.sfms.mf03.repository.StudentClinicalFileRepository;
import com.sfms.mf03.repository.StudentHealthRecordRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.UUID;

/** UC-051/094/050. C8 — {@code appendClinical} chỉ 4 field lâm sàng. */
@Service
public class StudentHealthFileServiceImpl implements StudentHealthFileService {

    private final StudentClinicalFileRepository clinicalRepository;
    private final StudentHealthRecordRepository recordRepository;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final AllergyDeclarationRepository allergyRepository;
    private final HealthDocumentLookup documents;
    private final HealthHistoryService healthHistoryService;

    public StudentHealthFileServiceImpl(StudentClinicalFileRepository clinicalRepository,
                                        StudentHealthRecordRepository recordRepository,
                                        StudentRepository studentRepository,
                                        StudentParentRepository studentParentRepository,
                                        AllergyDeclarationRepository allergyRepository,
                                        HealthDocumentLookup documents,
                                        HealthHistoryService healthHistoryService) {
        this.clinicalRepository = clinicalRepository;
        this.recordRepository = recordRepository;
        this.studentRepository = studentRepository;
        this.studentParentRepository = studentParentRepository;
        this.allergyRepository = allergyRepository;
        this.documents = documents;
        this.healthHistoryService = healthHistoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthFileResponse getFile(UUID studentId, AuthUser actor) {
        assertSchoolScope(studentId, actor);
        StudentClinicalFile file = clinicalRepository.findByStudentId(studentId).orElse(null);
        // D1 — trả MÃ + TÊN FILE thay vì UUID thô: UI cần cả hai (hiện tên, ghép đường tải xuống).
        List<HealthDocumentRef> medicalDocs = allergyRepository.findByStudentId(studentId).stream()
                .map(a -> documents.byId(a.getProofFileId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(doc -> new HealthDocumentRef(doc.getCode(), doc.getFileName()))
                .distinct().toList();
        if (file == null) {
            return new HealthFileResponse(studentIdCode(studentId), null, null, null, null, medicalDocs);
        }
        return new HealthFileResponse(studentIdCode(studentId), file.getMedicalConditions(), file.getMedications(),
                file.getEmergencyContact(), file.getEmergencyInstructions(), medicalDocs);
    }

    @Override
    @Transactional
    public HealthFileResponse appendClinical(UUID studentId, UpdateHealthFileRequest request, AuthUser medical) {
        assertSchoolScope(studentId, medical);
        StudentClinicalFile file = clinicalRepository.findByStudentId(studentId).orElseGet(() -> {
            StudentClinicalFile f = new StudentClinicalFile();
            f.setStudentId(studentId);
            return f;
        });
        // C8 — DTO chỉ khai 4 field lâm sàng nên binding JSON đã tự chặn field lạ ở tầng Jackson
        // KHÔNG (Jackson mặc định bỏ qua field lạ, không lỗi) — chặn tường minh ở controller
        // bằng cách đọc raw JSON map trước khi bind DTO (xem StudentHealthFileController).
        file.setMedicalConditions(request.medicalConditions());
        file.setMedications(request.medications());
        file.setEmergencyContact(request.emergencyContact());
        file.setEmergencyInstructions(request.emergencyInstructions());
        clinicalRepository.saveAndFlush(file);

        healthHistoryService.append(studentId, HealthHistoryEventType.GHI_CHU_LAM_SANG,
                "Cập nhật hồ sơ lâm sàng", medical.userId());

        return getFile(studentId, medical);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthRecordResponse getHealthRecord(UUID studentId, AuthUser actor) {
        assertSchoolScope(studentId, actor);
        return recordRepository.findByStudentId(studentId).map(this::toResponse).orElse(null);
    }

    @Override
    @Transactional
    public HealthRecordResponse upsertHealthRecord(UUID studentId, HealthRecordRequest request, AuthUser actor) {
        assertSchoolScope(studentId, actor);
        StudentHealthRecord record = recordRepository.findByStudentId(studentId).orElseGet(() -> {
            StudentHealthRecord r = new StudentHealthRecord();
            r.setStudentId(studentId);
            r.setStatus(DeclarationStatus.PENDING);
            return r;
        });
        if (request.heightCm() != null) {
            record.setHeightCm(request.heightCm());
        }
        if (request.weightKg() != null) {
            record.setWeightKg(request.weightKg());
        }
        if (request.bloodType() != null) {
            record.setBloodType(request.bloodType());
        }
        recordRepository.saveAndFlush(record);
        return toResponse(record);
    }

    private HealthRecordResponse toResponse(StudentHealthRecord r) {
        BigDecimal bmi = null;
        if (r.getHeightCm() != null && r.getWeightKg() != null && r.getHeightCm().signum() > 0) {
            BigDecimal heightM = r.getHeightCm().divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
            bmi = r.getWeightKg().divide(heightM.multiply(heightM), MathContext.DECIMAL64);
        }
        return new HealthRecordResponse(studentIdCode(r.getStudentId()), r.getHeightCm(), r.getWeightKg(),
                r.getBloodType(), bmi, r.getStatus().name());
    }

    private String studentIdCode(UUID studentId) {
        return studentRepository.findById(studentId)
                .map(com.sfms.mf03.domain.Student::getCode).orElse(null);
    }

    private void assertSchoolScope(UUID studentId, AuthUser actor) {
        if (actor.isSystemAdmin()) {
            return;
        }
        var student = studentRepository.findById(studentId).orElseThrow(StuExceptions::notFound);
        if (actor.role() == com.sfms.common.enums.RoleKey.PARENT) {
            if (!studentParentRepository.existsByStudentIdAndParentUserId(studentId, actor.userId())) {
                throw StuExceptions.accessDenied();
            }
            return;
        }
        if (!actor.belongsToSchool(student.getSchoolId())) {
            throw StuExceptions.accessDenied();
        }
    }
}
