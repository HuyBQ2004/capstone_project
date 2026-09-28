package com.sfms.mf08.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.UUID;

/**
 * {@code sample_label_reservations} — đặt trước mã/tem Mẫu số 4 cho món cần lấy mẫu của một
 * bữa SẮP TỚI (quyết định của Nam, 10/08/2026), trước khi có đủ dữ liệu để tạo {@link FoodSample}
 * thật (ảnh, khối lượng, nhiệt độ tủ...). Mã đặt trước dùng CHUNG scope bộ đếm với
 * {@code FoodSampleServiceImpl.resolveCreateCode()} nên lúc lấy mẫu thật, gửi lại đúng mã này
 * làm {@code id} của {@code POST /samples} là hợp lệ và không bao giờ đụng mã khác.
 */
@Entity
@Table(name = "sample_label_reservations",
        uniqueConstraints = @UniqueConstraint(name = "uq_smplbl_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_smplbl_school_date", columnList = "school_id,collect_date"),
                @Index(name = "ix_smplbl_status", columnList = "status")
        })
public class SampleLabelReservation extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "collect_date", nullable = false)
    private LocalDate collectDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 16)
    private MealType mealType;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "dish_name", nullable = false, length = 180)
    private String dishName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SampleLabelReservationStatus status = SampleLabelReservationStatus.PENDING;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public LocalDate getCollectDate() { return collectDate; }
    public void setCollectDate(LocalDate collectDate) { this.collectDate = collectDate; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public UUID getDishId() { return dishId; }
    public void setDishId(UUID dishId) { this.dishId = dishId; }
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
    public SampleLabelReservationStatus getStatus() { return status; }
    public void setStatus(SampleLabelReservationStatus status) { this.status = status; }
}
