import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/inspection_models.dart' show mealTypeLabel;
import '../data/issue_models.dart';
import '../data/issue_repository.dart';

String _todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

String _todayVn() {
  final parts = _todayIso().split('-');
  return '${parts[2]}/${parts[1]}/${parts[0]}';
}

String _fmtQty(double v) {
  if (v == v.roundToDouble()) return v.toInt().toString();
  return v.toStringAsFixed(2);
}

/// Xuất kho theo bữa (FEFO, BR-075/077) — MF06. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inventory/issue/page.tsx`.
///
/// 13/08/2026 (Nam) — mobile trước đây KHÔNG có màn này, bếp phải mở web để xuất kho. Trả
/// lời đúng 2 việc Nam yêu cầu: (1) xem hôm nay bữa nào cần nấu món gì, bao nhiêu suất
/// (`planned-dishes`, số suất PENDING thật sẽ phát — kể cả suất thay thế do dị ứng), và
/// (2) xuất kho — hệ thống tự gộp nhu cầu nguyên liệu, phân bổ lô theo FEFO (hạn gần nhất
/// trước), rồi xác nhận một lần cho cả bữa.
class IssueScreen extends StatefulWidget {
  const IssueScreen({super.key});

  @override
  State<IssueScreen> createState() => _IssueScreenState();
}

class _IssueScreenState extends State<IssueScreen> {
  final _dateIso = _todayIso();
  bool _loadingMenus = true;
  String? _menusError;
  List<IssueMenu> _dayMenus = [];
  Map<String, IssueDish> _dishesById = {};

  String? _mealType;
  bool _loadingPlan = false;
  String? _planError;
  List<IssuePlannedDish> _plannedDishes = [];
  IssuePlan? _plan;
  bool _submitting = false;

  @override
  void initState() {
    super.initState();
    _loadMenus();
  }

  Future<void> _loadMenus() async {
    setState(() {
      _loadingMenus = true;
      _menusError = null;
    });
    try {
      final results = await Future.wait([
        IssueRepository.instance.fetchMenus(),
        IssueRepository.instance.fetchDishesById(),
      ]);
      final menus = results[0] as List<IssueMenu>;
      setState(() {
        _dayMenus = menus.where((m) => m.menuDateIso == _dateIso).toList();
        _dishesById = results[1] as Map<String, IssueDish>;
      });
    } on ApiException catch (e) {
      setState(() => _menusError = e.message);
    } catch (_) {
      setState(() => _menusError = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loadingMenus = false);
    }
  }

  List<String> get _dayMeals {
    final set = <String>{};
    for (final m in _dayMenus) set.addAll(m.mealTypes);
    // Thứ tự cố định trong ngày, không theo thứ tự trả về của server.
    return ['BREAKFAST', 'LUNCH', 'SNACK'].where(set.contains).toList();
  }

  List<IssueMenu> get _mealMenus =>
      _mealType == null ? [] : _dayMenus.where((m) => m.mealTypes.contains(_mealType)).toList();

