package com.sfms.mf06.dto;

/** UC-? — xuất báo cáo lịch sử nhận hàng (tab "Lịch sử & báo cáo", trang receiving). */
public final class ReceivingExportDtos {

    private ReceivingExportDtos() {
    }

    public enum ExportFormat { XLSX, PDF }

    /** @param content bytes nhị phân thật (POI cho XLSX, PDFBox cho PDF — không phải CSV giả lập). */
    public record ExportedFile(String fileName, String contentType, byte[] content) {}
}
