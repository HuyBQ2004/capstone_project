package com.sfms.mf11.port;

import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.repository.IncidentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Bean thật của {@link IncidentStatusPort} — MF08 chờ từ {@code M08-D08}, đã merge và chạy
 * bằng stub cho tới bản này (`M11-D08`, hạng mục Bước 2 bắt buộc).
 *
 * <p>⚠️ File {@code com/sfms/mf08/port/IncidentStatusPortStub.java} PHẢI bị xoá cùng đợt —
 * 2 bean cùng interface không có {@code @Primary} làm Spring context load lỗi ambiguous.
 */
@Component
public class IncidentStatusPortImpl implements IncidentStatusPort {

    private final IncidentRepository incidentRepository;

    public IncidentStatusPortImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IncidentRef> resolveByCode(String code, UUID schoolId) {
        if (code == null || code.isBlank() || schoolId == null) {
            return Optional.empty();
        }
        return incidentRepository.findByCodeAndSchoolId(code, schoolId)
                .map(i -> new IncidentRef(i.getId(), i.getCode(), i.getStatus() != IncidentStatus.CLOSED));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IncidentRef> findOpen(UUID incidentId, UUID schoolId) {
        if (incidentId == null || schoolId == null) {
            return Optional.empty();
        }
        return incidentRepository.findById(incidentId)
                .filter(i -> i.getSchoolId().equals(schoolId))
                .map(i -> new IncidentRef(i.getId(), i.getCode(), i.getStatus() != IncidentStatus.CLOSED));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOpenIncidentLinked(UUID sampleIncidentId) {
        if (sampleIncidentId == null) {
            return false;
        }
        return incidentRepository.findById(sampleIncidentId)
                .map(i -> i.getStatus() != IncidentStatus.CLOSED)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public InvestigationWindow windowOf(UUID incidentId) {
        Instant to = incidentRepository.findById(incidentId).map(this::anchor).orElseGet(Instant::now);
        return new InvestigationWindow(to.minus(24, ChronoUnit.HOURS), to);
    }

    /** BR-149 (sửa 27/07: 24h) — mốc gốc {@code occurredAt → mealDate → reportedAt}. */
    private Instant anchor(Incident incident) {
        if (incident.getOccurredAt() != null) {
            return incident.getOccurredAt();
        }
        LocalDate mealDate = incident.getMealDate();
        if (mealDate != null) {
            return mealDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
        return incident.getReportedAt();
    }
}