  Future<void> _selectMeal(String mealType) async {
    setState(() {
      _mealType = mealType;
      _plannedDishes = [];
      _plan = null;
      _planError = null;
      _loadingPlan = true;
    });
    try {
      final planned = await IssueRepository.instance.fetchPlannedDishes(date: _dateIso, mealType: mealType);
      setState(() => _plannedDishes = planned);

      // Gộp nhu cầu nguyên liệu từ suất THẬT sẽ phát (không phải kế hoạch tĩnh của thực đơn) —
      // KHÔNG quy đổi đơn vị (xem javadoc đầu file `issue_models.dart`).
      final demand = <String, double>{};
      for (final pd in planned) {
        final dish = pd.dishId == null ? null : _dishesById[pd.dishId];
        if (dish == null) continue;
        for (final ing in dish.ingredients) {
          if (ing.ingredientId == null || ing.ingredientId!.isEmpty) continue; // ngoài danh mục (gia vị)
          final needed = ing.quantity * pd.count;
          if (needed <= 0) continue;
          demand[ing.ingredientId!] = (demand[ing.ingredientId!] ?? 0) + needed;
        }
      }

      if (demand.isNotEmpty) {
        final plan = await IssueRepository.instance.fetchIssuePlan(date: _dateIso, mealType: mealType, demand: demand);
        setState(() => _plan = plan);
      }
    } on ApiException catch (e) {
      setState(() => _planError = e.message);
    } catch (_) {
      setState(() => _planError = 'Không tải được dữ liệu bữa này. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loadingPlan = false);
    }
  }

  /// Món ĐẦU TIÊN dùng nguyên liệu này — gán cho dòng phiếu xuất (xấp xỉ đã biết, giống bản
  /// web — `IssuePlanLine` server trả PHẲNG theo nguyên liệu, không có breakdown theo món).
  String _firstDishIdFor(String? ingredientId) {
    for (final pd in _plannedDishes) {
      final dish = pd.dishId == null ? null : _dishesById[pd.dishId];
      if (dish == null) continue;
      if (dish.ingredients.any((i) => i.ingredientId == ingredientId)) return pd.dishId ?? '';
    }
    return '';
  }

  Future<void> _submit() async {
    final plan = _plan;
    final mealType = _mealType;
    if (plan == null || mealType == null) return;
    final issuableLines = <Map<String, dynamic>>[];
    for (final line in plan.lines) {
      for (final a in line.allocation) {
        issuableLines.add({
          'batchId': a.batchId ?? '',
          'dishId': _firstDishIdFor(line.ingredientId),
          'quantity': a.take,
          'unit': line.unit,
        });
      }
    }
    if (issuableLines.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Không có lô khả dụng nào để xuất cho buổi ăn này.')),
      );
      return;
    }
    setState(() => _submitting = true);
    try {
      await IssueRepository.instance.createIssueSlip(
        date: _dateIso,
        mealType: mealType,
        menuIds: _mealMenus.map((m) => m.id).toList(),
        lines: issuableLines,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Đã xuất ${issuableLines.length} dòng cho ${mealTypeLabel[mealType] ?? mealType} ${_todayVn()} (BR-075).'),
            backgroundColor: AppTheme.successEmerald,
          ),
        );
        setState(() {
          _plan = null;
          _plannedDishes = [];
          _mealType = null;
        });
      }
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Không xuất được kho — thử lại sau.')));
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Xuất kho theo bữa')),
      body: SafeArea(
        child: _loadingMenus
            ? const Center(child: CircularProgressIndicator())
            : _menusError != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(_menusError!, textAlign: TextAlign.center),
                          const SizedBox(height: 12),
                          ElevatedButton(onPressed: _loadMenus, child: const Text('Thử lại')),
                        ],
                      ),
                    ),
                  )
                : RefreshIndicator(
                    onRefresh: _loadMenus,
                    child: ListView(
                      padding: const EdgeInsets.all(16),
                      children: [
                        Text('Hôm nay — ${_todayVn()}', style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                        const SizedBox(height: 4),
                        const Text(
                          'Chỉ xuất được cho ngày hôm nay — bếp xuất nguyên liệu để nấu ngay, không xuất trước/xuất bù.',
                          style: TextStyle(fontSize: 12, color: Colors.white54),
                        ),
                        const SizedBox(height: 16),
                        if (_dayMeals.isEmpty)
                          const _WarningBox(text: 'Chưa có thực đơn đã duyệt (PUBLISHED) cho hôm nay — chưa có gì để xuất kho.')
                        else ...[
                          const Text('Chọn buổi ăn', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
                          const SizedBox(height: 8),
                          Wrap(
                            spacing: 8,
                            children: [
                              for (final m in _dayMeals)
                                ChoiceChip(
                                  label: Text(mealTypeLabel[m] ?? m),
                                  selected: _mealType == m,
                                  onSelected: (_) => _selectMeal(m),
                                ),
                            ],
                          ),
                        ],
                        if (_mealType != null) ...[
                          const SizedBox(height: 20),
                          if (_loadingPlan)
                            const Padding(padding: EdgeInsets.symmetric(vertical: 24), child: Center(child: CircularProgressIndicator()))
                          else if (_planError != null)
                            _WarningBox(text: _planError!, color: AppTheme.dangerRose)
                          else ...[
                            _PlannedDishesCard(dishes: _plannedDishes),
                            const SizedBox(height: 16),
                            if (_plan != null) _IssuePlanCard(plan: _plan!),
                            const SizedBox(height: 20),
                            if (_plannedDishes.isNotEmpty)
                              SizedBox(
                                width: double.infinity,
                                child: ElevatedButton.icon(
                                  onPressed: _submitting || _plan == null || _plan!.unknownIngredients.isNotEmpty ? null : _submit,
                                  icon: _submitting
                                      ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                                      : const Icon(Icons.outbox_outlined),
                                  label: Text(_submitting ? 'Đang xuất…' : 'Xác nhận xuất kho'),
                                ),
                              ),
                          ],
                        ],
                      ],
                    ),
                  ),
      ),
    );
  }
}

