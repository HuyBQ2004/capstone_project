package com.sfms.mf08.domain;

/** Vòng đời {@link SampleLabelReservation} — đặt trước tem, độc lập với {@link FoodSampleStatus}. */
public enum SampleLabelReservationStatus {
    /** Đã đặt trước, chưa dùng để lưu mẫu thật. */
    PENDING,

    /** Đã dùng mã này để tạo một {@link FoodSample} thật (BR-069). */
    COLLECTED,

    /** Bếp huỷ chỗ đặt trước — không lấy mẫu món này nữa. */
    CANCELLED
}
