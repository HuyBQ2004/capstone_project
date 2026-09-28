import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/class_delivery_models.dart';
import '../data/class_delivery_repository.dart';
import '../data/classroom_models.dart';
import '../data/classroom_repository.dart';
import '../data/distribution_models.dart';
import '../data/distribution_repository.dart';
import 'class_deliveries_screen.dart';

String _todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

const _mealDistributionMealLabel = {
  'BREAKFAST': 'Bữa sáng',
  'LUNCH': 'Bữa trưa',
  'SNACK': 'Bữa chiều',
};
const _tabs = [
  ('all', 'Tất cả'),
  ('pending', 'Chờ phát'),
  ('served', 'Đã phục vụ'),
];

/// Phân phát bữa ăn — checklist học sinh + cảnh báo dị ứng (BR-120/BR-123).
/// Đối chiếu `04_Development/Frontend/src/app/(app)/teacher/distribution/page.tsx`
/// và `.../distribution/[id]/page.tsx`. `POST /student-distributions/confirm`
/// là MỘT transaction cho mọi học sinh đã chọn — không có PUT/PATCH.
class MealDistributionScreen extends StatefulWidget {
  const MealDistributionScreen({super.key});

  @override
  State<MealDistributionScreen> createState() => _MealDistributionScreenState();
}

