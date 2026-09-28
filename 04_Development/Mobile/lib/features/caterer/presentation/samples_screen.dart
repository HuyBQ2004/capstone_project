import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/inspection_models.dart';
import '../data/sample_models.dart';
import '../data/sample_repository.dart';
import 'samples_register_screen.dart';

/// UC-069 — Lưu mẫu thực phẩm (Mẫu số 4 & 5, QĐ 1246/QĐ-BYT). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/samples/page.tsx`.
/// BR-105: chỉ lấy mẫu sau khi phiên kiểm thực PASSED cả 3 bước · BR-203:
/// ≥100g (đặc) / ≥150ml (lỏng) · BR-110: bảo quản 2–8°C (cảnh báo mềm) ·
/// BR-107: ảnh bằng chứng bắt buộc, RIÊNG TỪNG MẪU (sửa 10/08/2026, Nam:
/// "mỗi mẫu 1 ảnh", không dùng chung ảnh cho cả lô đã chọn) · BR-111: hạn lưu
/// tối thiểu 24h. Trang đăng ký nay LUÔN tiếp nối trực tiếp từ 1 phiên kiểm
/// thực cụ thể (`RegisterSamplesForSessionScreen`) — nút "+" ở đây chỉ là lối
/// tắt chọn nhanh phiên rồi điều hướng sang đó.
class SamplesScreen extends StatefulWidget {
  const SamplesScreen({super.key});

  @override
  State<SamplesScreen> createState() => _SamplesScreenState();
}

class _SamplesScreenState extends State<SamplesScreen> {
  late Future<List<FoodSample>> _future;

  @override
  void initState() {
    super.initState();
    _future = SampleRepository.instance.fetchSamples();
  }

  Future<void> _reload() async {
    final next = SampleRepository.instance.fetchSamples();
    setState(() {
      _future = next;
    });
    await next;
  }

