import '../../../core/network/api_client.dart';
import '../../profile/data/profile_repository.dart';
import 'classroom_models.dart';

/// Đối chiếu `useTeacherClass()` (`lib/stores/auth-store.ts`, bản web): `GET
/// /classrooms` (`ClassroomController` cho phép role TEACHER gọi) trả cả
/// trường, tự tìm bản ghi có `teacherId` khớp id giáo viên đang đăng nhập
/// (`GET /profile/me`) — backend không lọc sẵn theo lớp chủ nhiệm ở endpoint
/// này.
///
/// Cache trong bộ nhớ tiến trình: lớp chủ nhiệm không đổi trong một phiên làm
/// việc, tránh mỗi màn (Dashboard, Class Deliveries, Distribution, Meal
/// Records, Incidents) đều tự gọi `/classrooms` + `/profile/me` lại từ đầu.
class ClassroomRepository {
  ClassroomRepository._();
  static final ClassroomRepository instance = ClassroomRepository._();

  final _api = ApiClient.instance;
  Classroom? _cached;

  Future<Classroom?> resolveMyClassroom({bool forceRefresh = false}) async {
    if (_cached != null && !forceRefresh) return _cached;

    final profile = await ProfileRepository.instance.fetchMine();
    final json = await _api.get('/classrooms', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    final classrooms = items.map((e) => Classroom.fromJson(e as Map<String, dynamic>)).toList();

    Classroom? mine;
    for (final c in classrooms) {
      if (c.teacherId == profile.id) {
        mine = c;
        break;
      }
    }
    _cached = mine;
    return mine;
  }

  void clearCache() => _cached = null;
}
