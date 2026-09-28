/// `ClassroomResponse` (backend, MF02) — rút gọn, chỉ phần Đơn vị Suất ăn cần
/// để chọn lớp khi giao suất (Class Deliveries). Đối chiếu
/// `04_Development/Mobile/lib/features/teacher/data/classroom_models.dart`
/// (không import chéo feature — mỗi role giữ bản DTO riêng, cùng quy ước với
/// Medical Staff/Parent).
class Classroom {
  final String id;
  final String? name;
  // 13/08/2026 (Nam) — V44 (backend) đổi `grade` từ int (số khối) sang String
  // (nhãn hiển thị, VD "Khối 3") — mobile ép kiểu `int?` cũ gây TypeError ngay
  // lúc parse `/classrooms`, vỡ toàn bộ màn Giao suất tới lớp phía Caterer.
  final String? grade;
  final int? students;

  Classroom({required this.id, required this.name, required this.grade, required this.students});

  factory Classroom.fromJson(Map<String, dynamic> json) => Classroom(
        id: json['id'] as String,
        name: json['name'] as String?,
        grade: json['grade'] as String?,
        students: json['students'] as int?,
      );
}
