import '../../../core/network/api_client.dart';
import 'safety_report_models.dart';

/// `POST /source-reports` (`SourceReportController`, MF11) — Parent chỉ có
/// quyền TẠO + bổ sung ảnh bằng chứng (`@PreAuthorize hasAnyRole('TEACHER',
/// 'PARENT')` cho 2 endpoint này); `GET /source-reports` (danh sách) CHỈ cho
/// SYSTEM_ADMIN/SCHOOL_ADMIN/TEACHER/MEDICAL_STAFF — Parent gọi sẽ bị 403, nên
/// màn này (đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/safety-concern/page.tsx`)
/// không có danh sách lịch sử, chỉ có form gửi + xác nhận đã gửi.
class SafetyReportRepository {
  SafetyReportRepository._();
  static final SafetyReportRepository instance = SafetyReportRepository._();

  final _api = ApiClient.instance;

  /// `className` bắt buộc ở DTO nhưng backend bỏ qua với Parent — tự dò
  /// trường/lớp qua `studentCode` (BR-201). Vẫn gửi để qua validate.
  Future<SafetyReport> create({
    required String className,
    required String studentCode,
    required String suspectedMealText,
    required String description,
    int evidencePhotos = 0,
  }) async {
    final json = await _api.post('/source-reports', body: {
      'className': className,
      'studentCode': studentCode,
      'suspectedMealText': suspectedMealText,
      'description': description,
      'evidencePhotos': evidencePhotos,
    });
    return SafetyReport.fromJson(json!);
  }

  /// `POST /source-reports/{code}/evidence-photos` — multipart, field `files`.
  Future<void> uploadEvidencePhoto(String reportId, String filePath) =>
      _api.uploadFile('/source-reports/$reportId/evidence-photos', fieldName: 'files', filePath: filePath);
}
