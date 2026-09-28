import '../../../core/network/api_client.dart';
import 'dashboard_models.dart';
import 'inspection_repository.dart';
import 'sample_repository.dart';

String _todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

/// Gộp `inspections` (hôm nay) + `samples` (RETAINED) + `receiving-sessions`
/// (`due-deliveries` + hôm nay) — tất cả gọi thật, không mock.
class CatererDashboardRepository {
  CatererDashboardRepository._();
  static final CatererDashboardRepository instance = CatererDashboardRepository._();

  final _api = ApiClient.instance;

  Future<CatererDashboardData> load() async {
    final today = _todayIso();
    final inspections = await InspectionRepository.instance.fetchList(dateFrom: today, dateTo: today);
    final samples = await SampleRepository.instance.fetchSamples();
    final due = await _api.getList('/receiving-sessions/due-deliveries');
    final receivedToday = await _api.get('/receiving-sessions', query: {
      'pageSize': '100',
      'dateFrom': today,
      'dateTo': today,
    });

    String? overall;
    if (inspections.isNotEmpty) {
      overall = inspections.any((s) => s.overall == 'FAILED')
          ? 'FAILED'
          : inspections.every((s) => s.overall == 'PASSED')
              ? 'PASSED'
              : 'IN_PROGRESS';
    }

    return CatererDashboardData(
      todayInspectionCount: inspections.length,
      todayInspectionOverall: overall,
      failedInspectionCount: inspections.where((s) => s.overall == 'FAILED').length,
      retainedSamplesCount: samples.where((s) => s.status == 'RETAINED').length,
      dueDeliveriesCount: (due ?? const []).length,
      receivedTodayCount: (receivedToday?['items'] as List?)?.length ?? 0,
    );
  }
}
