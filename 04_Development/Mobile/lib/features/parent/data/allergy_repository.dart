import '../../../core/network/api_client.dart';
import 'allergy_models.dart';

/// `allergy-declarations` + `students/{code}/health-documents` (backend, MF03)
/// — đối chiếu `04_Development/Frontend/src/app/(app)/parent/allergies/page.tsx`.
class AllergyRepository {
  AllergyRepository._();
  static final AllergyRepository instance = AllergyRepository._();

  final _api = ApiClient.instance;

  /// Backend không lọc sẵn theo con — tự lọc `studentId` phía client, giống
  /// bản web (`declared.filter((a) => a.studentId === child.studentId)`).
  Future<List<AllergyDeclaration>> fetchForStudent(String studentId) async {
    final json = await _api.get('/allergy-declarations', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    return items
        .map((e) => AllergyDeclaration.fromJson(e as Map<String, dynamic>))
        .where((d) => d.studentId == studentId)
        .toList()
      ..sort((a, b) => (b.declaredAt ?? '').compareTo(a.declaredAt ?? ''));
  }

  /// `POST /students/{code}/health-documents` — upload TRƯỚC khi khai báo, lấy
  /// `id` trả về làm `proofFileId` (bắt buộc khi `severity == SEVERE`, BR-031).
  Future<String> uploadProof(String studentId, String filePath) async {
    final json = await _api.uploadFile(
      '/students/$studentId/health-documents',
      fieldName: 'file',
      filePath: filePath,
    );
    return json!['id'] as String;
  }

  /// `POST /allergy-declarations` (`SubmitAllergyRequest`). `id`/`declaredAt`/
  /// `status` do server tự sinh — trạng thái mặc định luôn `PENDING`.
  Future<AllergyDeclaration> create({
    required String studentId,
    required String category,
    required String allergen,
    required String severity,
    String? proofFileId,
  }) async {
    final json = await _api.post('/allergy-declarations', body: {
      'studentId': studentId,
      'category': category,
      'allergen': allergen,
      'severity': severity,
      if (proofFileId != null) 'proofFileId': proofFileId,
    });
    return AllergyDeclaration.fromJson(json!);
  }
}
