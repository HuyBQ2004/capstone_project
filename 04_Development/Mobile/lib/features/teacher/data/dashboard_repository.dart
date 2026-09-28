import '../../../core/network/api_client.dart';
import 'classroom_models.dart';
import 'classroom_repository.dart';
import 'dashboard_models.dart';
import 'student_models.dart';
import 'student_repository.dart';

String _todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

/// Đối chiếu `04_Development/Frontend/src/app/(app)/teacher/dashboard/page.tsx`
/// — backend chưa có endpoint "dashboard summary" riêng cho Teacher, màn này
/// tự gộp 5 nguồn thật (`incidents`, `source-reports`, `students`,
/// `student-distributions`, `special-meals`) giống bản web.
class TeacherDashboardRepository {
  TeacherDashboardRepository._();
  static final TeacherDashboardRepository instance = TeacherDashboardRepository._();

  final _api = ApiClient.instance;

  /*
   * 13/08/2026 — trước đây 5 request chạy TUẦN TỰ. 4 trong 5 request cần `classId` (chỉ
   * biết được SAU KHI `resolveMyClassroom()` xong) nên không thể chạy song song hết —
   * nhưng `incidents` KHÔNG phụ thuộc `classId`, và 4 request còn lại độc lập VỚI NHAU
   * một khi đã có `classId`. Chạy `resolveMyClassroom()` + `incidents` song song trước,
   * rồi 4 request phụ thuộc classId song song tiếp — giảm 5 bước tuần tự còn 2.
   */
  Future<(Classroom?, TeacherDashboardData)> load() async {
    final results = await Future.wait<dynamic>([
      ClassroomRepository.instance.resolveMyClassroom(),
      // Backend đã lọc SẴN theo lớp chủ nhiệm cho Teacher (EXISTS subquery, D11).
      _api.get('/incidents', query: const {'pageSize': '100'}),
    ]);
    final classroom = results[0] as Classroom?;
    final incidentsJson = results[1] as Map<String, dynamic>?;
    final incidents = ((incidentsJson?['items'] as List?) ?? const [])
        .map((e) => IncidentTeacher.fromJson(e as Map<String, dynamic>))
        .toList();
    final classId = classroom?.id;

    var students = <Student>[];
    var pendingReports = 0;
    var approvedSpecialMeals = <SpecialMealRequestSummary>[];
    var todayDistributed = 0;
    var todayPlanned = 0;

    if (classId != null) {
      final classResults = await Future.wait<dynamic>([
        StudentRepository.instance.fetchClassStudents(classId),
        _api.get('/source-reports', query: const {'pageSize': '200'}),
        _api.get('/special-meals', query: {'class': classId, 'pageSize': '100'}),
        _api.get('/student-distributions', query: {
          'date': _todayIso(),
          'class': classId,
          'pageSize': '300',
        }),
      ]);

      students = classResults[0] as List<Student>;

      final reportsJson = classResults[1] as Map<String, dynamic>?;
      final reportItems = (reportsJson?['items'] as List?) ?? const [];
      pendingReports = reportItems
          .cast<Map<String, dynamic>>()
          .where((r) => r['className'] == classId && r['status'] == 'PENDING_TRIAGE')
          .length;

      final specialJson = classResults[2] as Map<String, dynamic>?;
      approvedSpecialMeals = ((specialJson?['items'] as List?) ?? const [])
          .map((e) => SpecialMealRequestSummary.fromJson(e as Map<String, dynamic>))
          .where((r) => r.status == 'APPROVED')
          .toList();

      final distJson = classResults[3] as Map<String, dynamic>?;
      final distItems = (distJson?['items'] as List?) ?? const [];
      todayPlanned = distItems.length;
      todayDistributed = distItems.cast<Map<String, dynamic>>().where((d) => d['status'] == 'SERVED').length;
    }

    final allergic = students.where((s) => s.hasAllergy).length;

    return (
      classroom,
      TeacherDashboardData(
        classStudentsCount: students.length,
        allergicStudentsCount: allergic,
        incidents: incidents,
        pendingReportsCount: pendingReports,
        approvedSpecialMeals: approvedSpecialMeals,
        todayDistributed: todayDistributed,
        todayPlanned: todayPlanned,
      ),
    );
  }
}
