import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../ai_chat/presentation/ai_chat_button.dart';
import '../../notifications/presentation/notification_bell_button.dart';
import '../../profile/presentation/profile_avatar_button.dart';
import '../data/dashboard_models.dart';
import '../data/dashboard_repository.dart';
import 'health_reviews_screen.dart';
import 'incidents_screen.dart';
import 'inspection_screen.dart';
import 'samples_screen.dart';

/// 01_MedicalStaffDashboard.md — đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/dashboard/page.tsx`.
/// Trạng thái kiểm tra bếp hôm nay + số sự cố đang điều tra + số khai báo sức
/// khỏe chờ duyệt; 4 lối vào nhanh sang các màn còn lại của Medical Staff.
class MedicalStaffDashboard extends StatefulWidget {
  const MedicalStaffDashboard({super.key});

  @override
  State<MedicalStaffDashboard> createState() => _MedicalStaffDashboardState();
}

class _MedicalStaffDashboardState extends State<MedicalStaffDashboard> {
  late Future<MedicalStaffDashboardData> _future;

  @override
  void initState() {
    super.initState();
    _future = MedicalStaffDashboardRepository.instance.load();
  }

  Future<void> _reload() async {
    final next = MedicalStaffDashboardRepository.instance.load();
    setState(() {
      _future = next;
    });
    await next;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Cán Bộ Y Tế'),
        actions: const [AiChatButton(), NotificationBellButton(), ProfileAvatarButton()],
      ),
      body: SafeArea(
        child: FutureBuilder<MedicalStaffDashboardData>(
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
            return RefreshIndicator(
              onRefresh: _reload,
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  _TodayInspectionCard(data: data),
                  const SizedBox(height: 16),
                  _StatsGrid(data: data),
                  const SizedBox(height: 20),
                  const Text('Lối vào nhanh', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const SizedBox(height: 10),
                  _QuickActions(),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}

class _TodayInspectionCard extends StatelessWidget {
  final MedicalStaffDashboardData data;
  const _TodayInspectionCard({required this.data});

  @override
  Widget build(BuildContext context) {
    final ok = (data.todayInspectionScore ?? 0) >= 60;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Kiểm tra bếp hôm nay', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
            const SizedBox(height: 10),
            if (!data.todayInspectionDone)
              Row(
                children: const [
                  Icon(Icons.radio_button_unchecked, color: AppTheme.warningAmber),
                  SizedBox(width: 8),
                  Expanded(child: Text('Chưa kiểm tra hôm nay', style: TextStyle(color: AppTheme.warningAmber))),
                ],
              )
            else
              Row(
                children: [
                  Text(
                    '${data.todayInspectionScore ?? 0}%',
                    style: TextStyle(
                      fontSize: 26,
                      fontWeight: FontWeight.bold,
                      color: ok ? AppTheme.successEmerald : AppTheme.dangerRose,
                    ),
                  ),
                  const Spacer(),
                  Chip(
                    label: Text(ok ? 'Đạt' : 'Không đạt'),
                    backgroundColor: (ok ? AppTheme.successEmerald : AppTheme.dangerRose).withValues(alpha: 0.15),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}

class _StatsGrid extends StatelessWidget {
  final MedicalStaffDashboardData data;
  const _StatsGrid({required this.data});

  @override
  Widget build(BuildContext context) {
    final stats = [
      ('Sự cố đang điều tra', '${data.investigatingIncidentsCount}', Icons.report_problem_outlined, AppTheme.dangerRose),
      ('Khai báo chờ duyệt', '${data.pendingHealthReviewsCount}', Icons.health_and_safety_outlined, AppTheme.warningAmber),
      ('Suất ăn đặc biệt chờ duyệt', '${data.pendingSpecialMealsCount}', Icons.soup_kitchen_outlined, AppTheme.primarySky),
      ('Báo cáo chờ tiếp nhận', '${data.pendingSourceReportsCount}', Icons.mark_email_unread_outlined, AppTheme.primarySky),
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

class _QuickActions extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final actions = <(_QuickActionData, Widget Function())>[
      (
        const _QuickActionData(
          icon: Icons.fact_check_outlined,
          label: 'Kiểm tra an toàn bếp',
          color: AppTheme.primarySky,
        ),
        () => const InspectionScreen(),
      ),
      (
        const _QuickActionData(
          icon: Icons.science_outlined,
          label: 'Sổ lưu mẫu thực phẩm',
          color: AppTheme.successEmerald,
        ),
        () => const SamplesScreen(),
      ),
      (
        const _QuickActionData(
          icon: Icons.report_problem_outlined,
          label: 'Sự cố & Điều tra',
          color: AppTheme.dangerRose,
        ),
        () => const IncidentsScreen(),
      ),
      (
        const _QuickActionData(
          icon: Icons.health_and_safety_outlined,
          label: 'Duyệt khai báo sức khỏe',
          color: AppTheme.warningAmber,
        ),
        () => const HealthReviewsScreen(),
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
