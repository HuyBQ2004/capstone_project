import '../../../core/network/api_client.dart';
import '../../profile/data/profile_repository.dart';
import 'child_models.dart';

/// Đối chiếu `useParentChildren()` (`04_Development/Frontend/src/lib/stores/child-store.ts`):
/// `GET /parents/{code}/students` (`ParentController.studentsOf()`, UC-050) —
/// mã phụ huynh (`{code}`) chính là `SessionUser.id` của tài khoản đang đăng
/// nhập (`GET /profile/me`), KHÔNG phải id của con.
///
/// Cache trong bộ nhớ tiến trình: danh sách con không đổi trong một phiên làm
/// việc, tránh mỗi màn (Dashboard, Thực đơn, Lịch sử bữa ăn, Dị ứng, Báo sự cố)
/// đều tự gọi lại `/profile/me` + `/parents/{code}/students` từ đầu.
class ParentChildRepository {
  ParentChildRepository._();
  static final ParentChildRepository instance = ParentChildRepository._();

  final _api = ApiClient.instance;
  List<ParentChild>? _cached;

  /// Con đang được chọn ở bộ chọn con (Child Selector) — giữ lựa chọn xuyên
  /// suốt các màn Parent trong một phiên, đối chiếu `useChildStore` (bản web).
  String? activeChildId;

  Future<List<ParentChild>> fetchChildren({bool forceRefresh = false}) async {
    if (_cached != null && !forceRefresh) return _cached!;

    final profile = await ProfileRepository.instance.fetchMine();
    final json = await _api.get('/parents/${profile.id}/students', query: const {'pageSize': '50'});
    final items = (json?['items'] as List?) ?? const [];
    final children = items.map((e) => ParentChild.fromJson(e as Map<String, dynamic>)).toList();

    _cached = children;
    if (activeChildId == null || !children.any((c) => c.id == activeChildId)) {
      activeChildId = children.isEmpty ? null : children.first.id;
    }
    return children;
  }

  ParentChild? activeChildOf(List<ParentChild> children) {
    if (children.isEmpty) return null;
    return children.firstWhere((c) => c.id == activeChildId, orElse: () => children.first);
  }

  void setActiveChild(String studentId) => activeChildId = studentId;

  void clearCache() {
    _cached = null;
    activeChildId = null;
  }
}
