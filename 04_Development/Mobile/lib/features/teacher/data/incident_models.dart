/// `SourceReportResponse` (backend, MF11) — báo cáo THÔ do giáo viên tạo,
/// chờ Y tế trường tiếp nhận/gộp thành sự cố thật (`incidents`). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/incidents/page.tsx`.
class SourceReport {
  final String id;
  final String? className;
  final String? description;
  final String? status;
  final String? reportedAt;
  final String? reportedByLabel;
  final String? mealDate;
  final String? suspectedMealText;
  final List<String> symptoms;
  final int evidencePhotos;

  SourceReport({
    required this.id,
    required this.className,
    required this.description,
    required this.status,
    required this.reportedAt,
    required this.reportedByLabel,
    required this.mealDate,
    required this.suspectedMealText,
    required this.symptoms,
    required this.evidencePhotos,
  });

  factory SourceReport.fromJson(Map<String, dynamic> json) => SourceReport(
        id: json['id'] as String,
        className: json['className'] as String?,
        description: json['description'] as String?,
        status: json['status'] as String?,
        reportedAt: json['reportedAt'] as String?,
        reportedByLabel: json['reportedByLabel'] as String?,
        mealDate: json['mealDate'] as String?,
        suspectedMealText: json['suspectedMealText'] as String?,
        symptoms: (json['symptoms'] as List?)?.cast<String>() ?? const [],
        evidencePhotos: json['evidencePhotos'] as int? ?? 0,
      );
}

String sourceReportStatusLabel(String? status) {
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
