package com.sfms.mf12.service;

import com.sfms.mf12.domain.SentReport;

/** BR-178 — xuất PDF/XLSX THẬT (PDFBox/POI) từ snapshot {@link SentReport#getPayload()}. */
public interface ReportExporter {

    record ExportedFile(String filename, String contentType, byte[] bytes) {}

    /** BR-178 — {@code schoolName} là 1 trong 4 thành phần bắt buộc, KHÔNG suy được từ payload. */
    ExportedFile renderPdf(SentReport report, String schoolName);

    ExportedFile renderXlsx(SentReport report, String schoolName);
}
