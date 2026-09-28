import '../../../core/network/api_client.dart';
import 'class_delivery_models.dart';

/// `class-meal-deliveries` (backend MF06 — bếp giao suất tới lớp). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/class-deliveries/page.tsx`.
class ClassDeliveryRepository {
  ClassDeliveryRepository._();
  static final ClassDeliveryRepository instance = ClassDeliveryRepository._();

  final _api = ApiClient.instance;

  /// `GET /class-meal-deliveries?date=&mealType=` — KHÔNG truyền `class` thì
  /// trả TOÀN TRƯỜNG (đúng quyền SERVICE_CATERERS, khác Teacher chỉ thấy lớp
  /// mình). Trả mảng trần (`usePlainList` bên web).
  Future<List<ClassMealDelivery>> fetchAll({required String date, required String mealType}) async {
    final list = await _api.getList('/class-meal-deliveries', query: {'date': date, 'mealType': mealType});
    return (list ?? const []).map((e) => ClassMealDelivery.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// `GET /student-distributions/planned-dishes?date=&class=&mealType=`.
  Future<PlannedDishes> fetchPlannedDishes({
    required String date,
    required String mealType,
    required String classroomId,
  }) async {
    final json = await _api.get('/student-distributions/planned-dishes', query: {
      'date': date,
      'class': classroomId,
      'mealType': mealType,
    });
    return PlannedDishes.fromJson(json ?? const {});
  }

  /// `POST /class-meal-deliveries` — idempotent theo (ngày, bữa, lớp), 409 nếu
  /// đã có bản ghi.
  Future<ClassMealDelivery> deliver({
    required String date,
    required String mealType,
    required String classroomId,
    String? note,
  }) async {
    final json = await _api.post('/class-meal-deliveries', body: {
      'date': date,
      'mealType': mealType,
      'classroomId': classroomId,
      if (note != null && note.trim().isNotEmpty) 'note': note.trim(),
    });
    return ClassMealDelivery.fromJson(json!);
  }
}
