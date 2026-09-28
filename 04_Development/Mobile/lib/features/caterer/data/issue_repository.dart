import '../../../core/network/api_client.dart';
import 'issue_models.dart';

/// `inventory/issue-plan` + `inventory/issue-slips` + `menus` + `dishes` +
/// `student-distributions/planned-dishes` (MF06/MF09). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inventory/issue/page.tsx`.
class IssueRepository {
  IssueRepository._();
  static final IssueRepository instance = IssueRepository._();

  final _api = ApiClient.instance;

  /// Thực đơn PUBLISHED — lọc ngày hôm nay ở phía gọi (repository trả cả trang,
  /// cùng khuôn `MenuRepository` không có ở mobile nên đọc thẳng ở đây).
  Future<List<IssueMenu>> fetchMenus() async {
    final json = await _api.get('/menus', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    return items
        .map((e) => IssueMenu.fromJson(e as Map<String, dynamic>))
        .where((m) => m.status == 'PUBLISHED')
        .toList();
  }

  /// Thư viện món — cần `ingredients[]` của từng món để gộp nhu cầu nguyên liệu.
  Future<Map<String, IssueDish>> fetchDishesById() async {
    final json = await _api.get('/dishes', query: const {'pageSize': '500'});
    final items = (json?['items'] as List?) ?? const [];
    final map = <String, IssueDish>{};
    for (final e in items) {
      final d = IssueDish.fromJson(e as Map<String, dynamic>);
      map[d.id] = d;
    }
    return map;
  }

  /// `GET /student-distributions/planned-dishes?date=&mealType=` — KHÔNG truyền `class` để
  /// gộp TOÀN TRƯỜNG theo món (đúng số suất PENDING thật sẽ phát, kể cả món thay thế).
  Future<List<IssuePlannedDish>> fetchPlannedDishes({required String date, required String mealType}) async {
    final json = await _api.get('/student-distributions/planned-dishes', query: {'date': date, 'mealType': mealType});
    final byDish = (json?['byDish'] as List?) ?? const [];
    return byDish.map((e) => IssuePlannedDish.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// `GET /inventory/issue-plan?serviceDate=&mealType=&demand[id]=qty...` — FEFO server-side.
  Future<IssuePlan> fetchIssuePlan({
    required String date,
    required String mealType,
    required Map<String, double> demand,
  }) async {
    final query = <String, String>{
      'serviceDate': date,
      'mealType': mealType,
      for (final e in demand.entries) 'demand[${e.key}]': e.value.toString(),
    };
    final json = await _api.get('/inventory/issue-plan', query: query);
    return IssuePlan.fromJson(json ?? const {});
  }

  /// `POST /inventory/issue-slips` — MỘT phiếu xuất cho cả bữa (ADR-M06-6). Server tự chặn
  /// xuất trùng (ngày/bữa) và xuất cho ngày khác hôm nay — không cần tự kiểm trước ở client,
  /// lỗi trả về đã đủ rõ để hiển thị thẳng.
  Future<void> createIssueSlip({
    required String date,
    required String mealType,
    required List<String> menuIds,
    required List<Map<String, dynamic>> lines,
    String? note,
  }) async {
    await _api.post('/inventory/issue-slips', body: {
      'serviceDate': date,
      'mealType': mealType,
      'menuIds': menuIds,
      'lines': lines,
      if (note != null && note.trim().isNotEmpty) 'note': note.trim(),
    });
  }
}
