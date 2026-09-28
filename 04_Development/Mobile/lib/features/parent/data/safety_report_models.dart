/// `SourceReportResponse` (backend, MF11) — báo cáo thô do phụ huynh gửi khi
/// nghi ngờ mất an toàn thực phẩm, đi thẳng tới Y tế trường & Ban giám hiệu,
/// không qua phê duyệt (BR-138). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/safety-concern/page.tsx`
/// (UC-073) — chỉ giữ field màn Parent cần (không đủ field lâm sàng như bản
/// Y tế trường).
class SafetyReport {
  final String id;
  final String? studentCode;
  final String? className;
  final String? description;
  final String? suspectedMealText;
  final String? status;
  final String? reportedAt;
  final int evidencePhotos;

  SafetyReport({
    required this.id,
    required this.studentCode,
    required this.className,
    required this.description,
    required this.suspectedMealText,
    required this.status,
    required this.reportedAt,
    required this.evidencePhotos,
  });

  factory SafetyReport.fromJson(Map<String, dynamic> json) => SafetyReport(
        id: json['id'] as String,
        studentCode: json['studentCode'] as String?,
        className: json['className'] as String?,
        description: json['description'] as String?,
        suspectedMealText: json['suspectedMealText'] as String?,
        status: json['status'] as String?,
        reportedAt: json['reportedAt'] as String?,
        evidencePhotos: json['evidencePhotos'] as int? ?? 0,
      );
}

/// Đối chiếu `CONCERN_CATEGORIES` (`04_Development/Frontend/src/lib/mock-data.ts`).
const concernCategories = [
  'Nghi ngờ ngộ độc thực phẩm',
  'Dị vật trong thức ăn',
  'Thức ăn có mùi vị/màu sắc bất thường',
  'Phản ứng dị ứng sau bữa ăn',
  'Vệ sinh dụng cụ/khu vực ăn',
  'Khác',
];

String safetyReportStatusLabel(String? status) {
  switch (status) {
    case 'PENDING_TRIAGE':
      return 'Chờ tiếp nhận';
    case 'MERGED':
      return 'Đã gộp vào phiên điều tra';
    case 'CLOSED_NOT_FOOD_SAFETY':
      return 'Đã đóng — Không liên quan ATTP';
    default:
      return status ?? '—';
  }
}
