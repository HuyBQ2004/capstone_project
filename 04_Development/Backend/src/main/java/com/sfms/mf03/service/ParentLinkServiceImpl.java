package com.sfms.mf03.service;

import com.sfms.iam.port.ParentAccountPort;
import com.sfms.iam.port.ParentAccountPort.ParentAccountRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf03.domain.StudentParent;
import com.sfms.mf03.dto.StudentDtos.ParentInfoRequest;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.StudentParentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * ADR-M03-1/C4 — MF03 <b>không</b> tự tạo {@code user_accounts}, không tự gửi email; mọi thao
 * tác tài khoản PH đi qua {@link ParentAccountPort} (MF01, việc tồn đọng #3).
 */
@Service
public class ParentLinkServiceImpl implements ParentLinkService {

    private final StudentParentRepository studentParentRepository;
    private final ParentAccountPort parentAccountPort;
    private final ReferenceResolver refs;

    public ParentLinkServiceImpl(StudentParentRepository studentParentRepository,
                                 ParentAccountPort parentAccountPort,
                                 ReferenceResolver refs) {
        this.studentParentRepository = studentParentRepository;
        this.parentAccountPort = parentAccountPort;
        this.refs = refs;
    }

    @Override
    @Transactional
    public LinkResult linkOrCreateParent(UUID studentId, ParentInfoRequest info, AuthUser actor, String ip) {
        boolean created;
        ParentAccountRef parent = parentAccountPort.findParentByEmail(info.email()).orElse(null);
        if (parent == null) {
            String schoolCode = refs.schoolCode(actor.schoolId());
            parent = parentAccountPort.createParentAccount(
                    info.fullName(), info.email(), info.phone(), schoolCode, actor, ip);
            created = true;
        } else {
            created = false;
        }

        // BR-201 — kiểm ở service TRƯỚC (trả đúng mã lỗi); trigger DB check_parent_link_limit
        // là chốt chặn cuối cùng cho race 2 request song song (§5.2 TDS).
        if (studentParentRepository.countByParentUserId(parent.userId()) >= 5) {
            throw StuExceptions.parentLinkLimitReached();
        }

        StudentParent link = new StudentParent();
        link.setStudentId(studentId);
        link.setParentUserId(parent.userId());
        link.setRelationship(info.relationship());
        link.setPrimary(studentParentRepository.countByStudentId(studentId) == 0);
        studentParentRepository.save(link);

        return new LinkResult(parent.userId(), parent.code(), parent.name(), created);
    }

    @Override
    @Transactional
    public void unlink(UUID studentId, UUID parentUserId, AuthUser actor) {
        long remaining = studentParentRepository.countByStudentId(studentId);
        if (remaining <= 1) {
            throw StuExceptions.missingParent();          // BR-030 — không được gỡ PH cuối cùng
        }
        studentParentRepository.findByStudentId(studentId).stream()
                .filter(sp -> sp.getParentUserId().equals(parentUserId))
                .findFirst()
                .ifPresent(studentParentRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLinkedStudents(UUID parentUserId) {
        return studentParentRepository.countByParentUserId(parentUserId);
    }
}
