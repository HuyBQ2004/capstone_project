package com.sfms.mf03.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.HealthHistoryEvent;
import com.sfms.mf03.dto.HealthHistoryDtos.HealthHistoryEventResponse;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.HealthHistoryEventRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HealthHistoryServiceImpl implements HealthHistoryService {

    private final HealthHistoryEventRepository repository;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final ReferenceResolver refs;

    public HealthHistoryServiceImpl(HealthHistoryEventRepository repository,
                                    StudentRepository studentRepository,
                                    StudentParentRepository studentParentRepository,
                                    ReferenceResolver refs) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.studentParentRepository = studentParentRepository;
        this.refs = refs;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HealthHistoryEventResponse> timeline(String studentCode, ListQuery query, AuthUser actor) {
        // ⚠️ studentCode KHÔNG unique toàn cục — chỉ UNIQUE(school_id, code) (V4 DDL). findByCode
        // trần (không school-scope tại query) có thể trả nhầm/crash nếu 2 trường trùng mã HS —
        // RỦI RO CÓ SẴN, giữ nguyên để khớp đúng hành vi 403/STU-005 hiện có (đổi sang
        // findByCodeAndSchoolId sẽ biến cross-school thành 404, khác hành vi test đang assert —
        // đó là quyết định policy riêng, không tự đổi ở đây). Cùng pattern
        // StudentServiceImpl.requireInScope() — báo riêng cho chủ MF03 để chốt thống nhất.
        var student = studentRepository.findByCode(studentCode).orElseThrow(StuExceptions::notFound);
        UUID studentId = student.getId();
        // C2 — phạm vi dữ liệu: School/Medical chỉ trường mình.
        if (!actor.isSystemAdmin() && actor.schoolId() != null && !actor.schoolId().equals(student.getSchoolId())) {
            throw StuExceptions.accessDenied();
        }
        if (actor.role() == RoleKey.PARENT
                && !studentParentRepository.existsByStudentIdAndParentUserId(studentId, actor.userId())) {
            throw StuExceptions.accessDenied();
        }
        Page<HealthHistoryEvent> page = repository.findByStudentIdOrderByAtDesc(studentId, query.toPageable());
        List<HealthHistoryEventResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public void append(UUID studentId, HealthHistoryEventType type, String event, UUID byUserId) {
        // C7/BR-173 — KHÔNG log tên HS/nội dung khai báo ở INFO; chỉ ghi vào DB (append-only).
        repository.save(new HealthHistoryEvent(studentId, type, event, byUserId));
    }

    private HealthHistoryEventResponse toResponse(HealthHistoryEvent e) {
        String[] byUser = refs.userById(e.getByUserId());
        String studentCode = studentRepository.findById(e.getStudentId())
                .map(com.sfms.mf03.domain.Student::getCode).orElse(null);
        return new HealthHistoryEventResponse(e.getId().toString(), studentCode,
                e.getAt(), e.getType().name(), e.getEvent(), byUser == null ? null : byUser[2]);
    }
}
