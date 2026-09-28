/// `AllergyDeclarationResponse` (backend, MF03 — UC-077, BR-031).
class AllergyDeclaration {
  final String id;
  final String? studentId;
  final String? category;
  final String? allergen;
  final String? severity;
  final String? status;
  final String? declaredAt;
  final String? proofFileId;

  AllergyDeclaration({
    required this.id,
    required this.studentId,
    required this.category,
    required this.allergen,
    required this.severity,
    required this.status,
    required this.declaredAt,
    required this.proofFileId,
  });

  factory AllergyDeclaration.fromJson(Map<String, dynamic> json) => AllergyDeclaration(
        id: json['id'] as String? ?? '',
        studentId: json['studentId'] as String?,
        category: json['category'] as String?,
        allergen: json['allergen'] as String?,
        severity: json['severity'] as String?,
        status: json['status'] as String?,
        declaredAt: json['declaredAt'] as String?,
        proofFileId: json['proofFileId'] as String?,
      );
}

/// `ChronicConditionResponse` (backend, MF03 — UC-077, BR-031).
class ChronicCondition {
  final String id;
  final String? studentId;
  final String? category;
  final String? conditionName;
  final String? severity;
  final String? status;
  final String? declaredAt;
  final String? notes;
  final String? proofFileId;

  ChronicCondition({
    required this.id,
    required this.studentId,
    required this.category,
    required this.conditionName,
    required this.severity,
    required this.status,
    required this.declaredAt,
    required this.notes,
    required this.proofFileId,
  });

  factory ChronicCondition.fromJson(Map<String, dynamic> json) => ChronicCondition(
        id: json['id'] as String? ?? '',
        studentId: json['studentId'] as String?,
        category: json['category'] as String?,
        conditionName: json['conditionName'] as String?,
        severity: json['severity'] as String?,
        status: json['status'] as String?,
        declaredAt: json['declaredAt'] as String?,
        notes: json['notes'] as String?,
        proofFileId: json['proofFileId'] as String?,
      );
}

/// `SpecialMealRequestResponse` (backend, MF03 — UC-078, BR-065/BR-066/BR-220).
class SpecialMealRequest {
  final String id;
  final String? student;
  final String? classroomId;
  final String? dietaryType;
  final String? duration;
  final String? justification;
  final String? requestedBy;
  final String? date;
  final String? status;
  final bool hasDocument;

  SpecialMealRequest({
    required this.id,
    required this.student,
    required this.classroomId,
    required this.dietaryType,
    required this.duration,
    required this.justification,
    required this.requestedBy,
    required this.date,
    required this.status,
    required this.hasDocument,
  });

  factory SpecialMealRequest.fromJson(Map<String, dynamic> json) => SpecialMealRequest(
        id: json['id'] as String? ?? '',
        student: json['student'] as String?,
        classroomId: json['class'] as String?,
        dietaryType: json['dietaryType'] as String?,
        duration: json['duration'] as String?,
        justification: json['justification'] as String?,
        requestedBy: json['requestedBy'] as String?,
        date: json['date'] as String?,
        status: json['status'] as String?,
        hasDocument: json['hasDocument'] as bool? ?? false,
      );
}

/// Đối chiếu `STANDARD_DIETS` (`04_Development/Frontend/src/lib/mock-data.ts`, BR-066).
const standardDiets = ['Halal', 'Vegetarian', 'Gluten-Free', 'Nut-Free', 'Lactose-Free'];

String severityLabelVi(String? value) {
  switch (value) {
    case 'MILD':
      return 'Nhẹ';
    case 'MODERATE':
      return 'Trung bình';
    case 'SEVERE':
      return 'Nặng';
    default:
      return value ?? '—';
  }
}

String reviewStatusLabel(String? status) {
  switch (status) {
    case 'PENDING':
      return 'Chờ xử lý';
    case 'VERIFIED':
      return 'Đã xác minh';
    case 'NEEDS_INFO':
      return 'Cần bổ sung';
    case 'APPROVED':
      return 'Đã duyệt';
    case 'REJECTED':
      return 'Từ chối';
    default:
      return status ?? '—';
  }
}
