package com.sfms.mf05.event;

import com.sfms.common.enums.MealType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event MF05 phát ra khi thực đơn đổi trạng thái (TDS-MF05_Backend.md).
 *
 * <p>Theo đúng khuôn {@code com.sfms.iam.event.IamEvents} (Bên phát/Bên tiêu thụ khác module):
 * field tham chiếu chéo module là <b>mã nghiệp vụ</b> ({@code menuCode}/{@code schoolCode}, D1),
 * KHÔNG phải UUID nội bộ — bên nhận tự resolve qua {@code ReferenceResolver}.
 */
public final class Mf05Events {

    private Mf05Events() {
    }

    /**
     * UC-006/UC-087 — thực đơn chuyển sang {@code PUBLISHED} (cả 2 đường: action endpoint
     * {@code POST /publish} và CF-8 nhảy thẳng {@code PENDING_APPROVAL→PUBLISHED} qua PUT).
     * Phát cho MỌI {@code audience} (kể cả thực đơn riêng theo nhóm chế độ ăn) — sửa 06/08/2026:
     * trước đây chỉ phát cho {@code audience = "ALL"} với lý do "thực đơn riêng không có lớp/khối
     * cố định", nhưng {@code DistributionPlanHandler} giờ đã tự lọc đúng HS theo từng audience
     * (xem Javadoc ở đó) nên không còn giới hạn này.
     *
     * <p>Tiêu thụ bởi MF09 {@code DistributionPlanHandler} — TDS-MF09_Backend.md §7.2, sinh
     * phiên phát suất {@code PENDING} cho từng HS ĐÚNG audience của khối × ngày × bữa (ràng buộc C3).
     */
    /**
     * V44 — {@code grade} (text tự do) trở thành {@code gradeId} (UUID nội bộ của
     * {@code grades}). Đây là NGOẠI LỆ có chủ đích với quy ước D1 "field tham chiếu chéo module
     * là mã nghiệp vụ": {@code grades} không có cột {@code code} riêng (xem Javadoc
     * {@code com.sfms.mf02.domain.Grade}) — UUID chính là "public id" duy nhất tồn tại.
     */
    public record MenuPublished(String menuCode, String schoolCode, LocalDate date, MealType mealType,
                                 UUID gradeId, String audience, Instant occurredAt) {}
}
