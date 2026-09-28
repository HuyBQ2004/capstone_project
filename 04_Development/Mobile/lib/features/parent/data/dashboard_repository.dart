import 'child_repository.dart';
import 'dashboard_models.dart';
import 'menu_models.dart';
import 'menu_repository.dart';

/// Đối chiếu `04_Development/Frontend/src/app/(app)/parent/dashboard/page.tsx`
/// — dữ liệu theo con đang chọn: thực đơn hôm nay, hoặc bữa gần nhất sắp tới
/// nếu hôm nay chưa có thực đơn công bố.
class ParentDashboardRepository {
  ParentDashboardRepository._();
  static final ParentDashboardRepository instance = ParentDashboardRepository._();

  Future<ParentDashboardData> load() async {
    final children = await ParentChildRepository.instance.fetchChildren();
    final active = ParentChildRepository.instance.activeChildOf(children);

    if (active == null) {
      return ParentDashboardData(children: children, activeChild: null, todayOrNextMenu: null, isToday: false);
    }

    final menus = await ParentMenuRepository.instance.fetchPublishedForGrade(active.grade, active.id);
    final today = todayIso();

    ParentMenu? todayMenu;
    for (final m in menus) {
      if (m.menuDateIso == today) {
        todayMenu = m;
        break;
      }
    }

    ParentMenu? nextMenu;
    if (todayMenu == null) {
      for (final m in menus) {
        if ((m.menuDateIso ?? '').compareTo(today) > 0) {
          nextMenu = m;
          break;
        }
      }
    }

    return ParentDashboardData(
      children: children,
      activeChild: active,
      todayOrNextMenu: todayMenu ?? nextMenu,
      isToday: todayMenu != null,
    );
  }
}
