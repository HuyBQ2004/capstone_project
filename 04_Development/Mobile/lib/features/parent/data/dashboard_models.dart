import 'child_models.dart';
import 'menu_models.dart';

/// Gộp dữ liệu Dashboard Parent — mirror
/// `04_Development/Frontend/src/app/(app)/parent/dashboard/page.tsx`, không có
/// endpoint "dashboard summary" riêng, tự gộp từ `parents/{code}/students` +
/// `menus?grade=`.
class ParentDashboardData {
  final List<ParentChild> children;
  final ParentChild? activeChild;
  final ParentMenu? todayOrNextMenu;
  final bool isToday;

  ParentDashboardData({
    required this.children,
    required this.activeChild,
    required this.todayOrNextMenu,
    required this.isToday,
  });
}