  Future<void> _openSessionPicker() async {
    List<InspectionSession> sessions;
    try {
      sessions = await SampleRepository.instance.fetchPassedSessions();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
      return;
    }
    if (!mounted) return;
    if (sessions.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Chưa có phiên kiểm thực Đạt cả 3 bước (BR-105).')),
      );
      return;
    }
    final chosen = await showDialog<InspectionSession>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('Chọn phiên kiểm thực để lưu mẫu'),
        children: [
          for (final s in sessions)
            SimpleDialogOption(
              onPressed: () => Navigator.pop(ctx, s),
              child: Text('${s.id} · ${mealTypeLabel[s.meal] ?? s.meal} ${s.date ?? ''}'),
            ),
        ],
      ),
    );
    if (chosen == null || !mounted) return;
    await Navigator.of(context).push(MaterialPageRoute(builder: (_) => RegisterSamplesForSessionScreen(session: chosen)));
    _reload();
  }

  /// 13/08/2026 (Nam) — "Xem chi tiết" một mẫu lưu. Dùng lại đúng dữ liệu đã
  /// tải ở danh sách (`FoodSample` đã đủ field), không gọi thêm API.
  void _showSampleDetail(BuildContext context, FoodSample s) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => DraggableScrollableSheet(
        initialChildSize: 0.6,
        minChildSize: 0.35,
        maxChildSize: 0.9,
        expand: false,
        builder: (ctx, scrollController) => Padding(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 20),
          child: ListView(
            controller: scrollController,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(child: Text(s.id, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 17))),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                    decoration: BoxDecoration(color: AppTheme.primarySky.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(999)),
                    child: Text(sampleStatusLabel(s.status), style: const TextStyle(color: AppTheme.primarySky, fontSize: 12)),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Text(s.dish ?? '—', style: const TextStyle(color: Colors.white70, fontSize: 14)),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(child: _SampleStat(label: 'Bữa', value: s.mealShift ?? sampleMealTypeLabel[s.mealType] ?? s.meal ?? '—')),
                  Expanded(child: _SampleStat(label: 'Số suất', value: '${s.servings ?? '—'}')),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(child: _SampleStat(label: 'Khối lượng/thể tích', value: s.weight ?? '—')),
                  Expanded(child: _SampleStat(label: 'Dụng cụ lưu', value: s.container ?? '—')),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: _SampleStat(
                      label: 'Nhiệt độ tủ lưu',
                      value: s.fridgeTemp == null ? '—' : '${s.fridgeTemp}°C',
                      valueColor: s.tempOutOfRange ? AppTheme.dangerRose : null,
                    ),
                  ),
                  Expanded(
                    child: _SampleStat(
                      label: 'Hạn lưu còn lại',
                      value: (s.hoursRemaining ?? 0) > 0 ? '${s.hoursRemaining} giờ' : 'Đã hết hạn tối thiểu',
                      valueColor: (s.hoursRemaining ?? 0) <= 8 ? AppTheme.warningAmber : null,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              _SampleStat(label: 'Vị trí bảo quản', value: s.storageLocation ?? '—'),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(child: _SampleStat(label: 'Thời điểm lấy mẫu', value: s.collectedAt ?? '—')),
                  Expanded(child: _SampleStat(label: 'Người lấy mẫu', value: s.collectedBy ?? '—')),
                ],
              ),
              if (s.inspectionId != null) ...[
                const SizedBox(height: 8),
                _SampleStat(label: 'Phiên kiểm thực gốc', value: s.inspectionId!),
              ],
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
        title: const Text('Lưu mẫu thực phẩm'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            tooltip: 'Đăng ký lưu mẫu',
            onPressed: _openSessionPicker,
          ),
        ],
      ),
      body: SafeArea(
        child: FutureBuilder<List<FoodSample>>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              final message =
                  snapshot.error is ApiException ? (snapshot.error as ApiException).message : 'Không tải được dữ liệu.';
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
            final items = snapshot.data ?? const [];
            if (items.isEmpty) {
              return RefreshIndicator(
                onRefresh: _reload,
                child: ListView(
                  padding: const EdgeInsets.all(24),
                  children: const [
                    SizedBox(height: 80),
                    Center(child: Text('Chưa có mẫu lưu nào.', style: TextStyle(color: Colors.white54))),
                  ],
                ),
              );
            }
            return RefreshIndicator(
              onRefresh: _reload,
              child: ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: items.length,
                itemBuilder: (context, index) {
                  final s = items[index];
                  final hours = s.hoursRemaining ?? 0;
                  final tempOk = !s.tempOutOfRange;
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: InkWell(
                      borderRadius: BorderRadius.circular(12),
                      onTap: () => _showSampleDetail(context, s),
                      child: Padding(
                        padding: const EdgeInsets.all(14),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Expanded(child: Text('${s.id} · ${s.dish ?? '—'}', style: const TextStyle(fontWeight: FontWeight.w600))),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                  decoration: BoxDecoration(color: AppTheme.primarySky.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(999)),
                                  child: Text(sampleStatusLabel(s.status), style: const TextStyle(color: AppTheme.primarySky, fontSize: 11)),
                                ),
                                const SizedBox(width: 4),
                                const Icon(Icons.chevron_right, size: 18, color: Colors.white38),
                              ],
                            ),
                            const SizedBox(height: 6),
                            Text('${s.mealShift ?? sampleMealTypeLabel[s.mealType] ?? s.meal ?? '—'} · ${s.weight ?? '—'}', style: const TextStyle(fontSize: 12, color: Colors.white70)),
                            const SizedBox(height: 4),
                            Row(
                              children: [
                                Text(
                                  hours > 0 ? 'Còn $hours h trong hạn lưu 24h' : 'Hết hạn lưu tối thiểu',
                                  style: TextStyle(fontSize: 11, color: hours <= 8 && hours > 0 ? AppTheme.warningAmber : Colors.white54),
                                ),
                                const SizedBox(width: 10),
                                Text('Tủ lưu ${s.fridgeTemp ?? '—'}°C', style: TextStyle(fontSize: 11, color: tempOk ? Colors.white54 : AppTheme.dangerRose)),
                              ],
                            ),
                            Text('Vị trí: ${s.storageLocation ?? '—'} · Người lấy: ${s.collectedBy ?? '—'}', style: const TextStyle(fontSize: 11, color: Colors.white38)),
                          ],
                        ),
                      ),
                    ),
                  );
                },
              ),
            );
          },
        ),
      ),
    );
  }
}

class _SampleStat extends StatelessWidget {
  final String label;
  final String value;
  final Color? valueColor;
  const _SampleStat({required this.label, required this.value, this.valueColor});

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
