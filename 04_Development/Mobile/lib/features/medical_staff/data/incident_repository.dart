import '../../../core/network/api_client.dart';
import 'incident_models.dart';

/// `source-reports` + `incident-records` + `incidents` + `corrective-actions`
/// (backend, MF11). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/incidents/page.tsx`
/// và `[id]/page.tsx`. Scope mobile — theo TASKS_MOBILE_SCOPE.md — ưu tiên
/// dùng "hàng ngày": xem danh sách, tạo biên bản, mở phiên điều tra, đánh giá
/// mức độ, ghi kết luận điều tra, xác nhận khắc phục. KHÔNG port đủ luồng
/// desktop (món/lô nghi vấn, bằng chứng camera, báo cáo chính thức PDF) —
/// các việc đó vẫn làm ở bản Web.
class IncidentRepository {
  IncidentRepository._();
  static final IncidentRepository instance = IncidentRepository._();

  final _api = ApiClient.instance;

  Future<List<SourceReport>> fetchSourceReports() async {
    final json = await _api.get('/source-reports', query: const {'pageSize': '100'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => SourceReport.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<IncidentRecord>> fetchRecords() async {
    final json = await _api.get('/incident-records', query: const {'pageSize': '100'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => IncidentRecord.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<Incident>> fetchIncidents() async {
    final json = await _api.get('/incidents', query: const {'pageSize': '100'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => Incident.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<Incident> fetchIncidentDetail(String code) async {
    final json = await _api.get('/incidents/$code');
    return Incident.fromJson(json!);
  }

  Future<List<CorrectiveAction>> fetchCorrectiveActions() async {
    final json = await _api.get('/corrective-actions', query: const {'pageSize': '100'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => CorrectiveAction.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> closeReport(String code) async {
    await _api.post('/source-reports/$code/close-not-food-safety');
  }

  Future<IncidentRecord> createRecord({required List<String> sourceReportCodes, required String content}) async {
    final json = await _api.post('/incident-records', body: {
      'sourceReportCodes': sourceReportCodes,
      'content': content,
    });
    return IncidentRecord.fromJson(json!);
  }

  Future<void> closeRecord(String code) async {
    await _api.post('/incident-records/$code/close-not-food-safety');
  }

  /// Trả về mã sự cố (INC-xxx) vừa tạo (ADR-M11-1 — atomic).
  Future<String> openInvestigation({
    required String recordCode,
    required String title,
    required List<String> affectedStudentCodes,
  }) async {
    final json = await _api.post('/incident-records/$recordCode/open-investigation', body: {
      'title': title,
      'affectedStudentCodes': affectedStudentCodes,
    });
    return json!['incidentCode'] as String;
  }

  /// UC-009 — phân loại mức độ (BR-147), chuyển sự cố sang INVESTIGATING.
  Future<Incident> assess({
    required String code,
    required String severity,
    String? clinicalRiskAssessment,
  }) async {
    final json = await _api.post('/incidents/$code/assess', body: {
      'severity': severity,
      if (clinicalRiskAssessment != null) 'clinicalRiskAssessment': clinicalRiskAssessment,
    });
    return Incident.fromJson(json! as Map<String, dynamic>);
  }

  /// Ghi kết luận nguyên nhân gốc rễ — Sơ bộ đủ giao CAPA, Chính thức khi có
  /// kết quả lab (BR-150/151).
  Future<Incident> rootCause({
    required String code,
    required String rootCause,
    required String rootCauseStatus,
  }) async {
    final json = await _api.post('/incidents/$code/root-cause', body: {
      'rootCause': rootCause,
      'rootCauseStatus': rootCauseStatus,
    });
    return Incident.fromJson(json! as Map<String, dynamic>);
  }

  /// UC-168 — xác nhận khắc phục ĐẠT: server tự chốt CAPA COMPLETED và
  /// chuyển sự cố sang VALIDATED trong cùng transaction.
  Future<CorrectiveAction> validateCapa(String code) async {
    final json = await _api.post('/corrective-actions/$code/validate');
    return CorrectiveAction.fromJson(json!);
  }
}
