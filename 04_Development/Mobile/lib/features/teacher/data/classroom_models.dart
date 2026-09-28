/// `ClassroomResponse` (backend) — đối chiếu `Classroom` (`lib/mock-data.ts`,
/// bản web) và `useTeacherClass()` (`lib/stores/auth-store.ts`): lớp chủ nhiệm
/// của giáo viên đang đăng nhập, nối theo `teacherId` (khoá ổn định), KHÔNG so
/// tên hiển thị.
class Classroom {
  final String id;
  final String? name;
  final String? teacher;
  final String? teacherId;
  final int? capacity;
  // 13/08/2026 (Nam) — V44 (06/08/2026, backend) đổi `grade` từ int (số khối)
  // sang String (nhãn hiển thị, VD "Khối 3", `ClassroomDtos.java:57`). Mobile
  // vẫn ép kiểu `int?` cũ ⇒ TypeError NGAY LÚC PARSE mọi lần tải `/classrooms`
  // có khối — vỡ toàn bộ `resolveMyClassroom()`, kéo sập luôn Dashboard, Class
  // Deliveries, Meal Distribution, Incident Report (mọi màn Teacher gọi tới nó).
  // Lỗi không phải ApiException nên chỉ hiện được chữ chung chung "Không tải
  // được dữ liệu" — đúng triệu chứng người dùng báo, không có thông tin gì hơn.
  final String? grade;
  final int? students;
  final int? allergies;

  Classroom({
    required this.id,
    required this.name,
    required this.teacher,
    required this.teacherId,
    required this.capacity,
    required this.grade,
    required this.students,
    required this.allergies,
  });

  factory Classroom.fromJson(Map<String, dynamic> json) => Classroom(
        id: json['id'] as String,
        name: json['name'] as String?,
        teacher: json['teacher'] as String?,
        teacherId: json['teacherId'] as String?,
        capacity: json['capacity'] as int?,
        grade: json['grade'] as String?,
        students: json['students'] as int?,
        allergies: json['allergies'] as int?,
      );
}
