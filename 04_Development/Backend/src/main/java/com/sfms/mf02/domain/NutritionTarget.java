package com.sfms.mf02.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Chuẩn dinh dưỡng bữa trưa của MỘT khối trong MỘT trường (BR-062) — xem migration V54.
 *
 * <p>Trước 07/08/2026 chuẩn này là hằng số toàn hệ thống nằm trong code
 * ({@code MealHistoryServiceImpl.NUTRITION_TARGET}), nên không trường nào sửa được và mọi
 * khối bị chấm cùng một thước dù {@code menus} đã gắn {@code grade_id} từ V44.
 *
 * <p>Cùng quy ước {@link Grade}: KHÔNG có cột {@code code} riêng, API dùng thẳng
 * {@code gradeId} (UUID của khối) làm khoá — bản ghi này không có định danh nghiệp vụ độc
 * lập với khối mà nó cấu hình, nên thêm một mã ngắn nữa chỉ tạo hai đường gọi cho cùng một
 * thứ.
 *
 * <p><b>Không có dòng ⇒ chưa cấu hình</b>, và service trả hằng mặc định. Đừng biểu diễn
 * "chưa khai" bằng 0 — DB có CHECK chặn số không dương đúng vì lý do đó.
 */
@Entity
@Table(name = "nutrition_targets", uniqueConstraints = @UniqueConstraint(
        name = "uq_nutrition_target_school_grade", columnNames = {"school_id", "grade_id"}))
public class NutritionTarget extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "grade_id", nullable = false)
    private UUID gradeId;

    @Column(name = "kcal", nullable = false)
    private BigDecimal kcal;

    @Column(name = "protein", nullable = false)
    private BigDecimal protein;

    @Column(name = "fat", nullable = false)
    private BigDecimal fat;

    @Column(name = "carb", nullable = false)
    private BigDecimal carb;

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public UUID getGradeId() {
        return gradeId;
    }

    public void setGradeId(UUID gradeId) {
        this.gradeId = gradeId;
    }

    public BigDecimal getKcal() {
        return kcal;
    }

    public void setKcal(BigDecimal kcal) {
        this.kcal = kcal;
    }

    public BigDecimal getProtein() {
        return protein;
    }

    public void setProtein(BigDecimal protein) {
        this.protein = protein;
    }

    public BigDecimal getFat() {
        return fat;
    }

    public void setFat(BigDecimal fat) {
        this.fat = fat;
    }

    public BigDecimal getCarb() {
        return carb;
    }

    public void setCarb(BigDecimal carb) {
        this.carb = carb;
    }
}
