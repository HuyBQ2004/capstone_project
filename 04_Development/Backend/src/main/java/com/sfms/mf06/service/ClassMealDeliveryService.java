package com.sfms.mf06.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.dto.ClassMealDeliveryDtos.ClassMealDeliveryResponse;
import com.sfms.mf06.dto.ClassMealDeliveryDtos.CreateDeliveryRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * {@code class-meal-deliveries} — 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2
 * mục (1)+(4). Trạng thái giao/nhận suất ăn bếp→lớp theo (ngày, bữa, lớp).
 */
public interface ClassMealDeliveryService {

    /** GET — Teacher chỉ thấy lớp mình (BR-120); School Admin/Service Caterers thấy toàn trường. */
    List<ClassMealDeliveryResponse> list(LocalDate date, String mealType, String classroomCode, AuthUser actor);

    /** POST — Service Caterers xác nhận ĐÃ giao tới lớp (idempotent theo (ngày, bữa, lớp)). */
    ClassMealDeliveryResponse deliver(CreateDeliveryRequest request, AuthUser actor);

    /** POST /{code}/receive — Teacher (đúng lớp mình) xác nhận đã nhận đủ. */
    ClassMealDeliveryResponse receive(String code, AuthUser actor);
}
