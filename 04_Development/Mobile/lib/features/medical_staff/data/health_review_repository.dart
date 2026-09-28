import '../../../core/network/api_client.dart';
import 'health_review_models.dart';

/// `allergy-declarations` + `chronic-conditions` + `special-meals` (backend,
/// MF03 — UC-077/UC-078, BR-031/BR-065/BR-066). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/health-reviews/page.tsx`
/// và `.../special-meals/page.tsx`.
class HealthReviewRepository {
  HealthReviewRepository._();
  static final HealthReviewRepository instance = HealthReviewRepository._();

  final _api = ApiClient.instance;

  Future<List<AllergyDeclaration>> fetchAllergies() async {
    final json = await _api.get('/allergy-declarations', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => AllergyDeclaration.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// UC-077 — `POST /allergy-declarations/{id}/verify` (BR-031 → BR-123).
  Future<void> verifyAllergy(String id) async {
    await _api.post('/allergy-declarations/$id/verify');
  }

  /// `POST /allergy-declarations/{id}/request-info` (BR-032, `reason` bắt buộc).
  Future<void> requestAllergyInfo(String id, String reason) async {
    await _api.post('/allergy-declarations/$id/request-info', body: {'reason': reason});
  }

  Future<List<ChronicCondition>> fetchChronicConditions() async {
    final json = await _api.get('/chronic-conditions', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => ChronicCondition.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> verifyChronic(String id) async {
    await _api.post('/chronic-conditions/$id/verify');
  }

  Future<void> requestChronicInfo(String id, String reason) async {
    await _api.post('/chronic-conditions/$id/request-info', body: {'reason': reason});
  }

  Future<List<SpecialMealRequest>> fetchSpecialMeals({String? status}) async {
    final json = await _api.get('/special-meals', query: {
      'pageSize': '200',
      if (status != null) 'status': status,
    });
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => SpecialMealRequest.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// UC-078 — `POST /special-meals/{id}/approve` (BR-066/BR-220: ngoài danh
  /// mục chuẩn ⇒ 422 STU-066, backend tự chuyển sang NEEDS_INFO).
  Future<void> approveSpecialMeal(String id, {String? note}) async {
    await _api.post('/special-meals/$id/approve', body: {if (note != null) 'note': note});
  }

  Future<void> rejectSpecialMeal(String id, String reason) async {
    await _api.post('/special-meals/$id/reject', body: {'reason': reason});
  }
}
