import '../../../core/network/api_client.dart';
import 'student_models.dart';

/// `GET /students` — đối chiếu bản web (`usePagedList<StudentDto>("students",
/// { pageSize: 300 })`): backend KHÔNG lọc theo lớp ở endpoint này, phải tự
/// lọc `s.class == classroomId` phía client sau khi tải.
class StudentRepository {
  StudentRepository._();
  static final StudentRepository instance = StudentRepository._();

  final _api = ApiClient.instance;

  Future<List<Student>> fetchClassStudents(String classroomId) async {
    final json = await _api.get('/students', query: const {'pageSize': '300'});
    final items = (json?['items'] as List?) ?? const [];
    return items
        .map((e) => Student.fromJson(e as Map<String, dynamic>))
        .where((s) => s.classroomId == classroomId)
        .toList();
  }
}
