import '../../../core/network/api_client.dart';
import 'classroom_models.dart';

/// `GET /classrooms` — `ClassroomController` cho phép SERVICE_CATERERS gọi
/// (đối chiếu backend `@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN',
/// 'SERVICE_CATERERS','MEDICAL_STAFF','TEACHER')")`). Khác Teacher (chỉ lọc ra
/// đúng lớp chủ nhiệm), Đơn vị Suất ăn cần TOÀN BỘ lớp của trường để giao suất
/// từng lớp (Class Deliveries) — không lọc theo `teacherId`.
class ClassroomRepository {
  ClassroomRepository._();
  static final ClassroomRepository instance = ClassroomRepository._();

  final _api = ApiClient.instance;
  List<Classroom>? _cached;

  Future<List<Classroom>> fetchAll({bool forceRefresh = false}) async {
    if (_cached != null && !forceRefresh) return _cached!;
    final json = await _api.get('/classrooms', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    final classrooms = items.map((e) => Classroom.fromJson(e as Map<String, dynamic>)).toList()
      ..sort((a, b) => (a.name ?? a.id).compareTo(b.name ?? b.id));
    _cached = classrooms;
    return classrooms;
  }
}
