import '../../../core/network/api_client.dart';
import 'distribution_models.dart';

/// `StudentDistributionController` (backend, MF09) — KHÔNG có PUT/PATCH, chỉ
/// GET (phân trang) + 4 action POST (`/serve`, `/confirm`, `/revoke`,
/// `/error-report`). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/distribution/page.tsx` và
/// `.../distribution/[id]/page.tsx`.
class DistributionRepository {
  DistributionRepository._();
  static final DistributionRepository instance = DistributionRepository._();

  final _api = ApiClient.instance;

  Future<List<Distribution>> fetchForClassDay({
    required String date,
    required String classroomId,
    String? mealType,
    String? status,
    int pageSize = 200,
  }) async {
    final query = <String, String>{
      'date': date,
      'class': classroomId,
      'pageSize': '$pageSize',
    };
    if (mealType != null) query['mealType'] = mealType;
    if (status != null) query['status'] = status;
    final json = await _api.get('/student-distributions', query: query);
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => Distribution.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// `POST /student-distributions/confirm` — 1 transaction cho N học sinh
  /// PENDING đã chọn, server tự tính `servedDishes` (ADR-M09-1). Trả THẲNG
  /// mảng `DistributionResponse[]`, không bọc `PagedResponse`.
  Future<List<Distribution>> confirmBatch(List<String> distributionIds) async {
    final list = await _api.postList('/student-distributions/confirm', body: {
      'distributionIds': distributionIds,
    });
    return (list ?? const []).map((e) => Distribution.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// `POST /student-distributions/{code}/revoke` — chỉ trong ngày
  /// (ADR-M09-3).
  Future<Distribution> revoke(String id) async {
    final json = await _api.post('/student-distributions/$id/revoke');
    return Distribution.fromJson(json!);
  }

  /// `POST /student-distributions/{code}/error-report` (UC-016 AF1) — báo lỗi
  /// phân phát sai suất, nhà bếp được thông báo cấp lại (BR-065).
  Future<Distribution> reportError(String id, String reason) async {
    final json = await _api.post('/student-distributions/$id/error-report', body: {'reason': reason});
    return Distribution.fromJson(json!);
  }
}