class _MealDistributionScreenState extends State<MealDistributionScreen> {
  Classroom? _classroom;
  List<Distribution> _distributions = [];
  PlannedDishes? _planned;
  String? _mealType;
  bool _loading = true;
  String? _error;
  String _tab = 'all';
  bool _selectMode = false;
  final Set<String> _selected = {};
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final classroom = await ClassroomRepository.instance.resolveMyClassroom();
      _classroom = classroom;
      if (classroom == null) {
        setState(() {
          _error = 'Không tìm thấy lớp chủ nhiệm của tài khoản này.';
          _loading = false;
        });
        return;
      }
      final receivedDelivery = await ClassDeliveryRepository.instance.fetchReceivedToday(
        date: _todayIso(),
        classroomId: classroom.id,
      );
      final mealType = receivedDelivery?.mealType;
      if (mealType == null) {
        setState(() {
          _mealType = null;
          _distributions = [];
          _planned = null;
        });
        return;
      }
      final distributions = await DistributionRepository.instance.fetchForClassDay(
        date: _todayIso(),
        classroomId: classroom.id,
        mealType: mealType,
      );
      final planned = await ClassDeliveryRepository.instance.fetchPlannedDishes(
        date: _todayIso(),
        mealType: mealType,
        classroomId: classroom.id,
      );
      setState(() {
        _mealType = mealType;
        _distributions = distributions;
        _planned = planned;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  bool _isBlocked(String? studentId) =>
      (_planned?.perStudent ?? const <PlannedDishRow>[]).any((p) => p.studentId == studentId && p.blocked);

  /// 13/08/2026 (Nam) — danh sách món DỰ KIẾN của đúng học sinh này, dùng khi phiên còn
  /// PENDING (chưa phát, chưa có `servedDishes` — field đó chỉ đóng băng SAU khi phát,
  /// ADR-M09-1). Đã tải sẵn từ `_planned` cho việc chặn dị ứng (`_isBlocked`) — chỉ thiếu
  /// hiển thị ra thẻ học sinh.
  List<PlannedDishRow> _plannedDishesFor(String? studentId) =>
      (_planned?.perStudent ?? const <PlannedDishRow>[]).where((p) => p.studentId == studentId).toList();

  List<Distribution> get _filtered {
    switch (_tab) {
      case 'pending':
        return _distributions.where((d) => d.isPending).toList();
      case 'served':
        return _distributions.where((d) => d.isServed).toList();
      default:
        return _distributions;
    }
  }

  void _exitSelectMode() {
    setState(() {
      _selectMode = false;
      _selected.clear();
    });
  }

  void _toggle(Distribution d) {
    if (!d.isPending) return;
    if (_isBlocked(d.studentId)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Học sinh này dị ứng trùng món nhưng CHƯA có món thay thế cấu hình — cần Y tế/nhà bếp xử lý tay '
            'trước khi phát (BR-065).',
          ),
        ),
      );
      return;
    }
    setState(() {
      if (_selected.contains(d.id)) {
        _selected.remove(d.id);
      } else {
        _selected.add(d.id);
      }
    });
  }

  /*
   * 13/08/2026 — `DistributionRepository.reportError()` (UC-016 AF1, BR-065) đã tồn tại từ
   * trước nhưng chưa màn nào gọi tới: giáo viên phát nhầm suất (khác dị ứng/nguội/thiếu...)
   * trước đây chỉ có "Hủy phát" chứ không có cách báo cho bếp biết để cấp lại đúng suất.
   */
  Future<void> _reportError(Distribution d) async {
    final reasonCtrl = TextEditingController();
    final reason = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.cardDark,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Text('Báo lỗi suất ăn — ${d.name ?? d.studentId ?? ''}'),
        content: TextField(
          controller: reasonCtrl,
          autofocus: true,
          maxLines: 3,
          decoration: const InputDecoration(
            hintText: 'VD: phát nhầm suất dị ứng, thiếu món, suất bị đổ...',
            border: OutlineInputBorder(),
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Hủy')),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppTheme.dangerRose),
            onPressed: () => Navigator.pop(ctx, reasonCtrl.text.trim()),
            child: const Text('Gửi báo lỗi'),
          ),
        ],
      ),
    );
    if (reason == null || reason.isEmpty) return;

    try {
      await DistributionRepository.instance.reportError(d.id, reason);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Đã báo lỗi suất ăn của ${d.name ?? d.studentId} tới bếp (BR-065).')),
        );
      }
      _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không gửi được báo lỗi, thử lại sau.')),
        );
      }
    }
  }

  Future<void> _revoke(Distribution d) async {
    try {
      await DistributionRepository.instance.revoke(d.id);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Đã hủy đánh dấu phát cho ${d.name ?? d.studentId}.')),
        );
      }
      _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không thể hủy đánh dấu phát.')),
        );
      }
    }
  }

  Future<void> _openConfirmDialog() async {
    final selectedRows = _distributions.where((d) => _selected.contains(d.id)).toList();
    final withAllergy = selectedRows.where((d) => d.hasAllergyWarning).toList();

    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.cardDark,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: withAllergy.isEmpty
            ? Text('Lưu phân phát cho ${selectedRows.length} học sinh?')
            : const Row(
                children: [
                  Icon(Icons.warning_amber_rounded, color: AppTheme.warningAmber, size: 26),
                  SizedBox(width: 8),
                  Expanded(
                    child: Text('CẢNH BÁO DỊ ỨNG (BR-123)', style: TextStyle(fontSize: 16, color: AppTheme.warningAmber)),
                  ),
                ],
              ),
        content: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                '${selectedRows.length} học sinh sẽ được đánh dấu đã phát suất cùng lúc.',
                style: const TextStyle(color: Colors.white70),
              ),
              if (withAllergy.isNotEmpty) ...[
                const SizedBox(height: 12),
                const Text(
                  'Trong đó có học sinh dị ứng — kiểm tra kỹ suất thay thế trước khi phát!',
                  style: TextStyle(color: AppTheme.warningAmber, fontWeight: FontWeight.bold, fontSize: 13),
                ),
                const SizedBox(height: 6),
                for (final d in withAllergy)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 4),
                    child: Text(
                      '• ${d.name ?? d.studentId}: ${d.allergyWarning.join(", ")}',
                      style: const TextStyle(color: Colors.white70, fontSize: 13),
                    ),
                  ),
              ],
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Hủy')),
          ElevatedButton(
            style: withAllergy.isNotEmpty
                ? ElevatedButton.styleFrom(backgroundColor: AppTheme.warningAmber)
                : null,
            onPressed: () => Navigator.pop(ctx, true),
            child: Text(withAllergy.isNotEmpty ? 'Đã Kiểm Tra & Phân Phát' : 'Lưu phân phát'),
          ),
        ],
      ),
    );

    if (confirmed != true) return;
    setState(() => _saving = true);
    try {
      await DistributionRepository.instance.confirmBatch(_selected.toList());
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Đã lưu phân phát cho ${selectedRows.length} học sinh.')),
        );
      }
      _exitSelectMode();
      _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không thể lưu phân phát, thử lại sau.')),
        );
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Phân phát bữa ăn'),
        actions: [
          if (_selectMode)
            TextButton(
              onPressed: _exitSelectMode,
              child: const Text('Hủy', style: TextStyle(color: Colors.white70)),
            )
          else
            IconButton(
              onPressed: () => setState(() => _selectMode = true),
              icon: const Icon(Icons.checklist),
              tooltip: 'Bắt đầu phân phát',
            ),
        ],
      ),
      floatingActionButton: _selectMode && _selected.isNotEmpty
          ? FloatingActionButton.extended(
              onPressed: _saving ? null : _openConfirmDialog,
              icon: _saving
                  ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.check_circle),
              label: Text('Lưu phân phát (${_selected.length})'),
            )
          : null,
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(_error!, textAlign: TextAlign.center),
                          const SizedBox(height: 12),
                          ElevatedButton(onPressed: _load, child: const Text('Thử lại')),
                        ],
                      ),
                    ),
                  )
                : _mealType == null
                    ? Center(
                        child: Padding(
                          padding: const EdgeInsets.all(24),
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(Icons.info_outline, color: AppTheme.warningAmber, size: 32),
                              const SizedBox(height: 12),
                              const Text(
                                'Chưa có bữa nào được bếp giao và lớp xác nhận "Đã nhận" hôm nay.',
                                textAlign: TextAlign.center,
                                style: TextStyle(color: Colors.white70),
                              ),
                              const SizedBox(height: 12),
                              OutlinedButton(
                                onPressed: () async {
                                  await Navigator.of(context)
                                      .push(MaterialPageRoute(builder: (_) => const ClassDeliveriesScreen()));
                                  _load();
                                },
                                child: const Text('Nhận suất ăn từ bếp'),
                              ),
                              const SizedBox(height: 8),
                              TextButton(onPressed: _load, child: const Text('Tải lại')),
                            ],
                          ),
                        ),
                      )
                    : Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
                        child: Text(
                          'Lớp ${_classroom?.id ?? '—'} · ${_todayIso()}${_mealDistributionMealLabel[_mealType] != null ? ' · ${_mealDistributionMealLabel[_mealType]}' : ''}',
                          style: const TextStyle(color: Colors.white60, fontSize: 13),
                        ),
                      ),
                      Padding(
                        padding: const EdgeInsets.all(12),
                        child: Wrap(
                          spacing: 8,
                          children: [
                            for (final t in _tabs)
                              ChoiceChip(
                                label: Text(t.$2),
                                selected: _tab == t.$1,
                                onSelected: (_) => setState(() => _tab = t.$1),
                              ),
                          ],
                        ),
                      ),
                      Expanded(
                        child: _filtered.isEmpty
                            ? const Center(
                                child: Text('Chưa có phiên phân phát bữa ăn hôm nay.', style: TextStyle(color: Colors.white54)),
                              )
                            : RefreshIndicator(
                                onRefresh: _load,
                                child: ListView.builder(
                                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 96),
                                  itemCount: _filtered.length,
                                  itemBuilder: (context, index) {
                                    final d = _filtered[index];
                                    return _DistributionCard(
                                      distribution: d,
                                      selectMode: _selectMode,
                                      selected: _selected.contains(d.id),
                                      blocked: d.isPending && _isBlocked(d.studentId),
                                      plannedDishes: d.isPending ? _plannedDishesFor(d.studentId) : const [],
                                      onToggle: () => _toggle(d),
                                      onRevoke: () => _revoke(d),
                                      onReportError: () => _reportError(d),
                                    );
                                  },
                                ),
                              ),
                      ),
                    ],
                  ),
      ),
    );
  }
}

