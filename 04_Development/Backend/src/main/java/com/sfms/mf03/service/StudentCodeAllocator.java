package com.sfms.mf03.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.reference.ReferenceResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * D1 — cấp mã học sinh {@code HS-<mã lớp>-<3 chữ số>} ({@code HS-3A-001}).
 *
 * <p>13/08/2026 (Nam) — trước đây cả {@code StudentServiceImpl} lẫn {@code StudentImportServiceImpl}
 * đều có bản private giống hệt nhau, suy mã từ SĨ SỐ:
 *
 * <pre>{@code
 * long seq = studentRepository.countByClassroomId(classroomId) + 1;   // ← sai
 * }</pre>
 *
 * <p>Đếm chỉ đúng khi dãy mã không bao giờ thủng. Xoá một học sinh giữa dãy là sĩ số tụt về một
 * số ĐÃ CẤP rồi ⇒ cấp trùng mã ⇒ vỡ {@code uq_student_school_code}. Chuyện này thành cấp bách khi
 * dọn đống học sinh nhân bản do bug nhập Excel (xem {@code scripts/cleanup_duplicate_students.sql}):
 * dọn xong lớp 1A còn 25 em trong khi mã đã cấp tới HS-1A-045, em tiếp theo sẽ được cấp
 * HS-1A-026 — trùng một em đang tồn tại.
 *
 * <p>Nay dùng {@link CodeGenerator} — bộ đếm nằm ở bảng {@code code_sequences}, mỗi lớp một scope
 * {@code mf03_student:<classroomId>}, tăng bằng {@code INSERT … ON CONFLICT DO UPDATE … RETURNING}
 * nên không đua nhau cấp trùng, và {@code REQUIRES_NEW} nên KHÔNG lùi lại khi transaction nghiệp
 * vụ rollback. Đổi lại dãy mã có thể thủng số — chấp nhận được, cấp trùng thì không (D1).
 *
 * <p>Bộ đếm của các lớp đang có dữ liệu được nạp sẵn ở migration {@code V75} từ mã lớn nhất hiện
 * có, nên bản vá này không cấp đè lên mã cũ.
 */
@Component
public class StudentCodeAllocator {

    private final ReferenceResolver refs;
    private final CodeGenerator codeGenerator;

    public StudentCodeAllocator(ReferenceResolver refs, CodeGenerator codeGenerator) {
        this.refs = refs;
        this.codeGenerator = codeGenerator;
    }

    /** @return mã kế tiếp của lớp, VD {@code HS-3A-007}. */
    public String next(UUID classroomId) {
        return codeGenerator.next(scopeOf(classroomId), "HS-" + refs.classroomCode(classroomId), 3);
    }

    /**
     * Khóa bộ đếm của một lớp. {@code public} vì migration V75 phải sinh ĐÚNG chuỗi này — đổi ở
     * đây mà quên migration là bộ đếm quay về 1 và cấp đè mã cũ.
     */
    public static String scopeOf(UUID classroomId) {
        return "mf03_student:" + classroomId;
    }
}
