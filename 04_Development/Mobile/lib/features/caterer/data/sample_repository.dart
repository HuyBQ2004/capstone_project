import '../../../core/network/api_client.dart';
import 'inspection_models.dart';
import 'inspection_repository.dart';
import 'sample_models.dart';

/// `samples` (backend MF08 — UC-069/118). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/samples/page.tsx`.
class SampleRepository {
  SampleRepository._();
  static final SampleRepository instance = SampleRepository._();

  final _api = ApiClient.instance;

  Future<List<FoodSample>> fetchSamples({String? mealType}) async {
    final json = await _api.get('/samples', query: {
      'pageSize': '200',
      if (mealType != null) 'mealType': mealType,
    });
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => FoodSample.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// Phiên kiểm thực đã PASSED cả 3 bước — nguồn để đăng ký lưu mẫu (BR-105).
  Future<List<InspectionSession>> fetchPassedSessions() async {
    final sessions = await InspectionRepository.instance.fetchList();
    return sessions.where((s) => s.overall == 'PASSED').toList();
  }

  List<DishChoice> dishChoicesOf(InspectionSession session) {
    if (session.step3Rows.isNotEmpty) {
      return session.step3Rows
          .where((r) => r.dishName.trim().isNotEmpty && r.dishId != null)
          .map((r) => DishChoice(name: r.dishName, dishId: r.dishId!, servings: r.servings))
          .toList();
    }
    return session.step2Rows
        .where((r) => r.dishName.trim().isNotEmpty && r.dishId != null)
        .map((r) => DishChoice(name: r.dishName, dishId: r.dishId!, servings: r.servings))
        .toList();
  }

  /// `POST /samples` (multipart `payload` JSON + `evidencePhotos[]`) — UC-069.
  Future<FoodSample> register({
    required String inspectionId,
    required String dishId,
    required String mealType,
    required String mealShift,
    required int servings,
    required double amount,
    required String unit, // "G" | "ML"
    required String container,
    required bool sanitized,
    required bool sealed,
    required double fridgeTemp,
    required String collectedAtIso,
    required String storageLocation,
    List<String> evidencePhotoPaths = const [],
  }) async {
    final json = await _api.uploadMultipart(
      '/samples',
      jsonFieldName: 'payload',
      jsonBody: {
        'inspectionId': inspectionId,
        'dishId': dishId,
        'mealType': mealType,
        'mealShift': mealShift,
        'servings': servings,
        'amount': amount,
        'unit': unit,
        'container': container,
        'sanitized': sanitized,
        'sealed': sealed,
        'fridgeTemp': fridgeTemp,
        'collectedAt': collectedAtIso,
        'storageLocation': storageLocation,
      },
      fileFields: {
        if (evidencePhotoPaths.isNotEmpty) 'evidencePhotos': evidencePhotoPaths,
      },
    );
    return FoodSample.fromJson(json!);
  }
}
