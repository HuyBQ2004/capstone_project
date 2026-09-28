/// Gộp toàn bộ thẻ thống kê của Dashboard Medical Staff — mirror
/// `04_Development/Frontend/src/app/(app)/medical-staff/dashboard/page.tsx`,
/// backend chưa có endpoint "dashboard summary" riêng nên tự gộp 3 nguồn thật
/// (`incidents`, `students`, `special-meals`).
class MedicalStaffDashboardData {
  final int pendingHealthReviewsCount;
  final int investigatingIncidentsCount;
  final int pendingSpecialMealsCount;
  final int pendingSourceReportsCount;
  final bool todayInspectionDone;
  final int? todayInspectionScore;
  final String? todayInspectionResult;

  MedicalStaffDashboardData({
    required this.pendingHealthReviewsCount,
    required this.investigatingIncidentsCount,
    required this.pendingSpecialMealsCount,
    required this.pendingSourceReportsCount,
    required this.todayInspectionDone,
    required this.todayInspectionScore,
    required this.todayInspectionResult,
  });
}
