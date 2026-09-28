import '../../../core/network/api_client.dart';
import 'dashboard_models.dart';
import 'student_models.dart';
import 'student_repository.dart';

String _todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

/// Đối chiếu `04_Development/Frontend/src/app/(app)/medical-staff/dashboard/page.tsx`
/// — backend chưa có endpoint "dashboard summary" riêng, màn này tự gộp
/// `incidents`, `students`, `special-meals`, `source-reports`, `kitchen-inspections`
/// giống bản web (bản web không có thẻ kiểm tra bếp hôm nay — đã bỏ theo review
/// Mục 10.7 vì có nguồn khác; bản mobile thêm lại thẻ này để đáp ứng yêu cầu
/// "today's inspection status" bằng cách đọc thẳng `kitchen-inspections`,
/// không hard-code).
class MedicalStaffDashboardRepository {
  MedicalStaffDashboardRepository._();
  static final MedicalStaffDashboardRepository instance = MedicalStaffDashboardRepository._();

  final _api = ApiClient.instance;

  /*
   * 13/08/2026 — trước đây 5 request này chạy TUẦN TỰ (await lần lượt), độc lập với
   * nhau nên không có lý do gì phải chờ cái trước xong mới gọi cái sau — chạy song song
   * bằng `Future.wait` giảm thời gian tải dashboard gần 5 lần trên mạng di động (đúng vấn
   * đề "mobile load API chậm hơn web" mà `api_client.dart` đã ghi nhận ở chỗ khác).
   */
  Future<MedicalStaffDashboardData> load() async {
    final today = _todayIso();
    final results = await Future.wait<dynamic>([
      _api.get('/incidents', query: const {'pageSize': '100'}),
      MedicalStudentRepository.instance.fetchAll(),
      _api.get('/special-meals', query: const {'pageSize': '100'}),
      _api.get('/source-reports', query: const {'pageSize': '100'}),
      _api.get('/kitchen-inspections', query: const {'pageSize': '30'}),
    ]);

    final incidentsJson = results[0] as Map<String, dynamic>?;
    final incidents = ((incidentsJson?['items'] as List?) ?? const []).cast<Map<String, dynamic>>();
    final investigating = incidents.where((i) => i['status'] == 'INVESTIGATING').length;

    final students = results[1] as List<MedicalStudent>;
    final pendingReviews =
        students.where((s) => s.healthStatus == 'PENDING' || s.healthStatus == 'NEEDS_INFO').length;

    final specialMealsJson = results[2] as Map<String, dynamic>?;
    final specialMeals = ((specialMealsJson?['items'] as List?) ?? const []).cast<Map<String, dynamic>>();
    final pendingSpecialMeals = specialMeals.where((r) => r['status'] == 'PENDING').length;

    final reportsJson = results[3] as Map<String, dynamic>?;
    final reports = ((reportsJson?['items'] as List?) ?? const []).cast<Map<String, dynamic>>();
    final pendingReports =
        reports.where((r) => r['status'] == 'PENDING_TRIAGE' || r['status'] == 'DRAFTING').length;

    final inspectionsJson = results[4] as Map<String, dynamic>?;
    final inspections = ((inspectionsJson?['items'] as List?) ?? const []).cast<Map<String, dynamic>>();
    final todayInspection = inspections.cast<Map<String, dynamic>?>().firstWhere(
          (k) => ((k?['date'] as String?) ?? '').startsWith(today),
          orElse: () => null,
        );

    return MedicalStaffDashboardData(
      pendingHealthReviewsCount: pendingReviews,
      investigatingIncidentsCount: investigating,
      pendingSpecialMealsCount: pendingSpecialMeals,
      pendingSourceReportsCount: pendingReports,
      todayInspectionDone: todayInspection != null,
      todayInspectionScore: (todayInspection?['score'] as num?)?.toInt(),
      todayInspectionResult: todayInspection?['result'] as String?,
    );
  }
}
