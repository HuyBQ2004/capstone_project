/// Dữ liệu tổng hợp cho Dashboard Đơn vị Suất ăn — đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/dashboard/page.tsx`
/// (rút gọn cho mobile theo TASKS_MOBILE_SCOPE.md: chỉ số kiểm thực/mẫu lưu/
/// nhận hàng hôm nay + lối vào nhanh, bỏ biểu đồ 7 ngày và bảng kho).
class CatererDashboardData {
  /// Số phiên kiểm thực mở hôm nay + kết quả tổng quan (nếu có).
  final int todayInspectionCount;
  final String? todayInspectionOverall;
  final int failedInspectionCount;
  final int retainedSamplesCount;
  final int dueDeliveriesCount;
  final int receivedTodayCount;

  CatererDashboardData({
    required this.todayInspectionCount,
    required this.todayInspectionOverall,
    required this.failedInspectionCount,
    required this.retainedSamplesCount,
    required this.dueDeliveriesCount,
    required this.receivedTodayCount,
  });
}
