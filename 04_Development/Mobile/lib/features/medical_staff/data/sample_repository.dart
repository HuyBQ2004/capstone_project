import '../../../core/network/api_client.dart';
import 'sample_models.dart';

/// `samples` + `sample-lab-requests` + `receiving-authorities` (backend, MF08).
/// Đối chiếu `04_Development/Frontend/src/app/(app)/medical-staff/samples/page.tsx`.
class SampleRepository {
  SampleRepository._();
  static final SampleRepository instance = SampleRepository._();

  final _api = ApiClient.instance;

  Future<List<FoodSample>> fetchSamples({String? status}) async {
    final json = await _api.get('/samples', query: {
      'pageSize': '100',
      if (status != null) 'status': status,
    });
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => FoodSample.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<IncidentBrief>> fetchIncidents() async {
    final json = await _api.get('/incidents', query: const {'pageSize': '50'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => IncidentBrief.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<ReceivingAuthority>> fetchAuthorities() async {
    final json = await _api.get('/receiving-authorities', query: const {'pageSize': '50'});
    final items = (json?['items'] as List?) ?? const [];
    return items
        .map((e) => ReceivingAuthority.fromJson(e as Map<String, dynamic>))
        .where((a) => a.isActive)
        .toList();
  }

  /// `GET /sample-lab-requests/retrievable?incidentId=&includeAllDishes=` —
  /// server tự tính cửa sổ điều tra 24h (BR-149), trả THẲNG mảng mẫu RETAINED.
  Future<List<FoodSample>> fetchRetrievable(String incidentId, {bool includeAllDishes = false}) async {
    final json = await _api.getList('/sample-lab-requests/retrievable', query: {
      'incidentId': incidentId,
      'includeAllDishes': includeAllDishes ? 'true' : 'false',
    });
    return (json ?? const []).map((e) => FoodSample.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// `POST /sample-lab-requests` (multipart `payload` + `sealPhotos[]` +
  /// `handoverRecords[]`) — Medical Staff tự thu hồi & bàn giao mẫu (BR-113).
  Future<SampleLabRequest> handOver({
    required String incidentId,
    required List<String> sampleIds,
    required String handoverType,
    String? authorityId,
    String? testCriteria,
    String? labRepresentative,
    List<String> handoverRecordPaths = const [],
    List<String> sealPhotoPaths = const [],
  }) async {
    final json = await _api.uploadMultipart(
      '/sample-lab-requests',
      jsonFieldName: 'payload',
      jsonBody: {
        'incidentId': incidentId,
        'sampleIds': sampleIds,
        'handoverType': handoverType,
        if (authorityId != null) 'authorityId': authorityId,
        if (testCriteria != null) 'testCriteria': testCriteria,
        if (labRepresentative != null) 'labRepresentative': labRepresentative,
      },
      fileFields: {
        if (sealPhotoPaths.isNotEmpty) 'sealPhotos': sealPhotoPaths,
        if (handoverRecordPaths.isNotEmpty) 'handoverRecords': handoverRecordPaths,
      },
    );
    return SampleLabRequest.fromJson(json!);
  }

  /// UC-076 AF1 — liên kết mẫu ĐÃ HUỶ với sự cố (chỉ metadata).
  Future<FoodSample> linkDisposedToIncident(String sampleId, String incidentId) async {
    final json = await _api.post('/samples/$sampleId/incident-link', body: {'incidentId': incidentId});
    return FoodSample.fromJson(json!);
  }
}
