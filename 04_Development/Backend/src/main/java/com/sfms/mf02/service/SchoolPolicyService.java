package com.sfms.mf02.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.domain.School;
import com.sfms.mf02.domain.SchoolConfig;
import com.sfms.mf02.dto.SchoolPolicyDtos.SchoolPolicyResponse;
import com.sfms.mf02.error.SchExceptions;
import com.sfms.mf02.repository.SchoolConfigRepository;
import com.sfms.mf02.repository.SchoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-137 View Policy Of School (BR-022) — Teacher/Medical Staff/Service Caterers chỉ xem
 * chính sách trường của CHÍNH JWT; không nhận {@code schoolId} qua param (TDS §17.1 C2).
 */
@Service
public class SchoolPolicyService {

    private final SchoolRepository schoolRepository;
    private final SchoolConfigRepository configRepository;

    public SchoolPolicyService(SchoolRepository schoolRepository, SchoolConfigRepository configRepository) {
        this.schoolRepository = schoolRepository;
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public SchoolPolicyResponse getForCurrentUser(AuthUser actor) {
        if (actor.schoolId() == null) {
            // System Admin/Supplier không thuộc trường nào — không có chính sách để xem.
            throw SchExceptions.notFound();
        }
        School school = schoolRepository.findById(actor.schoolId()).orElseThrow(SchExceptions::notFound);
        int geofence = configRepository.findBySchoolId(school.getId())
                .map(SchoolConfig::getGeofenceRadius)
                .orElse(200);

        return new SchoolPolicyResponse(school.getCode(), school.getName(), geofence,
                school.getLat(), school.getLng());
    }
}
