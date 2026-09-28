import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/receiving_models.dart';
import '../data/receiving_repository.dart';
import 'receiving_new_screen.dart';

/// Nhận nguyên liệu (MF06 — UC-044/060/072/123). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/receiving/page.tsx`.
/// Tab "Sắp nhận hàng" = chuyến giao DELIVERED chờ mở phiên; tab "Lịch sử" =
/// phiên nhận đã hoàn tất (BR-151 truy xuất nguồn gốc).
///
/// ⚠️ NGOÀI PHẠM VI (đã grep xác nhận 06/08/2026): `GET /delivery-persons`
/// (`DeliveryPersonController`) chỉ `@PreAuthorize("hasAnyRole('SYSTEM_ADMIN',
/// 'SUPPLIER')")` — SERVICE_CATERERS gọi vào sẽ 403, dù bản Web
/// (`receiving/page.tsx`) vẫn gọi resource này để tra tên tài xế (cùng dạng lỗ
/// hổng role-gating với `partners`/`classrooms` mà Parent/Medical Staff đã ghi
/// nhận). Mobile KHÔNG gọi endpoint này — chỉ hiển thị `driverId` thô, tránh
/// crash toàn màn hình vì 1 lời gọi phụ bị chặn quyền.
class ReceivingScreen extends StatefulWidget {
  const ReceivingScreen({super.key});

  @override
  State<ReceivingScreen> createState() => _ReceivingScreenState();
}

class _ReceivingScreenState extends State<ReceivingScreen> with SingleTickerProviderStateMixin {
  late TabController _tab;
  late Future<List<Delivery>> _upcomingFuture;
  late Future<List<ReceivingSession>> _historyFuture;

  @override
  void initState() {
    super.initState();
    _tab = TabController(length: 2, vsync: this);
    _upcomingFuture = ReceivingRepository.instance.fetchDeliveredDeliveries();
    _historyFuture = ReceivingRepository.instance.fetchHistory();
  }

  @override
  void dispose() {
    _tab.dispose();
    super.dispose();
  }

  Future<void> _reloadUpcoming() async {
    final next = ReceivingRepository.instance.fetchDeliveredDeliveries();
    setState(() {
      _upcomingFuture = next;
    });
    await next;
  }

  Future<void> _reloadHistory() async {
    final next = ReceivingRepository.instance.fetchHistory();
    setState(() {
      _historyFuture = next;
    });
    await next;
  }

  String _driverName(String? id) => id ?? '—';

