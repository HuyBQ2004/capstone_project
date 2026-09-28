package com.sfms.mf12.service;

import com.sfms.mf12.dto.SystemDtos.BackupsResponse;
import com.sfms.mf12.dto.SystemDtos.SystemStatusResponse;

/** UC-149 + BR-160/161 — ADR-M12-04: KHÔNG bảng metric riêng, đọc trực tiếp trạng thái đã có. */
public interface SystemStatusService {

    /** UC-149 — trạng thái 4 dịch vụ ngoài (đọc {@code external_integrations}, L14/CF-10:
     *  một nguồn duy nhất, KHÔNG tự sinh trạng thái Gemini riêng). */
    SystemStatusResponse healthChecks();

    /** BR-160/161 — chưa có hạ tầng backup job trong code đã merge; trả trạng thái rỗng trung thực. */
    BackupsResponse backups();
}
