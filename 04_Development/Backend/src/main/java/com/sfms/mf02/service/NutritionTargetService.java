package com.sfms.mf02.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf02.dto.NutritionTargetDtos.NutritionTargetResponse;
import com.sfms.mf02.dto.NutritionTargetDtos.UpdateNutritionTargetRequest;

import java.util.List;
import java.util.UUID;

public interface NutritionTargetService {

    /** Mọi khối ĐANG DÙNG của trường, khối chưa cấu hình trả giá trị mặc định. */
    List<NutritionTargetResponse> list(AuthUser actor);

    NutritionTargetResponse update(UUID gradeId, UpdateNutritionTargetRequest request, AuthUser actor);

    /**
     * Chuẩn ÁP DỤNG cho một khối — có cấu hình thì trả cấu hình, không thì trả mặc
     * định. Dùng cho module khác (MF09 lịch sử dinh dưỡng) nên KHÔNG nhận
     * {@code AuthUser}: điểm vào bên đó là học sinh, phạm vi trường đã được kiểm ở
     * tầng gọi.
     */
    NutritionTargetResponse effectiveForGrade(UUID gradeId);
}
