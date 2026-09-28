/// `AllergyDeclarationResponse` (backend, MF03) — đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/allergies/page.tsx` (UC-084,
/// BR-031). Trạng thái chỉ có hiệu lực (cảnh báo bếp) sau khi `status ==
/// "VERIFIED"` bởi Y tế trường — phụ huynh KHÔNG tự sửa/xoá được khai báo đã gửi.
class AllergyDeclaration {
  final String id;
  final String? studentId;
  final String? category;
  final String? allergen;
  final String? severity;
  final String? status;
  final String? declaredAt;
  final String? reviewNote;

  AllergyDeclaration({
    required this.id,
    required this.studentId,
    required this.category,
    required this.allergen,
    required this.severity,
    required this.status,
    required this.declaredAt,
    required this.reviewNote,
  });

  factory AllergyDeclaration.fromJson(Map<String, dynamic> json) => AllergyDeclaration(
        id: json['id'] as String,
        studentId: json['studentId'] as String?,
        category: json['category'] as String?,
        allergen: json['allergen'] as String?,
        severity: json['severity'] as String?,
        status: json['status'] as String?,
        declaredAt: json['declaredAt'] as String?,
        reviewNote: json['reviewNote'] as String?,
      );
}

/// Đối chiếu `ALLERGEN_CATEGORIES` (`04_Development/Frontend/src/lib/mock-data.ts`).
const allergenCategories = [
  'Hải sản (tôm, cua, sò, ốc)',
  'Cá',
  'Đậu phộng & các loại hạt',
  'Sữa & chế phẩm từ sữa',
  'Trứng',
  'Đậu nành',
  'Lúa mì (gluten)',
  'Khác',
];

/// Đối chiếu `AllergySeverity` (D6, backend enum) và `SEVERITY_LABEL_VI`
/// (`04_Development/Frontend/src/lib/health-severity.ts`).
const severityOptions = ['MILD', 'MODERATE', 'SEVERE'];

String severityLabel(String? value) {
  switch (value) {
    case 'MILD':
      return 'Nhẹ';
    case 'MODERATE':
      return 'Trung bình';
    case 'SEVERE':
      return 'Nặng (sốc phản vệ)';
    default:
      return value ?? '—';
  }
}

String allergyStatusLabel(String? status) {
  switch (status) {
    case 'PENDING':
      return 'Chờ xử lý';
    case 'VERIFIED':
      return 'Đã xác minh';
    case 'NEEDS_INFO':
      return 'Cần bổ sung';
    case 'REJECTED':
      return 'Từ chối';
    default:
      return status ?? '—';
  }
}
