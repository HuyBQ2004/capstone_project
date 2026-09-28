import '../../../core/network/api_client.dart';
import 'class_delivery_models.dart';

/// Đối chiếu `04_Development/Frontend/src/app/(app)/teacher/class-deliveries/page.tsx`
/// (UC-016 Phase 2 mục 1+4) — giáo viên xác nhận ĐÃ NHẬN đủ suất bếp giao cho
/// lớp mình TRƯỚC khi sang màn Phân phát bữa ăn chia từng món cho từng học
/// sinh.
class ClassDeliveryRepository {
  ClassDeliveryRepository._();
  static final ClassDeliveryRepository instance = ClassDeliveryRepository._();

  final _api = ApiClient.instance;

  /// `GET /class-meal-deliveries?date=&mealType=&class=` — trả MẢNG TRẦN
  /// (không phân trang, `usePlainList` bên web). Có tối đa 1 bản ghi cho mỗi
  /// (ngày, bữa, lớp).
  Future<ClassMealDelivery?> fetchDelivery({
    required String date,
    required String mealType,
    required String classroomId,
  }) async {
    final list = await _api.getList('/class-meal-deliveries', query: {
      'date': date,
      'mealType': mealType,
      'class': classroomId,
    });
    final items = list ?? const [];
    if (items.isEmpty) return null;
    return ClassMealDelivery.fromJson(items.first as Map<String, dynamic>);
  }

  /// `GET /student-distributions/planned-dishes?date=&class=&mealType=` —
  /// GET-only, xem trước suất dự kiến, dùng lại đúng luật của `serve()`.
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

  /// `POST /class-meal-deliveries/{code}/receive` — xác nhận đã nhận đủ suất.
  Future<ClassMealDelivery> confirmReceive(String id) async {
    final json = await _api.post('/class-meal-deliveries/$id/receive');
    return ClassMealDelivery.fromJson(json!);
  }

  /// Bữa "đang phát" = bữa lớp đã bấm "Đã nhận" GẦN NHẤT hôm nay. `GET
  /// /class-meal-deliveries` bắt buộc `mealType` (không có giá trị mặc định) —
  /// không gọi được "lấy cả 3 bữa" trong 1 lần, phải gọi riêng từng bữa rồi
  /// gộp. Đối chiếu `teacher/distribution/page.tsx` (sửa 10/08/2026, Nam):
  /// KHÔNG suy theo giờ trong ngày, KHÔNG cho chọn tay — bếp phát bữa nào thì
  /// giáo viên chỉ thấy đúng bữa đó.
  Future<ClassMealDelivery?> fetchReceivedToday({required String date, required String classroomId}) async {
    const mealTypes = ['BREAKFAST', 'LUNCH', 'SNACK'];
    final results = await Future.wait(
      mealTypes.map((m) => fetchDelivery(date: date, mealType: m, classroomId: classroomId)),
    );
    final received = results.whereType<ClassMealDelivery>().where((d) => d.isReceived).toList()
      ..sort((a, b) => (b.receivedAt ?? '').compareTo(a.receivedAt ?? ''));
    return received.isEmpty ? null : received.first;
  }
}
