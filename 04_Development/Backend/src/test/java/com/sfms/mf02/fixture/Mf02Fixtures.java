package com.sfms.mf02.fixture;

import com.sfms.mf02.domain.AcademicYear;
import com.sfms.mf02.domain.AcademicYearStatus;
import com.sfms.mf02.domain.Classroom;
import com.sfms.mf02.domain.Grade;
import com.sfms.mf02.domain.School;
import com.sfms.mf02.domain.SchoolStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * CASE 2.0 — Props Isolation Pattern (TestSpec-MF02 §3 TDS-05). Mỗi test gọi factory —
 * KHÔNG shared mutable state giữa các test case (chống AP-AI-002 Green-from-Birth).
 */
public final class Mf02Fixtures {

    public static final UUID SCHOOL_A_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    public static final UUID SCHOOL_B_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0051");
    public static final UUID AY_2025_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0005");

    /** V44 — {@code grades.id} chuẩn dùng trong test (Khối 3, active, SCHOOL_A). */
    public static final UUID GRADE_3_ID = UUID.fromString("00000000-0000-0000-0000-0000000c0003");

    /** V44 — thay {@code int grade} (BR-024 cũ) bằng {@link Grade}. */
    public static Grade grade(Consumer<Grade> overrides) {
        Grade g = new Grade();
        g.setId(GRADE_3_ID);
        g.setSchoolId(SCHOOL_A_ID);
        g.setName("Khối 3");
        g.setSortOrder(3);
        g.setActive(true);
        overrides.accept(g);
        return g;
    }

    /** Baseline hợp lệ — đồng bộ FX-011 (§3 TDS-05). Mọi override qua {@link Consumer}. */
    public static Classroom classroom(Consumer<Classroom> overrides) {
        Classroom c = new Classroom();
        c.setCode("3A");                 // D1: code = mã nghiệp vụ = tên lớp (L2)
        c.setName("3A");
        c.setSchoolId(SCHOOL_A_ID);
        c.setAcademicYearId(AY_2025_ID);
        c.setGrade(grade(g -> {
        }));                               // V44 — FK Grade thay BR-024 cũ
        c.setCapacity(45);                 // BR-025
        c.setReadOnly(false);              // BR-193
        overrides.accept(c);
        return c;
    }

    public static AcademicYear academicYear(Consumer<AcademicYear> overrides) {
        AcademicYear ay = new AcademicYear();
        ay.setCode("AY-2026");
        ay.setSchoolId(SCHOOL_A_ID);
        ay.setName("Năm học 2026–2027");
        /*
         * Ngày TƯƠNG ĐỐI, không phải hằng cố định (sửa 08/08/2026).
         *
         * Từ 08/08/2026 `activate()` chặn năm học ĐÃ KẾT THÚC. Fixture cũ ghi cứng
         * 2026-09-05 → 2027-05-31, nên sau 31/05/2027 mọi test dùng nó sẽ vỡ — bom hẹn giờ
         * trong bộ test, và người sửa sẽ tưởng cổng mới bị lỗi. Neo theo "hôm nay" thì fixture
         * luôn là một năm học ĐANG DIỄN RA, đúng ý nghĩa mặc định của nó.
         */
        ay.setStartDate(LocalDate.now().minusMonths(3));   // D4: ISO-8601
        ay.setEndDate(LocalDate.now().plusMonths(6));
        ay.setStatus(AcademicYearStatus.DRAFT);           // D6: SCREAMING_SNAKE
        overrides.accept(ay);
        return ay;
    }

    public static School school(Consumer<School> overrides) {
        School s = new School();
        s.setCode("SCH-HN-0042");
        s.setName("TH Đoàn Thị Điểm");
        s.setTaxId("0101245678");
        s.setEmail("contact@dtd.edu.vn");
        s.setLat(new BigDecimal("21.031200"));            // BR-028
        s.setLng(new BigDecimal("105.763800"));
        s.setStatus(SchoolStatus.ACTIVE);
        s.setCoordinateLocked(true);                      // BR-028
        overrides.accept(s);
        return s;
    }

    private Mf02Fixtures() {
    }
}
