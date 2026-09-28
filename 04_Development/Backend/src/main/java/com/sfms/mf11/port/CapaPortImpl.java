package com.sfms.mf11.port;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf11.domain.CapaStatus;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Year;
import java.util.UUID;

@Component
public class CapaPortImpl implements CapaPort {

    private final CorrectiveActionRepository repository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public CapaPortImpl(CorrectiveActionRepository repository, ReferenceResolver referenceResolver,
                         CodeGenerator codeGenerator, Clock clock) {
        this.repository = repository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public String openFromFailedStep(UUID inspectionSessionId, String step, String description,
                                      UUID schoolId, UUID actorId) {
        var existing = repository.findByInspectionId(inspectionSessionId);
        if (existing.isPresent()) {
            return existing.get().getCode();
        }

        String inspectionCode = referenceResolver.inspectionCode(inspectionSessionId);
        CorrectiveAction capa = new CorrectiveAction();
        capa.setCode(codeGenerator.next("mf11_capa:" + Year.now(clock).getValue(),
                "CAPA-" + Year.now(clock).getValue(), 3));
        capa.setSchoolId(schoolId);
        capa.setSourceLabel("Phiên kiểm thực " + (inspectionCode == null ? inspectionSessionId : inspectionCode)
                + " — " + (step == null ? "" : step));
        capa.setInspectionId(inspectionSessionId);
        capa.setDescription(description == null || description.isBlank()
                ? "Khắc phục sau khi bước kiểm thực trượt (BR-095)." : description);
        capa.setAssigneeLabel("Chờ phân công đơn vị suất ăn");
        capa.setStatus(CapaStatus.DRAFT);
        repository.saveAndFlush(capa);
        return capa.getCode();
    }
}
