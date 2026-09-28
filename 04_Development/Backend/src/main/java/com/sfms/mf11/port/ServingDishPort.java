package com.sfms.mf11.port;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Port sang MF09 (Meal Serving &amp; Distribution) — "em nào đã ăn món này?", mắt xích cuối
 * của trục xương sống. TDS-MF11_Backend.md §8.3 (v1.2: gộp thêm nửa hành vi của
 * {@code DishQueryPort} cũ — món ĐÃ PHỤC VỤ trong cửa sổ điều tra, dữ liệu {@code serving_dishes}
 * của MF09, không phải danh mục món của MF05).
 */
public interface ServingDishPort {

    record ServedStudent(UUID studentId, String studentName, String className, Instant servedAt) {}

    List<ServedStudent> studentsWhoAte(UUID dishId, LocalDate date, UUID schoolId);

    record ServedDishBrief(UUID dishId, String dishName, String mealType, Instant servedAt) {}

    /** BR-149 (sửa 27/07: 24 giờ) — món đã phục vụ trong cửa sổ điều tra {@code [from, to]}. */
    List<ServedDishBrief> dishesServedBetween(UUID schoolId, Instant from, Instant to);

    /**
     * BR-149 (backlog B4) — món đã phục vụ trong cửa sổ điều tra, GIAO với tập học sinh bị ảnh
     * hưởng, kèm SỐ em bị ảnh hưởng đã ăn món đó.
     *
     * <p>Đây là câu hỏi điều tra thật, khác hẳn {@link #dishesServedBetween}: bản cũ trả TOÀN BỘ
     * món cả trường trong 24h nên hôm nào cũng ra y hệt nhau, không khoanh vùng được gì. Giao
     * với {@code incident_affected_students} rồi sắp theo số em cùng ăn mới ra được món nghi ngờ.
     */
    record ServedDishByAffected(UUID dishId, String dishName, String mealType, Instant servedAt,
                                 long affectedStudentsWhoAte) {}

    List<ServedDishByAffected> dishesEatenByStudentsBetween(UUID schoolId, List<UUID> studentIds,
                                                             Instant from, Instant to);
}
