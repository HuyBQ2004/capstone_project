package com.sfms.mf08.dto;

/** UC-118 — xuất sổ Mẫu số 5 (12 cột, Phụ lục 2 QĐ 1246/QĐ-BYT). */
public final class ExportDtos {

    private ExportDtos() {
    }

    public enum ExportFormat { XLSX, PDF }

    /** @param content bytes nhị phân thật (POI cho XLSX, PDFBox cho PDF — không phải CSV giả lập). */
    public record ExportedFile(String fileName, String contentType, byte[] content) {}
}
