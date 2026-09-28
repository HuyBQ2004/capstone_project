/// `StudentResponse` (backend, MF03) — chỉ giữ field các màn Teacher cần
/// (danh sách lớp, cảnh báo dị ứng, chế độ ăn riêng). Đối chiếu
/// `04_Development/Frontend/src/lib/api/types.ts` (`StudentDto`).
class Student {
  final String id;
  final String? name;
  final String? classroomId;
  final List<String> allergies;
  final String? allergySeverity;
  final String? specialDiet;
  final bool hasAllergy;
  final bool hasSpecialDiet;

  Student({
    required this.id,
    required this.name,
    required this.classroomId,
    required this.allergies,
    required this.allergySeverity,
    required this.specialDiet,
    required this.hasAllergy,
    required this.hasSpecialDiet,
  });

  factory Student.fromJson(Map<String, dynamic> json) => Student(
        id: json['id'] as String,
        name: json['name'] as String?,
        classroomId: json['class'] as String?,
        allergies: (json['allergies'] as List?)?.cast<String>() ?? const [],
        allergySeverity: json['allergySeverity'] as String?,
        specialDiet: json['specialDiet'] as String?,
        hasAllergy: json['hasAllergy'] == 'yes',
        hasSpecialDiet: json['hasSpecialDiet'] == 'yes',
      );
}
