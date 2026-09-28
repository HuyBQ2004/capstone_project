package com.sfms.mf02.domain;

/**
 * Vòng đời năm học (TDS-MF02 §6.3, BR-191/193/195). Không thuộc D6 — chỉ dùng ở MF02.
 *
 * <p>Bất biến: DRAFT→ACTIVE→CLOSED→ARCHIVED; ≤1 {@code ACTIVE}/trường tại một thời điểm
 * (BR-191); {@code CLOSED}/{@code ARCHIVED} không xóa dữ liệu, luôn truy vấn được (BR-195).
 */
public enum AcademicYearStatus {
    DRAFT,
    ACTIVE,
    CLOSED,
    ARCHIVED
}