  /// 13/08/2026 (Nam) — "Xem chi tiết" cho lịch sử nhận hàng. `GET
  /// /receiving-sessions` (dùng ở `_buildHistory`) đã trả sẵn `lines` đầy đủ
  /// cho từng phiên (`ReceivingSession.lines`) — không cần gọi thêm API nào,
  /// chỉ mở lại đúng dữ liệu đã tải.
  void _showSessionDetail(BuildContext context, ReceivingSession s) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        minChildSize: 0.4,
        maxChildSize: 0.95,
        expand: false,
        builder: (ctx, scrollController) => Padding(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 20),
          child: ListView(
            controller: scrollController,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(s.id, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 17)),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                    decoration: BoxDecoration(
                      color: (s.status == 'RECEIVED' ? AppTheme.successEmerald : AppTheme.warningAmber).withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text(s.status ?? '—', style: TextStyle(fontSize: 11, color: s.status == 'RECEIVED' ? AppTheme.successEmerald : AppTheme.warningAmber)),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Text('NCC: ${s.supplier ?? '—'} (${s.supplierStatus ?? '—'})', style: const TextStyle(color: Colors.white70, fontSize: 13)),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(child: _DetailStat(label: 'Ngày nhận', value: s.receivedAt ?? '—')),
                  Expanded(child: _DetailStat(label: 'Người nhận', value: s.receivedBy ?? '—')),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(child: _DetailStat(label: 'Ảnh bằng chứng', value: '${s.photosCount}')),
                  Expanded(
                    child: _DetailStat(
                      label: 'GPS xác thực',
                      value: s.gpsVerified ? 'Có' : 'Không',
                      valueColor: s.gpsVerified ? AppTheme.successEmerald : AppTheme.dangerRose,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              Text('Đối chiếu lô hàng (${s.lines.length})', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
              const SizedBox(height: 8),
              if (s.lines.isEmpty)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 12),
                  child: Text('Không có dòng đối chiếu nào.', style: TextStyle(color: Colors.white54)),
                )
              else
                for (final line in s.lines)
                  Container(
                    margin: const EdgeInsets.only(bottom: 8),
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      border: Border.all(color: line.rejected ? AppTheme.dangerRose.withValues(alpha: 0.4) : AppTheme.borderDark),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Expanded(child: Text(line.ingredient ?? '—', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13))),
                            if (line.rejected)
                              const _Badge2(label: 'Từ chối', color: AppTheme.dangerRose)
                            else
                              const _Badge2(label: 'Đạt', color: AppTheme.successEmerald),
                          ],
                        ),
                        if (line.batchId != null) ...[
                          const SizedBox(height: 2),
                          Text('Lô: ${line.batchId}', style: const TextStyle(fontSize: 11, color: Colors.white54)),
                        ],
                        if (line.rejected && (line.rejectReason ?? '').isNotEmpty) ...[
                          const SizedBox(height: 4),
                          Text('Lý do: ${line.rejectReason}', style: const TextStyle(fontSize: 12, color: AppTheme.dangerRose)),
                        ],
                      ],
                    ),
                  ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Nhận nguyên liệu'),
        bottom: TabBar(controller: _tab, tabs: const [Tab(text: 'Sắp nhận hàng'), Tab(text: 'Lịch sử')]),
      ),
      body: SafeArea(
        child: TabBarView(
          controller: _tab,
          children: [
            _buildUpcoming(),
            _buildHistory(),
          ],
        ),
      ),
    );
  }

  Widget _buildUpcoming() {
    return FutureBuilder<List<Delivery>>(
      future: _upcomingFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          final message = snapshot.error is ApiException ? (snapshot.error as ApiException).message : 'Không tải được dữ liệu.';
          return Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(message, textAlign: TextAlign.center),
                  const SizedBox(height: 12),
                  ElevatedButton(onPressed: _reloadUpcoming, child: const Text('Thử lại')),
                ],
              ),
            ),
          );
        }
        final items = snapshot.data ?? const [];
        if (items.isEmpty) {
          return RefreshIndicator(
            onRefresh: _reloadUpcoming,
            child: ListView(
              padding: const EdgeInsets.all(24),
              children: const [
                SizedBox(height: 80),
                Center(child: Text('Không có chuyến giao nào sắp tới.', style: TextStyle(color: Colors.white54))),
              ],
            ),
          );
        }
        return RefreshIndicator(
          onRefresh: _reloadUpcoming,
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: items.length,
            itemBuilder: (context, index) {
              final d = items[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 10),
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(d.id, style: const TextStyle(fontWeight: FontWeight.w600)),
                      const SizedBox(height: 4),
                      Text('Tài xế: ${_driverName(d.driverId)} · Biển số ${d.vehiclePlate ?? '—'}', style: const TextStyle(fontSize: 12, color: Colors.white70)),
                      Text('Đơn: ${d.purchaseRequestId ?? '—'} · ${d.batches} lô (BR-071)', style: const TextStyle(fontSize: 12, color: Colors.white54)),
                      const SizedBox(height: 10),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          icon: const Icon(Icons.local_shipping_outlined),
                          label: const Text('Mở phiên nhận'),
                          onPressed: () async {
                            await Navigator.of(context).push(MaterialPageRoute(builder: (_) => ReceivingNewScreen(delivery: d)));
                            _reloadUpcoming();
                            _reloadHistory();
                          },
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }

  Widget _buildHistory() {
    return FutureBuilder<List<ReceivingSession>>(
      future: _historyFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          final message = snapshot.error is ApiException ? (snapshot.error as ApiException).message : 'Không tải được dữ liệu.';
          return Center(child: Text(message, textAlign: TextAlign.center));
        }
        final items = snapshot.data ?? const [];
        final received = items.where((s) => s.status == 'RECEIVED').length;
        final rejected = items.where((s) => s.status == 'REJECTED' || s.status == 'RECEIVED_PARTIAL').length;
        return RefreshIndicator(
          onRefresh: _reloadHistory,
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Row(
                children: [
                  Expanded(child: _StatTile(label: 'Thành công', value: '$received', color: AppTheme.successEmerald)),
                  const SizedBox(width: 10),
                  Expanded(child: _StatTile(label: 'Có lô từ chối', value: '$rejected', color: AppTheme.warningAmber)),
                ],
              ),
              const SizedBox(height: 12),
              if (items.isEmpty)
                const Padding(padding: EdgeInsets.only(top: 40), child: Center(child: Text('Chưa có phiên nhận hàng nào.', style: TextStyle(color: Colors.white54)))),
              for (final s in items)
                Card(
                  margin: const EdgeInsets.only(bottom: 10),
                  child: ListTile(
                    onTap: () => _showSessionDetail(context, s),
                    title: Text('${s.id} · ${s.supplier ?? '—'}', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                    subtitle: Text('${s.receivedAt ?? ''} · ${s.items} mặt hàng · ${s.photosCount} ảnh', style: const TextStyle(fontSize: 11)),
                    trailing: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                          decoration: BoxDecoration(
                            color: (s.status == 'RECEIVED' ? AppTheme.successEmerald : AppTheme.warningAmber).withValues(alpha: 0.15),
                            borderRadius: BorderRadius.circular(999),
                          ),
                          child: Text(s.status ?? '—', style: TextStyle(fontSize: 10, color: s.status == 'RECEIVED' ? AppTheme.successEmerald : AppTheme.warningAmber)),
                        ),
                        const SizedBox(width: 4),
                        const Icon(Icons.chevron_right, size: 18, color: Colors.white38),
                      ],
                    ),
                  ),
                ),
            ],
          ),
        );
      },
    );
  }
}

class _DetailStat extends StatelessWidget {
  final String label;
  final String value;
  final Color? valueColor;
  const _DetailStat({required this.label, required this.value, this.valueColor});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 11, color: Colors.white54)),
        const SizedBox(height: 2),
        Text(value, style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600, color: valueColor)),
      ],
    );
  }
}

class _Badge2 extends StatelessWidget {
  final String label;
  final Color color;
  const _Badge2({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(999)),
      child: Text(label, style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w600)),
    );
  }
}

class _StatTile extends StatelessWidget {
  final String label;
  final String value;
  final Color color;
  const _StatTile({required this.label, required this.value, required this.color});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(value, style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: color)),
            const SizedBox(height: 2),
            Text(label, style: const TextStyle(fontSize: 11, color: Colors.white60)),
          ],
        ),
      ),
    );
  }
}
