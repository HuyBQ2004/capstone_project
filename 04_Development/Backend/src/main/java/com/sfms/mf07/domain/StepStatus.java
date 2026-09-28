package com.sfms.mf07.domain;

/**
 * CF-23: {@code overall} khởi tạo {@code IN_PROGRESS} ngay lúc mở phiên, giữ nguyên tới khi
 * Bước 3 ĐẠT. {@code step1}/{@code step2}/{@code step3} chỉ nhận {@code PENDING|PASSED|FAILED}
 * — DB constraint (V12) không cho 3 cột đó nhận {@code IN_PROGRESS}.
 */
public enum StepStatus {
    PENDING, IN_PROGRESS, PASSED, FAILED
}
