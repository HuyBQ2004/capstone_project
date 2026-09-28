import '../../../core/network/api_client.dart';
import 'adjustment_models.dart';

/// `inventory` + `inventory-transactions/adjustments` (MF06, BR-204). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inventory/page.tsx`.
class AdjustmentRepository {
  AdjustmentRepository._();
  static final AdjustmentRepository instance = AdjustmentRepository._();

  final _api = ApiClient.instance;

  /// BR-205: lô hết hạn/cách ly không điều chỉnh xuất dùng — chỉ trả lô thao tác được.
  Future<List<AdjustableBatch>> fetchAdjustableBatches() async {
    final json = await _api.get('/inventory', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    return items
        .map((e) => AdjustableBatch.fromJson(e as Map<String, dynamic>))
        .where((b) => !b.quarantined)
        .toList();
  }

  /// `POST /inventory-transactions/adjustments` — bắt buộc `reasonCode` + `managerSignature`
  /// (BR-204). `serviceDate`/`mealType` tuỳ chọn (14/08/2026) — gắn điều chỉnh (thường là
  /// EXTRA_USAGE) vào đúng bữa để kiểm thực Bước 1 auto-fill và "Phạm vi bữa ăn" nhận diện
  /// được, giống hệt giao dịch xuất kho FEFO.
  Future<void> createAdjustment({
    required String batchId,
    required double delta,
    required String unit,
    required String reasonCode,
    required String managerSignature,
    String? serviceDate,
    String? mealType,
  }) async {
    await _api.post('/inventory-transactions/adjustments', body: {
      'batchId': batchId,
      'delta': delta,
      'unit': unit,
      'reasonCode': reasonCode,
      'managerSignature': managerSignature,
      if (serviceDate != null) 'serviceDate': serviceDate,
      if (mealType != null) 'mealType': mealType,
    });
  }
}
