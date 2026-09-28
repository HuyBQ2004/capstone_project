import 'package:flutter/material.dart';

import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/child_models.dart';
import '../data/child_repository.dart';
import '../data/menu_models.dart';
import '../data/menu_repository.dart';
import 'widgets/child_selector.dart';

/// 02_WeeklyMenu.md — UC-135 (MSG-004/005/051). Gộp "Thực đơn tuần" + "Bữa ăn
/// hôm nay" vào một màn (đối chiếu 2 trang bản web `parent/weekly-menu` và
/// `parent/school-meals` — trang sau chỉ là dashboard tóm tắt của trang trước,
/// không có nghiệp vụ riêng); ngày hôm nay được ghim lên đầu danh sách và tô
/// nổi bật cho hợp thao tác một tay trên điện thoại.
class WeeklyMenuScreen extends StatefulWidget {
  const WeeklyMenuScreen({super.key});

  @override
  State<WeeklyMenuScreen> createState() => _WeeklyMenuScreenState();
}

class _WeeklyMenuScreenState extends State<WeeklyMenuScreen> {
  List<ParentChild> _children = [];
  ParentChild? _active;
  List<ParentMenu> _menus = [];
  bool _loading = true;
  String? _error;

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
      final children = await ParentChildRepository.instance.fetchChildren();
      final active = ParentChildRepository.instance.activeChildOf(children);
      final menus = active == null
          ? <ParentMenu>[]
          : await ParentMenuRepository.instance.fetchPublishedForGrade(active.grade, active.id);
      setState(() {
        _children = children;
        _active = active;
        _menus = menus;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _selectChild(String studentId) async {
    ParentChildRepository.instance.setActiveChild(studentId);
    await _load();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Thực đơn tuần')),
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
                : RefreshIndicator(
                    onRefresh: _load,
                    child: ListView(
                      padding: const EdgeInsets.all(16),
                      children: [
                        if (_children.isEmpty)
                          const Text('Tài khoản chưa liên kết học sinh nào.', style: TextStyle(color: Colors.white54))
                        else ...[
                          ChildSelector(children: _children, active: _active, onSelect: _selectChild),
                          const SizedBox(height: 12),
                          Text(
                            '${_active?.name ?? '—'} · Lớp ${_active?.classroomId ?? '—'} · ${_active?.grade ?? '—'}',
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'Món có allergen của con sẽ được cảnh báo đỏ (BR-063).',
                            style: const TextStyle(color: Colors.white60, fontSize: 12),
                          ),
                          const SizedBox(height: 16),
                          if (_menus.isEmpty)
                            Padding(
                              padding: const EdgeInsets.symmetric(vertical: 32),
                              child: Text(
                                'Chưa có thực đơn nào được công bố cho ${_active?.grade ?? 'khối này'}.',
                                textAlign: TextAlign.center,
                                style: const TextStyle(color: Colors.white54),
                              ),
                            )
                          else
                            ..._menus.map((m) => _MenuDayCard(menu: m, allergies: _active?.allergies ?? const [])),
                        ],
                      ],
                    ),
                  ),
      ),
    );
  }
}

class _MenuDayCard extends StatelessWidget {
  final ParentMenu menu;
  final List<String> allergies;
  const _MenuDayCard({required this.menu, required this.allergies});

  bool get _isToday {
    final d = DateTime.now();
    String p(int n) => n.toString().padLeft(2, '0');
    final today = '${d.year}-${p(d.month)}-${p(d.day)}';
    return menu.menuDateIso == today;
  }

  bool _conflicts(MenuDish dish) {
    if (allergies.isEmpty) return false;
    return dish.manualAllergens.any(
      (a) => allergies.any((v) => a.toLowerCase().contains(v.toLowerCase()) || v.toLowerCase().contains(a.toLowerCase())),
    );
  }

  /// 13/08/2026 (Nam) — "Xem chi tiết món" (nguyên liệu + ảnh nếu bếp đã tải lên + dinh
  /// dưỡng) khi bấm vào 1 món trong thực đơn tuần. `MenuDish` (dòng trong thực đơn) chỉ có
  /// tên/kcal rút gọn — gọi thêm `GET /dishes/{id}` lấy bản đầy đủ.
  void _showDishDetail(BuildContext context, String dishId, String? fallbackName) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (ctx) => DraggableScrollableSheet(
        initialChildSize: 0.6,
        minChildSize: 0.35,
        maxChildSize: 0.92,
        expand: false,
        builder: (ctx, scrollController) => FutureBuilder<DishDetail>(
          future: ParentMenuRepository.instance.fetchDishDetail(dishId),
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: Padding(padding: EdgeInsets.all(32), child: CircularProgressIndicator()));
            }
            if (snapshot.hasError || !snapshot.hasData) {
              return Padding(
                padding: const EdgeInsets.all(24),
                child: Text(
                  snapshot.error is ApiException ? (snapshot.error as ApiException).message : 'Không tải được chi tiết món ${fallbackName ?? ''}.',
                  textAlign: TextAlign.center,
                ),
              );
            }
            return _DishDetailSheet(dish: snapshot.data!, scrollController: scrollController);
          },
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: _isToday ? AppTheme.primarySky : AppTheme.borderDark, width: _isToday ? 1.5 : 1),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    menu.week ?? menu.menuDateIso ?? '—',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                  ),
                ),
                if (_isToday)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                    decoration: BoxDecoration(
                      color: AppTheme.primarySky.withValues(alpha: 0.18),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const Text('Hôm nay', style: TextStyle(color: AppTheme.primarySky, fontSize: 11, fontWeight: FontWeight.w600)),
                  ),
                const SizedBox(width: 6),
                Text('${menu.totalKcal} kcal', style: const TextStyle(color: Colors.white38, fontSize: 11)),
              ],
            ),
            const SizedBox(height: 10),
            for (final block in menu.blocks) ...[
              Text(
                mealTypeLabel[block.mealType] ?? block.mealType,
                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: Colors.white70),
              ),
              const SizedBox(height: 4),
              for (final dish in block.dishes)
                InkWell(
                  onTap: dish.dishId == null ? null : () => _showDishDetail(context, dish.dishId!, dish.name),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(vertical: 3),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Icon(
                          _conflicts(dish) ? Icons.warning_amber_rounded : Icons.circle,
                          size: _conflicts(dish) ? 14 : 6,
                          color: _conflicts(dish) ? AppTheme.dangerRose : AppTheme.primarySky,
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            dish.name ?? '—',
                            style: TextStyle(
                              fontSize: 13,
                              color: _conflicts(dish) ? AppTheme.dangerRose : Colors.white,
                              fontWeight: _conflicts(dish) ? FontWeight.w600 : FontWeight.normal,
                            ),
                          ),
                        ),
                        if (dish.dishId != null)
                          const Icon(Icons.chevron_right, size: 16, color: Colors.white38),
                      ],
                    ),
                  ),
                ),
              const SizedBox(height: 8),
            ],
          ],
        ),
      ),
    );
  }
}

