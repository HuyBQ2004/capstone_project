/// `StudentResponse` (backend, MF03) chiếu qua góc nhìn "con của phụ huynh" —
/// đối chiếu `ParentChild` (`04_Development/Frontend/src/lib/mock-data.ts`) và
/// `parentChildOfStudent()` (`04_Development/Frontend/src/lib/student-adapter.ts`).
/// Chỉ giữ field 4 màn Parent (Dashboard, Thực đơn, Lịch sử bữa ăn, Dị ứng) cần.
class ParentChild {
  final String id;
  final String? name;
  final String? classroomId;
  final List<String> allergies;
  final String? allergySeverity;
  final String? specialDiet;
  final bool hasAllergy;
  final String? school;

  ParentChild({
    required this.id,
    required this.name,
    required this.classroomId,
    required this.allergies,
    required this.allergySeverity,
    required this.specialDiet,
    required this.hasAllergy,
    required this.school,
  });

  factory ParentChild.fromJson(Map<String, dynamic> json) => ParentChild(
        id: json['id'] as String,
        name: json['name'] as String?,
        classroomId: json['class'] as String? ?? json['classroomId'] as String?,
        allergies: (json['allergies'] as List?)?.cast<String>() ?? const [],
        allergySeverity: json['allergySeverity'] as String?,
        specialDiet: json['specialDiet'] as String?,
        hasAllergy: json['hasAllergy'] == 'yes',
        school: json['school'] as String?,
      );

  /// Đối chiếu `gradeOfClass()` (bản web, `lib/mock-data.ts`): thực đơn quản lý
  /// theo khối ("Khối 1".."Khối 5"), suy ra từ chữ số đầu của tên lớp.
  String get grade {
    final className = classroomId;
    if (className == null || className.isEmpty) return 'Khối ?';
    final digit = RegExp(r'^\d').firstMatch(className)?.group(0);
    return digit == null ? 'Khối ?' : 'Khối $digit';
  }
}
