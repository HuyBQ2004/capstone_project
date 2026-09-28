package com.sfms.mf08.service;

import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.dto.ExportDtos.ExportFormat;
import com.sfms.mf08.dto.ExportDtos.ExportedFile;

/** @version 1.0 — UC-118, xuất sổ Mẫu số 5 (12 cột, Phụ lục 2 QĐ 1246/QĐ-BYT). */
public interface SampleRegisterExportService {

    ExportedFile export(ListQuery query, ExportFormat format, AuthUser actor);
}
