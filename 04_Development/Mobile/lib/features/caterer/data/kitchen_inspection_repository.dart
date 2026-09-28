import '../../../core/network/api_client.dart';
import 'kitchen_inspection_models.dart';

/// `kitchen-inspections` (backend MF07 — BR-221, xác nhận hai chiều biên bản
/// kiểm tra bếp). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/kitchen-inspections/`.
class KitchenInspectionRepository {
  KitchenInspectionRepository._();
  static final KitchenInspectionRepository instance = KitchenInspectionRepository._();

  final _api = ApiClient.instance;

  Future<List<KitchenInspection>> fetchHistory() async {
    final json = await _api.get('/kitchen-inspections', query: const {'pageSize': '50'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => KitchenInspection.fromJson(e as Map<String, dynamic>)).toList()
      ..sort((a, b) => (b.date ?? '').compareTo(a.date ?? ''));
  }

  Future<KitchenInspection> fetchDetail(String code) async {
    final json = await _api.get('/kitchen-inspections/$code');
    return KitchenInspection.fromJson(json!);
  }

  /// `POST /kitchen-inspections/{code}/confirm` (SERVICE_CATERERS only).
  Future<KitchenInspection> confirm(String code) async {
    final json = await _api.post('/kitchen-inspections/$code/confirm');
    return KitchenInspection.fromJson(json!);
  }
}
