import '../../../core/network/api_client.dart';
import 'incident_models.dart';

/// `source-reports` (backend, MF11 — "Redesign Incident") — Teacher chỉ tạo
/// BÁO CÁO THÔ (RPT-xxx, status PENDING_TRIAGE), KHÔNG tạo thẳng `incidents`
/// (INC-xxx, chỉ Y tế trường tạo sau khi gộp/triage). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/incidents/page.tsx`
/// (UC-074).
class IncidentRepository {
  IncidentRepository._();
  static final IncidentRepository instance = IncidentRepository._();

  final _api = ApiClient.instance;

  Future<List<SourceReport>> fetchHistory({required String className, String? status}) async {
    final query = <String, String>{'className': className, 'pageSize': '100'};
    if (status != null) query['status'] = status;
    final json = await _api.get('/source-reports', query: query);
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => SourceReport.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// `POST /source-reports` (`CreateSourceReportRequest`). BR-146 auto-detect
  /// cụm triệu chứng ≥3 HS/24h chạy phía server.
  Future<SourceReport> create({
    required String className,
    required String description,
    int evidencePhotos = 0,
  }) async {
    final json = await _api.post('/source-reports', body: {
      'className': className,
      'description': description,
      'evidencePhotos': evidencePhotos,
    });
    return SourceReport.fromJson(json!);
  }

  /// `POST /source-reports/{code}/evidence-photos` — multipart, field `files`.
  /// [ApiClient.uploadFile] chỉ hỗ trợ 1 file/lần gọi — gọi tuần tự cho từng
  /// ảnh nếu người dùng đính kèm nhiều ảnh.
  Future<void> uploadEvidencePhoto(String reportId, String filePath) =>
      _api.uploadFile('/source-reports/$reportId/evidence-photos', fieldName: 'files', filePath: filePath);
}
