import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../ai_chat/presentation/ai_chat_button.dart';
import '../../notifications/presentation/notification_bell_button.dart';
import '../../profile/presentation/profile_avatar_button.dart';
import '../data/classroom_models.dart';
import '../data/dashboard_models.dart';
import '../data/dashboard_repository.dart';
import 'class_deliveries_screen.dart';
import 'incident_report_screen.dart';
import 'meal_distribution_screen.dart';
import 'meal_records_screen.dart';

/// 01_TeacherHome.md — đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/dashboard/page.tsx`. Bảng
/// tin hôm nay tổng hợp cảnh báo dị ứng/suất ăn đặc biệt/sự cố mới nhất
/// (BR-123, BR-138); 4 lối vào nhanh sang các màn còn lại của Teacher.
class TeacherDashboard extends StatefulWidget {
  const TeacherDashboard({super.key});

  @override
  State<TeacherDashboard> createState() => _TeacherDashboardState();
}

class _TeacherDashboardState extends State<TeacherDashboard> {
  late Future<(Classroom?, TeacherDashboardData)> _future;

  @override
  void initState() {
    super.initState();
    _future = TeacherDashboardRepository.instance.load();
  }

  Future<void> _reload() async {
    final next = TeacherDashboardRepository.instance.load();
    // `() => _future = next` trả về GIÁ TRỊ phép gán (chính `next`, một Future), không phải
    // void — cùng lỗi đã vá ở `parent_dashboard.dart`. Dùng block body để trả về void thật.
    setState(() {
      _future = next;
    });
    await next;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Giáo Viên'),
        actions: const [AiChatButton(), NotificationBellButton(), ProfileAvatarButton()],
      ),
      body: SafeArea(
        child: FutureBuilder<(Classroom?, TeacherDashboardData)>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              // 13/08/2026 (Nam) — trước đây lỗi thật (VD TypeError ép kiểu sai khi parse JSON)
              // bị nuốt hoàn toàn, chỉ hiện chữ chung chung "Không tải được dữ liệu" — không có
              // cách nào biết ĐÚNG chỗ nào hỏng khi debug từ xa (đo được: report của người dùng
              // hiện đúng thông báo NÀY chứ không phải thông báo lỗi API cụ thể, tức lỗi KHÔNG
              // phải HTTP/ApiException mà là lỗi Dart runtime khi parse). In ra console để lần
              // sau tái hiện là bắt được ngay dòng/kiểu lỗi thật trong `adb logcat`.
              // ignore: avoid_print
              debugPrint('[TeacherDashboard] load() failed: ${snapshot.error}\n${snapshot.stackTrace}');
              final message = snapshot.error is ApiException
                  ? (snapshot.error as ApiException).message
                  : 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.';
              return Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(message, textAlign: TextAlign.center),
                      const SizedBox(height: 12),
                      ElevatedButton(onPressed: _reload, child: const Text('Thử lại')),
                    ],
                  ),
                ),
              );
            }

            final (classroom, data) = snapshot.data!;
            return RefreshIndicator(
              onRefresh: _reload,
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  Text(
                    'Lớp ${classroom?.id ?? '—'} — ${classroom?.teacher ?? '—'}',
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Sĩ số ${data.classStudentsCount}',
                    style: const TextStyle(color: Colors.white60),
                  ),
                  const SizedBox(height: 16),
                  _QuickActions(classroom: classroom),
                  const SizedBox(height: 20),
                  _NoticeBoard(classroom: classroom, data: data),
                  const SizedBox(height: 20),
                  _StatsGrid(data: data),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}

class _QuickActions extends StatelessWidget {
  final Classroom? classroom;
  const _QuickActions({required this.classroom});

  @override
  Widget build(BuildContext context) {
    final actions = <(_QuickActionData, Widget Function())>[
      (
        const _QuickActionData(
          icon: Icons.local_shipping_outlined,
          label: 'Nhận suất ăn từ bếp',
          color: AppTheme.primarySky,
        ),
        () => const ClassDeliveriesScreen(),
      ),
      (
        const _QuickActionData(
          icon: Icons.restaurant,
          label: 'Phân phát bữa ăn',
          color: AppTheme.successEmerald,
        ),
        () => const MealDistributionScreen(),
      ),
      (
        const _QuickActionData(
          icon: Icons.fact_check_outlined,
          label: 'Ghi nhận bữa ăn',
          color: AppTheme.warningAmber,
        ),
        () => const MealRecordsScreen(),
      ),
      (
        const _QuickActionData(
          icon: Icons.health_and_safety_outlined,
          label: 'Báo cáo sự cố',
          color: AppTheme.dangerRose,
        ),
        () => const IncidentReportScreen(),
      ),
    ];

    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: 12,
      crossAxisSpacing: 12,
      childAspectRatio: 1.5,
      children: [
        for (final (data, builder) in actions)
          _QuickActionCard(
            data: data,
            onTap: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => builder())),
          ),
      ],
    );
  }
}

