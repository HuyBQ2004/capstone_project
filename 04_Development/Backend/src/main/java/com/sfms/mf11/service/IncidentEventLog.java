package com.sfms.mf11.service;

import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentEvent;
import com.sfms.mf11.domain.IncidentEventType;
import com.sfms.mf11.repository.IncidentEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Ghi sổ sự kiện APPEND-ONLY (ADR-M11-2, C4) — điểm ghi DUY NHẤT cho mọi service. Dòng thời
 * gian KHÔNG được dựng lại từ cột trạng thái; mã sự kiện theo khuôn {@code IEV-<số INC>-<seq>}
 * (TDS §5.2), đánh số TRONG PHẠM VI một sự cố bằng {@code count+1} — không qua {@code code_sequences}.
 */
@Component
public class IncidentEventLog {

    private final IncidentEventRepository repository;
    private final Clock clock;

    public IncidentEventLog(IncidentEventRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void append(Incident incident, IncidentEventType type, String description, String actorLabel) {
        append(incident.getId(), incident.getCode(), type, description, actorLabel);
    }

    /** Overload dùng khi chỉ có {@code incidentId}/{@code code} (VD từ CapaService, không tải cả aggregate). */
    @Transactional
    public void append(java.util.UUID incidentId, String incidentCode, IncidentEventType type,
                        String description, String actorLabel) {
        long seq = repository.countByIncidentId(incidentId) + 1;
        String numSuffix = incidentCode.substring(incidentCode.lastIndexOf('-') + 1);
        String code = "IEV-" + numSuffix + "-" + String.format("%02d", seq);
        Instant at = Instant.now(clock);
        repository.saveAndFlush(new IncidentEvent(incidentId, code, at, type, description, actorLabel));
    }
}
