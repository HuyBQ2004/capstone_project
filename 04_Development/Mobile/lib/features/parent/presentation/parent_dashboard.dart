import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../ai_chat/presentation/ai_chat_button.dart';
import '../../notifications/presentation/notification_bell_button.dart';
import '../../profile/presentation/profile_avatar_button.dart';
import '../data/child_models.dart';
import '../data/dashboard_models.dart';
import '../data/dashboard_repository.dart';
import '../data/menu_models.dart';
import 'allergy_screen.dart';
import 'meal_history_screen.dart';
import 'safety_concern_screen.dart';
import 'weekly_menu_screen.dart';
import 'widgets/child_selector.dart';
import '../data/child_repository.dart';

/// 01_ParentDashboard.md — đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/dashboard/page.tsx`. Tóm tắt
/// (các) con + bữa ăn hôm nay/sắp tới của con đang chọn; 4 lối vào nhanh sang
/// các màn còn lại của Parent.
class ParentDashboard extends StatefulWidget {
  const ParentDashboard({super.key});

  @override
  State<ParentDashboard> createState() => _ParentDashboardState();
}

class _ParentDashboardState extends State<ParentDashboard> {
  late Future<ParentDashboardData> _future;

  @override
  void initState() {
    super.initState();
    _future = ParentDashboardRepository.instance.load();
  }

  Future<void> _reload() async {
    final next = ParentDashboardRepository.instance.load();
    // `() => _future = next` là arrow function — trả về GIÁ TRỊ của phép gán (chính `next`,
    // một Future), không phải void, khiến setState() tưởng callback là async. Dùng block
    // body để callback thực sự trả về void.
    setState(() {
      _future = next;
    });
    await next;
  }

  Future<void> _selectChild(String studentId) async {
    ParentChildRepository.instance.setActiveChild(studentId);
    await _reload();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Phụ Huynh'),
        actions: const [AiChatButton(), NotificationBellButton(), ProfileAvatarButton()],
      ),
      body: SafeArea(
        child: FutureBuilder<ParentDashboardData>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
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

            final data = snapshot.data!;
            if (data.children.isEmpty) {
              return RefreshIndicator(
                onRefresh: _reload,
                child: ListView(
                  padding: const EdgeInsets.all(24),
                  children: const [
                    SizedBox(height: 80),
                    Center(
                      child: Text(
                        'Tài khoản chưa liên kết học sinh nào. Liên hệ nhà trường để được hỗ trợ.',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: Colors.white54),
                      ),
                    ),
                  ],
                ),
              );
            }

            return RefreshIndicator(
              onRefresh: _reload,
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  ChildSelector(children: data.children, active: data.activeChild, onSelect: _selectChild),
                  if (data.children.length > 1) const SizedBox(height: 12),
                  _ChildSummaryCard(child: data.activeChild!),
                  const SizedBox(height: 20),
                  _QuickActions(),
                  const SizedBox(height: 20),
                  _TodayMenuCard(data: data),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}

class _ChildSummaryCard extends StatelessWidget {
  final ParentChild child;
  const _ChildSummaryCard({required this.child});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            const CircleAvatar(
              radius: 26,
              backgroundColor: AppTheme.primarySky,
              child: Icon(Icons.child_care, color: Colors.white, size: 28),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(child.name ?? child.id, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const SizedBox(height: 2),
                  Text(
                    'Lớp ${child.classroomId ?? '—'} · Mã HS: ${child.id}',
                    style: const TextStyle(color: Colors.white70, fontSize: 13),
                  ),
                  if (child.allergies.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 6,
                      runSpacing: 6,
                      children: [
                        for (final a in child.allergies)
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                            decoration: BoxDecoration(
                              color: AppTheme.dangerRose.withValues(alpha: 0.15),
                              borderRadius: BorderRadius.circular(999),
                            ),
                            child: Text(a, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 11, fontWeight: FontWeight.w600)),
                          ),
                      ],
                    ),
                  ] else if (child.specialDiet != null) ...[
                    const SizedBox(height: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: AppTheme.primarySky.withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text('Chế độ ăn: ${child.specialDiet}', style: const TextStyle(color: AppTheme.primarySky, fontSize: 11)),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _QuickActions extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final actions = <(_QuickActionData, Widget Function())>[
      (
        const _QuickActionData(icon: Icons.restaurant_menu, label: 'Thực đơn tuần', color: AppTheme.primarySky),
        () => const WeeklyMenuScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.history, label: 'Lịch sử bữa ăn', color: AppTheme.successEmerald),
        () => const MealHistoryScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.health_and_safety_outlined, label: 'Khai báo dị ứng', color: AppTheme.warningAmber),
        () => const AllergyScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.report_problem_outlined, label: 'Báo sự cố ATTP', color: AppTheme.dangerRose),
        () => const SafetyConcernScreen(),
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
              Text(data.label, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
            ],
          ),
        ),
      ),
    );
  }
}

class _TodayMenuCard extends StatelessWidget {
  final ParentDashboardData data;
  const _TodayMenuCard({required this.data});

  @override
  Widget build(BuildContext context) {
    final menu = data.todayOrNextMenu;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.soup_kitchen_outlined, color: AppTheme.primarySky),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    data.isToday ? 'Bữa ăn hôm nay' : 'Bữa ăn sắp tới',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                  ),
                ),
                TextButton(
                  onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const WeeklyMenuScreen())),
                  child: const Text('Cả tuần →'),
                ),
              ],
            ),
            const SizedBox(height: 8),
            if (menu == null)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 16),
                child: Text(
                  'Chưa có thực đơn nào được công bố cho ${data.activeChild?.grade ?? 'khối này'}.',
                  style: const TextStyle(color: Colors.white54),
                ),
              )
            else ...[
              Text(menu.week ?? menu.menuDateIso ?? '—', style: const TextStyle(color: Colors.white70, fontSize: 12)),
              const SizedBox(height: 10),
              for (final block in menu.blocks) ...[
                Text(
                  mealTypeLabel[block.mealType] ?? block.mealType,
                  style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: Colors.white70),
                ),
                const SizedBox(height: 4),
                Text(
                  block.dishes.map((d) => d.name ?? '—').join(', '),
                  style: const TextStyle(fontSize: 13),
                ),
                const SizedBox(height: 8),
              ],
              Text('Tổng ${menu.totalDishes} món · ${menu.totalKcal} kcal', style: const TextStyle(color: Colors.white38, fontSize: 11)),
            ],
          ],
        ),
      ),
    );
  }
}