class _WarningBox extends StatelessWidget {
  final String text;
  final Color color;
  const _WarningBox({required this.text, this.color = AppTheme.warningAmber});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        border: Border.all(color: color.withValues(alpha: 0.3)),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(Icons.info_outline, size: 18, color: color),
          const SizedBox(width: 8),
          Expanded(child: Text(text, style: TextStyle(color: color, fontSize: 13))),
        ],
      ),
    );
  }
}

/// "Xem ngày nào cần nấu gì, bao nhiêu suất" — đúng số suất PENDING thật sẽ phát.
class _PlannedDishesCard extends StatelessWidget {
  final List<IssuePlannedDish> dishes;
  const _PlannedDishesCard({required this.dishes});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.soup_kitchen_outlined, size: 18, color: AppTheme.primarySky),
                SizedBox(width: 8),
                Text('Cần nấu gì hôm nay', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              ],
            ),
            const SizedBox(height: 10),
            if (dishes.isEmpty)
              const Text('Chưa có học sinh nào đang chờ phát suất (PENDING) cho bữa này.', style: TextStyle(color: Colors.white54, fontSize: 12))
            else
              for (final d in dishes)
                Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: Row(
                    children: [
                      Expanded(child: Text(d.dishName ?? '—', style: const TextStyle(fontSize: 13))),
                      Text('${d.count} suất', style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
                      if (d.substituteCount > 0) ...[
                        const SizedBox(width: 6),
                        _MiniBadge(label: 'thay thế ×${d.substituteCount}', color: AppTheme.successEmerald),
                      ],
                      if (d.blockedCount > 0) ...[
                        const SizedBox(width: 6),
                        _MiniBadge(label: 'chặn ×${d.blockedCount}', color: AppTheme.dangerRose),
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

class _IssuePlanCard extends StatelessWidget {
  final IssuePlan plan;
  const _IssuePlanCard({required this.plan});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Row(
                  children: [
                    Icon(Icons.inventory_2_outlined, size: 18, color: AppTheme.primarySky),
                    SizedBox(width: 8),
                    Expanded(
                      child: Text('Định mức & phân bổ FEFO (hạn gần nhất trước)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                if (plan.lines.isEmpty)
                  const Text('Không có nguyên liệu nào cần xuất.', style: TextStyle(color: Colors.white54, fontSize: 12))
                else
                  for (final l in plan.lines)
                    Container(
                      margin: const EdgeInsets.only(bottom: 8),
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        border: Border.all(color: l.shortfall > 0 ? AppTheme.warningAmber.withValues(alpha: 0.5) : AppTheme.borderDark),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Expanded(child: Text(l.ingredientName, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13))),
                              Text('${_fmtQty(l.needed)} ${l.unit}', style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
                            ],
                          ),
                          if (l.allocation.isNotEmpty) ...[
                            const SizedBox(height: 6),
                            Wrap(
                              spacing: 6,
                              runSpacing: 4,
                              children: [
                                for (final a in l.allocation)
                                  _MiniBadge(label: '${a.batchId} · ${_fmtQty(a.take)} ${l.unit} · HSD ${a.expiryDate ?? '—'}', color: AppTheme.primarySky),
                              ],
                            ),
                          ],
                          const SizedBox(height: 6),
                          if (l.shortfall > 0)
                            _MiniBadge(label: 'Thiếu ${_fmtQty(l.shortfall)} ${l.unit}', color: AppTheme.warningAmber)
                          else
                            const _MiniBadge(label: 'Đủ', color: AppTheme.successEmerald),
                        ],
                      ),
                    ),
              ],
            ),
          ),
        ),
        if (plan.unknownIngredients.isNotEmpty) ...[
          const SizedBox(height: 12),
          _WarningBox(
            color: AppTheme.dangerRose,
            text: '${plan.unknownIngredients.join(", ")} — chưa có trong danh mục nguyên liệu nên không gắn được lô (BR-076). Bổ sung vào danh mục trước khi xuất kho.',
          ),
        ],
        if (plan.shortages.isNotEmpty) ...[
          const SizedBox(height: 12),
          _WarningBox(
            text: 'Thiếu tồn kho (BR-077): ${plan.shortages.map((s) => '${s.ingredientName} thiếu ${_fmtQty(s.shortfall)} ${s.unit}').join('; ')}. '
                'Các dòng đủ tồn vẫn xuất được ngay — phần thiếu cần đặt hàng bổ sung ở web.',
          ),
        ],
      ],
    );
  }
}

class _MiniBadge extends StatelessWidget {
  final String label;
  final Color color;
  const _MiniBadge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(999)),
      child: Text(label, style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.w600)),
    );
  }
}
