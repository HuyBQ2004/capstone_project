import '../../../core/network/api_client.dart';
import 'meal_history_models.dart';

/// `GET /students/{code}/meal-history` — đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/meal-history/page.tsx`.
/// Server chỉ lọc `dateFrom`/`dateTo`; lọc `consumption` làm ở client (đúng
/// hành vi bản web).
class MealHistoryRepository {
  MealHistoryRepository._();
  static final MealHistoryRepository instance = MealHistoryRepository._();

  final _api = ApiClient.instance;

  Future<List<MealHistoryEntry>> fetchForStudent(
    String studentId, {
    String? dateFrom,
    String? dateTo,
  }) async {
    final query = <String, String>{'pageSize': '200'};
    if (dateFrom != null && dateFrom.isNotEmpty) query['dateFrom'] = dateFrom;
    if (dateTo != null && dateTo.isNotEmpty) query['dateTo'] = dateTo;

    final json = await _api.get('/students/$studentId/meal-history', query: query);
    final items = (json?['items'] as List?) ?? const [];
    final entries = items.map((e) => MealHistoryEntry.fromJson(e as Map<String, dynamic>)).toList()
      ..sort((a, b) => (b.date ?? '').compareTo(a.date ?? ''));
    return entries;
  }
}
