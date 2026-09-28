package com.sfms.mf07.port;

import com.sfms.mf07.domain.KitchenInspection;
import com.sfms.mf07.domain.StepStatus;
import com.sfms.mf07.repository.KitchenInspectionRepository;
import com.sfms.mf11.port.ViolationQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class ComplianceDataPortImpl implements ComplianceDataPort {

    private final KitchenInspectionRepository kitchenInspectionRepository;
    private final ViolationQueryPort violationQueryPort;

    public ComplianceDataPortImpl(KitchenInspectionRepository kitchenInspectionRepository,
                                   ViolationQueryPort violationQueryPort) {
        this.kitchenInspectionRepository = kitchenInspectionRepository;
        this.violationQueryPort = violationQueryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public CompliancePeriodData kitchenInspectionsIn(UUID schoolId, LocalDate from, LocalDate to) {
        List<KitchenInspection> inspections =
                kitchenInspectionRepository.findBySchoolIdAndDateBetweenOrderByDateAsc(schoolId, from, to);

        long pass = inspections.stream().filter(k -> k.getResult() == StepStatus.PASSED).count();
        long fail = inspections.stream().filter(k -> k.getResult() == StepStatus.FAILED).count();
        double avg = inspections.stream().mapToInt(KitchenInspection::getScore).average().orElse(0.0);

        List<InspectionBrief> briefs = inspections.stream()
                .map(k -> new InspectionBrief(k.getCode(), k.getDate(), k.getScore(),
                        k.getResult() == null ? null : k.getResult().name(), k.isStaffHealthValid()))
                .toList();

        return new CompliancePeriodData(inspections.size(), avg, pass, fail, briefs);
    }

    @Override
    @Transactional(readOnly = true)
    public ViolationPeriodData violationsIn(UUID schoolId, LocalDate from, LocalDate to) {
        List<ViolationQueryPort.ViolationBrief> violations = violationQueryPort.inPeriod(schoolId, from, to);
        List<ViolationBrief> briefs = violations.stream()
                .map(v -> new ViolationBrief(v.code(), v.date(), v.severity(), v.violationType(), v.status()))
                .toList();
        return new ViolationPeriodData(briefs.size(), briefs);
    }
}
