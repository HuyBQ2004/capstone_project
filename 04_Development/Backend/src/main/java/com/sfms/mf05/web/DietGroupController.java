package com.sfms.mf05.web;

import com.sfms.mf05.dto.DietGroupDtos.DietGroupResponse;
import com.sfms.mf05.service.DietGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MF05 — {@code /api/v1/diet-groups} (BR-065) — danh mục nhóm chế độ ăn đặc biệt TOÀN HỆ THỐNG,
 * chỉ đọc. Nguồn sự thật cho mọi màn chọn nhóm chế độ ăn (Thực đơn mẫu, Lịch thực đơn tuần) —
 * KHÔNG dùng danh mục khai tay ở FE (đó là dữ liệu KHÔNG do Y tế duyệt).
 */
@Tag(name = "MF05 — Menu & Diet Management")
@RestController
@RequestMapping("/api/v1/diet-groups")
public class DietGroupController {

    private final DietGroupService dietGroupService;

    public DietGroupController(DietGroupService dietGroupService) {
        this.dietGroupService = dietGroupService;
    }

    /*
     * TEACHER + PARENT bổ sung 07/08/2026. Trước đó 5 màn của đúng hai vai này gọi
     * `useDietGroups()` và nhận 403 IM LẶNG (parent/dashboard, parent/special-meals,
     * parent/weekly-menu, teacher/special-meals, teacher/students/[id]) — triệu chứng thấy
     * được: cột "Ghi chú chế độ ăn" của giáo viên LUÔN hiện "—", và thực đơn của phụ huynh
     * mất phần cá nhân hoá theo nhóm chế độ ăn.
     *
     * Mở đọc là an toàn: đây là danh mục THAM CHIẾU toàn hệ thống, không school-scoped,
     * không chứa PII — không tăng bề mặt rủi ro. Ghi vẫn không có đường nào (chỉ mỗi GET).
     */
    @Operation(summary = "Danh mục nhóm chế độ ăn đặc biệt", description = "BR-065 — toàn hệ thống, "
            + "cấu hình qua DB (đội vận hành/y tế), không school-scoped")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF',"
            + "'TEACHER','PARENT')")
    @GetMapping
    public List<DietGroupResponse> list() {
        return dietGroupService.list();
    }
}
