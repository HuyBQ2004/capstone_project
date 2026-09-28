import '../../../core/network/api_client.dart';
import 'inspection_models.dart';

/// `inspections` (backend MF07 — UC-052/UC-065/UC-124). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inspections/`.
class InspectionRepository {
  InspectionRepository._();
  static final InspectionRepository instance = InspectionRepository._();

  final _api = ApiClient.instance;

  Future<List<InspectionSession>> fetchList({String? dateFrom, String? dateTo}) async {
    final json = await _api.get('/inspections', query: {
      'pageSize': '100',
      if (dateFrom != null) 'dateFrom': dateFrom,
      if (dateTo != null) 'dateTo': dateTo,
    });
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => InspectionSession.fromJson(e as Map<String, dynamic>)).toList()
      ..sort((a, b) => (b.date ?? '').compareTo(a.date ?? ''));
  }

  Future<InspectionSession> fetchDetail(String code) async {
    final json = await _api.get('/inspections/$code');
    return InspectionSession.fromJson(json!);
  }

  /// `POST /inspections` — BR-079: mở phiên cho HÔM NAY, mỗi bữa 1 phiên gốc
  /// còn hiệu lực (409 nếu trùng, server tự kiểm).
  Future<InspectionSession> open({required String date, required String mealType}) async {
    final json = await _api.post('/inspections', body: {'date': date, 'mealType': mealType});
    return InspectionSession.fromJson(json!);
  }

  /// `POST /inspections/{code}/steps/{step}` — chốt luôn (không có lưu nháp
  /// phía server). `result` là "PASSED"|"FAILED".
  Future<InspectionSession> submitStep(
    String code,
    int step, {
    required List<Step1FreshRow> step1Fresh,
    required List<Step1DryRow> step1Dry,
    required List<Step2Row> step2Rows,
    required List<Step3Row> step3Rows,
    required String result,
    double? coreTemp,
    double? holdingTemp,
    String? failReason,
    String? failSeverity,
    String? failWitness,
    double? gpsLat,
    double? gpsLng,
  }) async {
    final json = await _api.post('/inspections/$code/steps/$step', body: {
      'step1Fresh': step1Fresh.map((r) => r.toJson()).toList(),
      'step1Dry': step1Dry.map((r) => r.toJson()).toList(),
      'step2Rows': step2Rows.map((r) => r.toJson()).toList(),
      'step3Rows': step3Rows.map((r) => r.toJson()).toList(),
      'result': result,
      if (coreTemp != null) 'coreTemp': coreTemp,
      if (holdingTemp != null) 'holdingTemp': holdingTemp,
      if (failReason != null) 'failReason': failReason,
      if (failSeverity != null) 'failSeverity': failSeverity,
      if (failWitness != null) 'failWitness': failWitness,
      if (gpsLat != null) 'gpsLat': gpsLat,
      if (gpsLng != null) 'gpsLng': gpsLng,
    });
    return InspectionSession.fromJson(json!);
  }

  /// `POST /inspections/{code}/evidences/{ownerType}` — ảnh bằng chứng thật
  /// (BR-083), server tự resolve ownerId = UUID của phiên. `ownerType` một
  /// trong STEP1_FRESH/STEP1_DRY/STEP2/STEP3/SESSION.
  Future<void> uploadEvidence(String code, String ownerType, String filePath) async {
    await _api.uploadFile('/inspections/$code/evidences/$ownerType', fieldName: 'file', filePath: filePath);
  }
}
