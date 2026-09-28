import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../ai_chat/presentation/ai_chat_button.dart';
import '../../notifications/presentation/notification_bell_button.dart';
import '../../profile/presentation/profile_avatar_button.dart';
import '../data/dashboard_models.dart';
import '../data/dashboard_repository.dart';
import 'adjustment_screen.dart';
import 'class_deliveries_screen.dart';
import 'inspections_screen.dart';
import 'issue_screen.dart';
import 'kitchen_inspections_screen.dart';
import 'receiving_screen.dart';
import 'samples_screen.dart';

/// Dashboard Đơn vị Suất ăn — đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/dashboard/page.tsx`
/// (rút gọn cho mobile: bỏ biểu đồ 7 ngày + bảng kho theo trạng thái lô, giữ
/// đúng 3 chỉ số hàng ngày caterer cần thao tác tại hiện trường + 4 lối vào
/// nhanh). Thay bản mock cũ (dialog xác thực khuôn mặt giả lập) — xác thực
/// khuôn mặt tài xế THẬT nay nằm trong luồng Nhận nguyên liệu (BR-050/051,
/// đúng chỗ nó xảy ra trong nghiệp vụ — xem `receiving_new_screen.dart`).
class CatererDashboard extends StatefulWidget {
  const CatererDashboard({super.key});

  @override
  State<CatererDashboard> createState() => _CatererDashboardState();
}

class _CatererDashboardState extends State<CatererDashboard> {
  late Future<CatererDashboardData> _future;

  @override
  void initState() {
    super.initState();
    _future = CatererDashboardRepository.instance.load();
  }

  Future<void> _reload() async {
    final next = CatererDashboardRepository.instance.load();
    setState(() {
      _future = next;
    });
    await next;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Đơn Vị Suất Ăn'),
        actions: const [AiChatButton(), NotificationBellButton(), ProfileAvatarButton()],
      ),
      body: SafeArea(
        child: FutureBuilder<CatererDashboardData>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              final message =
                  snapshot.error is ApiException ? (snapshot.error as ApiException).message : 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.';
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
                  const _QuickActions(),
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
  final CatererDashboardData data;
  const _TodayInspectionCard({required this.data});

  @override
  Widget build(BuildContext context) {
    final overall = data.todayInspectionOverall;
    final ok = overall == 'PASSED';
    final failed = overall == 'FAILED';
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Kiểm thực ba bước hôm nay', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
            const SizedBox(height: 10),
            if (data.todayInspectionCount == 0)
              Row(
                children: const [
                  Icon(Icons.radio_button_unchecked, color: AppTheme.warningAmber),
                  SizedBox(width: 8),
                  Expanded(child: Text('Chưa mở phiên kiểm thực nào hôm nay', style: TextStyle(color: AppTheme.warningAmber))),
                ],
              )
            else
              Row(
                children: [
                  Icon(
                    failed ? Icons.error_outline : (ok ? Icons.check_circle : Icons.hourglass_bottom),
                    color: failed ? AppTheme.dangerRose : (ok ? AppTheme.successEmerald : AppTheme.primarySky),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      failed
                          ? 'Có bước Không đạt — đang chặn phục vụ (BR-094)'
                          : ok
                              ? '${data.todayInspectionCount} phiên — tất cả Đạt'
                              : '${data.todayInspectionCount} phiên đang thực hiện',
                      style: TextStyle(
                        fontWeight: FontWeight.w600,
                        color: failed ? AppTheme.dangerRose : (ok ? AppTheme.successEmerald : AppTheme.primarySky),
                      ),
                    ),
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
  final CatererDashboardData data;
  const _StatsGrid({required this.data});

  @override
  Widget build(BuildContext context) {
    final stats = [
      ('Mẫu đang lưu', '${data.retainedSamplesCount}', Icons.science_outlined, AppTheme.primarySky),
      ('Chuyến hàng chờ nhận', '${data.dueDeliveriesCount}', Icons.local_shipping_outlined, AppTheme.warningAmber),
      ('Phiên nhận hôm nay', '${data.receivedTodayCount}', Icons.inventory_2_outlined, AppTheme.successEmerald),
      ('Bước kiểm thực Không đạt', '${data.failedInspectionCount}', Icons.warning_amber_rounded, AppTheme.dangerRose),
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
  const _QuickActions();

  @override
  Widget build(BuildContext context) {
    final actions = <(_QuickActionData, Widget Function())>[
      (
        const _QuickActionData(icon: Icons.fact_check_outlined, label: 'Kiểm thực ba bước', color: AppTheme.primarySky),
        () => const InspectionsScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.science_outlined, label: 'Lưu mẫu thực phẩm', color: AppTheme.successEmerald),
        () => const SamplesScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.local_shipping_outlined, label: 'Nhận nguyên liệu', color: AppTheme.warningAmber),
        () => const ReceivingScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.outbox_outlined, label: 'Xuất kho theo bữa', color: AppTheme.successEmerald),
        () => const IssueScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.delivery_dining_outlined, label: 'Giao suất tới lớp', color: AppTheme.dangerRose),
        () => const ClassDeliveriesScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.checklist_rtl, label: 'Kiểm tra vệ sinh bếp', color: AppTheme.primarySky),
        () => const KitchenInspectionsScreen(),
      ),
      (
        const _QuickActionData(icon: Icons.tune, label: 'Điều chỉnh tồn kho', color: AppTheme.warningAmber),
        () => const AdjustmentScreen(),
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
          _QuickActionCard(data: data, onTap: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => builder()))),
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
