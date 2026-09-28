package com.sfms.mf06.service;

import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.dto.ReceivingExportDtos.ExportFormat;
import com.sfms.mf06.dto.ReceivingExportDtos.ExportedFile;

public interface ReceivingReportExportService {

    ExportedFile export(ListQuery query, ExportFormat format, AuthUser actor);
}