class _DistributionCard extends StatelessWidget {
  final Distribution distribution;
  final bool selectMode;
  final bool selected;
  final bool blocked;
  /// Món DỰ KIẾN — chỉ có ý nghĩa khi `distribution.isPending` (xem javadoc
  /// `_plannedDishesFor`); rỗng khi đã phát vì lúc đó dùng `distribution.servedDishes`.
  final List<PlannedDishRow> plannedDishes;
  final VoidCallback onToggle;
  final VoidCallback onRevoke;
  final VoidCallback onReportError;

  const _DistributionCard({
    required this.distribution,
    required this.selectMode,
    required this.selected,
    required this.blocked,
    required this.plannedDishes,
    required this.onToggle,
    required this.onRevoke,
    required this.onReportError,
  });

  @override
  Widget build(BuildContext context) {
    final d = distribution;
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: selectMode && d.isPending ? onToggle : null,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              CircleAvatar(
                backgroundColor: d.hasAllergyWarning ? Colors.amber[900] : AppTheme.primarySky,
                child: Icon(
                  d.hasAllergyWarning ? Icons.warning_rounded : Icons.person,
                  color: d.hasAllergyWarning ? Colors.amber : Colors.white,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(d.name ?? d.studentId ?? '—', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                    const SizedBox(height: 2),
                    Text('Mã HS: ${d.studentId ?? '—'}', style: const TextStyle(color: Colors.white60, fontSize: 12)),
                    if (d.hasAllergyWarning) ...[
                      const SizedBox(height: 4),
                      Text(
                        '⚠ Dị ứng: ${d.allergyWarning.join(", ")}',
                        style: const TextStyle(color: AppTheme.warningAmber, fontWeight: FontWeight.bold, fontSize: 12),
                      ),
                    ],
                    if (d.specialDiet != null && d.specialDiet!.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      _Badge(label: d.specialDiet!, color: AppTheme.primarySky),
                    ],
                    const SizedBox(height: 6),
                    // 13/08/2026 (Nam) — danh sách món cần/đã phát cho ĐÚNG học sinh này.
                    // Đã phát dùng `servedDishes` (đóng băng lúc phát, ADR-M09-1); chưa phát
                    // dùng `plannedDishes` (dự kiến, suy từ luật thay món hiện hành — có thể
                    // đổi cho tới lúc thật sự bấm Lưu phân phát).
                    if (d.isServed && d.servedDishes.isNotEmpty)
                      _DishList(
                        names: [for (final sd in d.servedDishes) (name: sd.dishName ?? '—', isSubstitute: sd.isSubstitute)],
                      )
                    else if (!d.isServed && plannedDishes.isNotEmpty)
                      _DishList(
                        names: [for (final pd in plannedDishes) (name: pd.dishName ?? '—', isSubstitute: pd.isSubstitute)],
                      ),
                    const SizedBox(height: 6),
                    if (d.isServed)
                      Row(
                        children: [
                          const _Badge(label: 'Đã phục vụ', color: AppTheme.successEmerald),
                          if (d.servedAt != null) ...[
                            const SizedBox(width: 6),
                            Text(d.servedAt!, style: const TextStyle(color: Colors.white38, fontSize: 11)),
                          ],
                        ],
                      )
                    else if (blocked)
                      const _Badge(label: 'Cần xử lý tay (BR-065)', color: AppTheme.dangerRose)
                    else
                      const _Badge(label: 'Chờ phát', color: Colors.white38),
                  ],
                ),
              ),
              if (!selectMode && d.isServed)
                Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    TextButton(onPressed: onRevoke, child: const Text('Hủy phát')),
                    TextButton(
                      style: TextButton.styleFrom(foregroundColor: AppTheme.dangerRose),
                      onPressed: onReportError,
                      child: const Text('Báo lỗi'),
                    ),
                  ],
                )
              else if (selectMode && d.isPending && !blocked)
                Checkbox(value: selected, onChanged: (_) => onToggle()),
            ],
          ),
        ),
      ),
    );
  }
}

class _DishList extends StatelessWidget {
  final List<({String name, bool isSubstitute})> names;
  const _DishList({required this.names});

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 6,
      runSpacing: 4,
      children: [
        for (final d in names)
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
            decoration: BoxDecoration(
              color: (d.isSubstitute ? AppTheme.successEmerald : Colors.white).withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(999),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.restaurant, size: 11, color: d.isSubstitute ? AppTheme.successEmerald : Colors.white54),
                const SizedBox(width: 4),
                Text(
                  d.name,
                  style: TextStyle(fontSize: 11, color: d.isSubstitute ? AppTheme.successEmerald : Colors.white70),
                ),
                if (d.isSubstitute) ...[
                  const SizedBox(width: 3),
                  const Text('(thay thế)', style: TextStyle(fontSize: 10, color: AppTheme.successEmerald, fontStyle: FontStyle.italic)),
                ],
              ],
            ),
          ),
      ],
    );
  }
}

class _Badge extends StatelessWidget {
  final String label;
  final Color color;
  const _Badge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(label, style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w600)),
    );
  }
}
