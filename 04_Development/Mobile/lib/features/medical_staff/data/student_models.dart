/// `StudentResponse` (backend, MF03) — rút gọn cho các màn Medical Staff cần
/// (tra tên/lớp, đếm khai báo sức khỏe chờ duyệt, chọn học sinh ảnh hưởng khi
/// mở phiên điều tra). Đối chiếu `04_Development/Frontend/src/lib/api/types.ts`
/// (`StudentDto`).
class MedicalStudent {
  final String id;
  final String? name;
  final String? classroomId;
  final String? healthStatus;
  final List<String> allergies;

  MedicalStudent({
    required this.id,
    required this.name,
    required this.classroomId,
    required this.healthStatus,
    required this.allergies,
  });

  factory MedicalStudent.fromJson(Map<String, dynamic> json) => MedicalStudent(
        id: json['id'] as String,
        name: json['name'] as String?,
        classroomId: json['class'] as String?,
        healthStatus: json['healthStatus'] as String?,
        allergies: (json['allergies'] as List?)?.cast<String>() ?? const [],
      );
}