class _QuickActionData {
  final IconData icon;
  final String label;
  final Color color;
  const _QuickActionData({required this.icon, required this.label, required this.color});
}

class _QuickActionCard extends StatelessWidget {
  final _QuickActionData data;
  final VoidCallback onTap;
  const _QuickActionCard({required this.data, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(data.icon, color: data.color, size: 28),
              const SizedBox(height: 10),
              Text(
                data.label,
                style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _NoticeBoard extends StatelessWidget {
  final Classroom? classroom;
  final TeacherDashboardData data;
  const _NoticeBoard({required this.classroom, required this.data});

  @override
  Widget build(BuildContext context) {
    final latest = data.latestIncident;
    final items = <Widget>[
      if (data.approvedSpecialMeals.isNotEmpty)
        _NoticeTile(
          icon: Icons.soup_kitchen_outlined,
          color: AppTheme.warningAmber,
          title: '${data.approvedSpecialMeals.length} suất ăn đặc biệt cần kiểm tra trước giờ ăn (BR-125)',
          detail: data.approvedSpecialMeals.map((r) => '${r.student ?? '—'} (${r.dietaryType ?? '—'})').join(' · '),
        ),
      if (latest != null)
        _NoticeTile(
          icon: Icons.campaign_outlined,
          color: AppTheme.primarySky,
          title: 'Sự cố mới nhất: ${latest.title ?? '—'}',
          detail: '${latest.affectedStudentsCount} học sinh ảnh hưởng — đang điều tra (BR-138)',
        ),
      if (data.approvedSpecialMeals.isEmpty && latest == null)
        const _NoticeTile(
          icon: Icons.check_circle_outline,
          color: AppTheme.successEmerald,
          title: 'Chưa có cảnh báo nào hôm nay',
          detail: 'Lớp học đang ổn định.',
        ),
    ];

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.notifications_active_outlined, color: AppTheme.primarySky),
                SizedBox(width: 8),
                Text('Bảng tin hôm nay', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              ],
            ),
            const SizedBox(height: 10),
            ...items,
          ],
        ),
      ),
    );
  }
}

class _NoticeTile extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String title;
  final String detail;
  const _NoticeTile({required this.icon, required this.color, required this.title, required this.detail});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        border: Border.all(color: AppTheme.borderDark),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 18, color: color),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                if (detail.isNotEmpty) ...[
                  const SizedBox(height: 2),
                  Text(detail, style: const TextStyle(fontSize: 12, color: Colors.white60)),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StatsGrid extends StatelessWidget {
  final TeacherDashboardData data;
  const _StatsGrid({required this.data});

  @override
  Widget build(BuildContext context) {
    final stats = [
      ('Sĩ số hôm nay', '${data.classStudentsCount}', Icons.groups_outlined, AppTheme.primarySky),
      ('Suất ăn đặc biệt', '${data.approvedSpecialMeals.length}', Icons.soup_kitchen_outlined, AppTheme.warningAmber),
      ('HS có dị ứng', '${data.allergicStudentsCount}', Icons.warning_amber_rounded, AppTheme.dangerRose),
      (
        'Đã phát / Kế hoạch hôm nay',
        '${data.todayDistributed}/${data.todayPlanned}',
        Icons.restaurant_menu,
        AppTheme.successEmerald,
      ),
    ];

    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: 12,
      crossAxisSpacing: 12,
      childAspectRatio: 1.4,
      children: [
        for (final (label, value, icon, color) in stats)
          Card(
            margin: EdgeInsets.zero,
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(icon, color: color, size: 22),
                  const SizedBox(height: 8),
                  Text(value, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 2),
                  Text(label, style: const TextStyle(fontSize: 11, color: Colors.white60)),
                ],
              ),
            ),
          ),
      ],
    );
  }
}
