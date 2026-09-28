import '../../../core/network/api_client.dart';
import 'meal_record_models.dart';

/// `GET /meal-records?dateFrom=&dateTo=` + `PATCH /meal-records/{code}`
/// (backend chỉ nhận `consumption`/`specialMealServed` — `recordedAt` server
/// tự set, BR-121). Đối chiếu bản web: KHÔNG truyền filter lớp — role TEACHER
/// tự bị giới hạn ở lớp chủ nhiệm phía server (cùng khuôn `incidents`).
class MealRecordRepository {
  MealRecordRepository._();
  static final MealRecordRepository instance = MealRecordRepository._();

  final _api = ApiClient.instance;

  Future<List<MealRecord>> fetchForDate(String isoDate) async {
    final json = await _api.get('/meal-records', query: {
      'dateFrom': isoDate,
      'dateTo': isoDate,
      'pageSize': '200',
    });
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => MealRecord.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<MealRecord> updateConsumption(String id, String consumption) async {
    final json = await _api.patch('/meal-records/$id', body: {'consumption': consumption});
    return MealRecord.fromJson(json!);
  }
}