const _categoryLabel = {
  'MON_CHINH': 'Món chính',
  'MON_MAN': 'Món mặn',
  'CANH': 'Canh',
  'RAU': 'Rau',
  'BUA_PHU': 'Bữa phụ',
  'TRANG_MIENG': 'Tráng miệng',
};

class _DishDetailSheet extends StatelessWidget {
  final DishDetail dish;
  final ScrollController scrollController;
  const _DishDetailSheet({required this.dish, required this.scrollController});

  @override
  Widget build(BuildContext context) {
    return ListView(
      controller: scrollController,
      padding: EdgeInsets.zero,
      children: [
        // Ảnh minh hoạ — chỉ hiện khi bếp đã tải lên (`POST /dishes/{id}/image`); món chưa
        // có ảnh thì bỏ qua hẳn khối này, không hiện khung trống/placeholder xám.
        if ((dish.imageUrl ?? '').isNotEmpty)
          ClipRRect(
            borderRadius: const BorderRadius.only(topLeft: Radius.circular(20), topRight: Radius.circular(20)),
            child: AspectRatio(
              aspectRatio: 16 / 9,
              child: Image.network(
                ApiConstants.resolveMediaUrl(dish.imageUrl!),
                fit: BoxFit.cover,
                loadingBuilder: (context, child, progress) =>
                    progress == null ? child : const Center(child: CircularProgressIndicator()),
                errorBuilder: (context, error, stack) => Container(
                  color: AppTheme.cardDark,
                  alignment: Alignment.center,
                  child: const Icon(Icons.image_not_supported_outlined, color: Colors.white24, size: 32),
                ),
              ),
            ),
          ),
        Padding(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(dish.name ?? '—', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
              if (dish.category != null) ...[
                const SizedBox(height: 4),
                Text(_categoryLabel[dish.category] ?? dish.category!, style: const TextStyle(color: Colors.white54, fontSize: 12)),
              ],
              if ((dish.description ?? '').isNotEmpty) ...[
                const SizedBox(height: 10),
                Text(dish.description!, style: const TextStyle(fontSize: 13, height: 1.4, color: Colors.white70)),
              ],
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(child: _NutriStat(label: 'Kcal', value: dish.kcal)),
                  Expanded(child: _NutriStat(label: 'Đạm (g)', value: dish.protein)),
                  Expanded(child: _NutriStat(label: 'Béo (g)', value: dish.fat)),
                  Expanded(child: _NutriStat(label: 'Bột đường (g)', value: dish.carb)),
                ],
              ),
              if (dish.manualAllergens.isNotEmpty) ...[
                const SizedBox(height: 16),
                const Text('Dị nguyên (BR-063)', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                const SizedBox(height: 6),
                Wrap(
                  spacing: 6,
                  runSpacing: 6,
                  children: [
                    for (final a in dish.manualAllergens)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                        decoration: BoxDecoration(color: AppTheme.dangerRose.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(999)),
                        child: Text(a, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 11, fontWeight: FontWeight.w600)),
                      ),
                  ],
                ),
              ],
              const SizedBox(height: 16),
              Text('Nguyên liệu (${dish.ingredients.length})', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
              const SizedBox(height: 6),
              if (dish.ingredients.isEmpty)
                const Text('Chưa khai nguyên liệu cho món này.', style: TextStyle(color: Colors.white54, fontSize: 12))
              else
                for (final ing in dish.ingredients)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 4),
                    child: Row(
                      children: [
                        const Icon(Icons.circle, size: 5, color: AppTheme.primarySky),
                        const SizedBox(width: 8),
                        Expanded(child: Text(ing.name, style: const TextStyle(fontSize: 13))),
                        if (ing.quantity != null)
                          Text('${ing.quantity} ${ing.unit ?? ''}', style: const TextStyle(fontSize: 12, color: Colors.white54)),
                      ],
                    ),
                  ),
            ],
          ),
        ),
      ],
    );
  }
}

class _NutriStat extends StatelessWidget {
  final String label;
  final String? value;
  const _NutriStat({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(value ?? '—', style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
        Text(label, style: const TextStyle(fontSize: 10, color: Colors.white54)),
      ],
    );
  }
}
