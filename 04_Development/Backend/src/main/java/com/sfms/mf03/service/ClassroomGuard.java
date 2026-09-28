package com.sfms.mf03.service;

import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 13/08/2026 (Nam) — gác cổng lớp học dùng chung cho MỌI đường tạo/chuyển học sinh của MF03.
 *
 * <p>Trước đây logic này nằm private trong {@code StudentServiceImpl.assertClassroomHasRoom()},
 * nên {@code StudentImportServiceImpl.commit()} (nhập Excel hàng loạt) KHÔNG hề gọi — bug thật
 * tìm thấy trên production 13/08/2026: import đẩy sĩ số lớp 1A vượt quá {@code capacity}, trong
 * khi thêm từng học sinh một thì bị chặn đúng BR-025. Tách ra component để hai đường đi không
 * thể lệch nhau lần nữa.
 *
 * <p>Đọc {@code classrooms} bằng native query, KHÔNG import entity {@code Classroom} của MF02
 * (ADR-M02-5, giữ nguyên cách cũ).
 */
@Component
public class ClassroomGuard {

    private final StudentRepository studentRepository;

    @PersistenceContext
    private EntityManager em;

    public ClassroomGuard(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * BR-025 — STU-010/MSG-142 (chốt 02/08, Q3), KHÔNG mượn MSG-010 của MF02.
     * BR-193 (MF02) — lớp thuộc năm học CLOSED/ARCHIVED là read-only ⇒ STU-193 (409). MF02 đã
     * đánh dấu {@code classrooms.read_only = true} khi đóng năm học (xem
     * {@code AcademicYearService.close()}).
     */
    @Transactional(readOnly = true)
    public void assertHasRoom(UUID classroomId) {
        assertHasRoomFor(classroomId, 1);
    }

    /**
     * Như {@link #assertHasRoom} nhưng cho {@code extra} chỗ cùng lúc — nhập hàng loạt phải kiểm
     * CẢ LÔ trước khi ghi, không thể kiểm từng dòng rồi ghi dần (ghi được nửa lô rồi mới vỡ
     * ở dòng cuối là đúng thứ ADR-M03-6 all-or-nothing muốn tránh).
     */
    @Transactional(readOnly = true)
    public void assertHasRoomFor(UUID classroomId, int extra) {
        int capacity = capacityOfWritableClassroom(classroomId);
        long current = studentRepository.countByClassroomId(classroomId);
        if (current + extra > capacity) {
            throw StuExceptions.classroomCapacityExceeded(capacity, current);
        }
    }

    /**
     * Số chỗ còn trống — dùng ở bước {@code validate()} của nhập hàng loạt để đánh dấu ĐÚNG
     * những dòng vượt sức chứa (người dùng thấy trước khi bấm "Nhập"), thay vì để cả lô ném lỗi
     * ở {@code commit()}. Lớp read-only vẫn ném luôn: không phải chuyện hết chỗ.
     */
    @Transactional(readOnly = true)
    public long remainingSeats(UUID classroomId) {
        int capacity = capacityOfWritableClassroom(classroomId);
        return Math.max(0, capacity - studentRepository.countByClassroomId(classroomId));
    }

    private int capacityOfWritableClassroom(UUID classroomId) {
        var row = em.createNativeQuery("select capacity, read_only from classrooms where id = :id")
                .setParameter("id", classroomId).getResultList();
        if (row.isEmpty()) {
            throw StuExceptions.notFound();
        }
        Object[] cols = (Object[]) row.get(0);
        if ((Boolean) cols[1]) {
            throw StuExceptions.classroomReadOnly();                        // STU-193 (BR-193)
        }
        return ((Number) cols[0]).intValue();
    }
}
