package com.sfms.mf11.port;

import com.sfms.mf11.repository.ViolationRecordRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class ViolationQueryPortImpl implements ViolationQueryPort {

    private final ViolationRecordRepository repository;

    public ViolationQueryPortImpl(ViolationRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViolationBrief> bySourceInspection(UUID inspectionSessionId) {
        if (inspectionSessionId == null) {
            return List.of();
        }
        return repository.findBySourceInspectionId(inspectionSessionId).stream()
                .map(v -> new ViolationBrief(v.getCode(), v.getDate(),
                        v.getSeverity() == null ? null : v.getSeverity().name(),
                        v.getViolationType() == null ? null : v.getViolationType().name(),
                        v.getResolution().name(), v.getStatus().name()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViolationBrief> inPeriod(UUID schoolId, LocalDate from, LocalDate to) {
        return repository.findBySchoolIdAndDateBetweenOrderByDateAsc(schoolId, from, to).stream()
                .map(v -> new ViolationBrief(v.getCode(), v.getDate(),
                        v.getSeverity() == null ? null : v.getSeverity().name(),
                        v.getViolationType() == null ? null : v.getViolationType().name(),
                        v.getResolution().name(), v.getStatus().name()))
                .toList();
    }
}
