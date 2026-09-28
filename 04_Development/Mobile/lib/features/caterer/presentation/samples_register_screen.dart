import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/inspection_models.dart';
import '../data/sample_models.dart';
import '../data/sample_repository.dart';

const _containers = [
  'Hộp inox có nắp kín',
  'Hộp thủy tinh có nắp',
  'Hộp nhựa PP có nắp kín',
  'Túi vô trùng chuyên dụng',
];

class _RowState {
  String kind = 'solid'; // solid | liquid
  final amountCtrl = TextEditingController(text: '120');
  String container = _containers.first;
  bool sanitized = true;
  bool sealed = true;
  final List<XFile> photos = [];
  bool submitting = false;

  void dispose() => amountCtrl.dispose();
}

/// Đăng ký lưu mẫu — TIẾP NỐI TRỰC TIẾP từ 1 phiên kiểm thực đã Đạt (BR-105).
/// Đối chiếu `04_Development/Frontend/src/app/(app)/service-caterers/samples/register/[sessionId]/page.tsx`
/// (sửa 10/08/2026, Nam): mỗi món cần lấy mẫu có field + ẢNH RIÊNG (không dùng
/// chung 1 bộ ảnh cho cả lô như bản cũ) — mỗi mẫu 1 ảnh.
class RegisterSamplesForSessionScreen extends StatefulWidget {
  final InspectionSession session;
  const RegisterSamplesForSessionScreen({super.key, required this.session});

  @override
  State<RegisterSamplesForSessionScreen> createState() => _RegisterSamplesForSessionScreenState();
}

class _RegisterSamplesForSessionScreenState extends State<RegisterSamplesForSessionScreen> {
  bool _loading = true;
  String? _error;
  List<DishChoice> _dishOptions = [];
  final Map<String, FoodSample> _registeredByDish = {};
  final Map<String, _RowState> _rows = {};

  final _collectedTimeCtrl = TextEditingController(text: '10:45');
  final _storageLocationCtrl = TextEditingController(text: 'Tủ lưu mẫu số 1');
  final _storageTempCtrl = TextEditingController(text: '4.2');
  DateTime? _collectedAt;

  @override
  void initState() {
    super.initState();
    _collectedAt = DateTime.now();
    _load();
  }

  @override
  void dispose() {
    _collectedTimeCtrl.dispose();
    _storageLocationCtrl.dispose();
    _storageTempCtrl.dispose();
    for (final r in _rows.values) r.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final dishes = SampleRepository.instance.dishChoicesOf(widget.session);
      final samples = await SampleRepository.instance.fetchSamples();
      final registered = <String, FoodSample>{};
      for (final s in samples) {
        if (s.inspectionId == widget.session.id && s.dishId != null) registered[s.dishId!] = s;
      }
      final rows = <String, _RowState>{};
      for (final d in dishes) {
        if (!registered.containsKey(d.dishId)) rows[d.dishId] = _RowState();
      }
      setState(() {
        _dishOptions = dishes;
        _registeredByDish
          ..clear()
          ..addAll(registered);
        _rows
          ..clear()
          ..addAll(rows);
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được danh sách món cần lấy mẫu.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _pickCollectedAt() async {
    final now = DateTime.now();
    final date = await showDatePicker(context: context, initialDate: now, firstDate: now.subtract(const Duration(days: 1)), lastDate: now.add(const Duration(days: 1)));
    if (date == null || !mounted) return;
    final time = await showTimePicker(context: context, initialTime: TimeOfDay.fromDateTime(now));
    if (time == null) return;
    setState(() => _collectedAt = DateTime(date.year, date.month, date.day, time.hour, time.minute));
  }

  /*
   * 13/08/2026 — trước đây `pickMultiImage()` cho chọn ảnh có sẵn trong thư viện, lách được
   * yêu cầu chụp trực tiếp (BR-107 — ảnh bằng chứng mẫu lưu phải chụp tại chỗ). Đổi sang
   * camera, mỗi lần bấm chụp 1 ảnh; bấm nút nhiều lần để thêm ảnh cho cùng một mẫu.
   */
  Future<void> _pickPhotos(String dishId) async {
    final photo = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 85);
    if (photo == null) return;
    setState(() => _rows[dishId]?.photos.add(photo));
  }

  bool _rowValid(_RowState row) {
    final amount = double.tryParse(row.amountCtrl.text.trim()) ?? 0;
    if (row.kind == 'solid' && amount < 100) return false;
    if (row.kind == 'liquid' && amount < 150) return false;
    if (!row.sanitized || !row.sealed) return false;
    if (row.photos.isEmpty) return false;
    return true;
  }

  Future<void> _submitDish(String dishId, DishChoice dish) async {
    final row = _rows[dishId];
    if (row == null) return;
    if (!_rowValid(row)) {
      String msg;
      final amount = double.tryParse(row.amountCtrl.text.trim()) ?? 0;
      if (row.kind == 'solid' && amount < 100) {
        msg = 'Thức ăn đặc phải lưu tối thiểu 100g (BR-203, Điều 7.2a).';
      } else if (row.kind == 'liquid' && amount < 150) {
        msg = 'Thức ăn lỏng phải lưu tối thiểu 150ml (BR-203, Điều 7.2b).';
      } else if (!row.sanitized) {
        msg = 'Dụng cụ phải được rửa sạch, tiệt trùng trước khi dùng (Điều 6.2).';
      } else if (!row.sealed) {
        msg = 'Mỗi món phải lưu vào dụng cụ riêng và niêm phong (Điều 7.1).';
      } else {
        msg = 'Ảnh bằng chứng riêng của mẫu "${dish.name}" là bắt buộc (BR-107).';
      }
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg), backgroundColor: AppTheme.dangerRose));
      return;
    }
    if (_collectedAt == null || _storageLocationCtrl.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Thời gian lấy mẫu và vị trí lưu mẫu là bắt buộc.'), backgroundColor: AppTheme.dangerRose),
      );
      return;
    }

    setState(() => row.submitting = true);
    final mealLabel = mealTypeLabel[widget.session.meal] ?? widget.session.meal ?? '';
    try {
      final created = await SampleRepository.instance.register(
        inspectionId: widget.session.id,
        dishId: dish.dishId,
        mealType: widget.session.meal ?? '',
        mealShift: '$mealLabel — ${_collectedTimeCtrl.text.trim()}',
        servings: int.tryParse(dish.servings) ?? 0,
        amount: double.tryParse(row.amountCtrl.text.trim()) ?? 0,
        unit: row.kind == 'solid' ? 'G' : 'ML',
        container: row.container,
        sanitized: row.sanitized,
        sealed: row.sealed,
        fridgeTemp: double.tryParse(_storageTempCtrl.text.trim()) ?? 4.0,
        collectedAtIso: _collectedAt!.toUtc().toIso8601String(),
        storageLocation: _storageLocationCtrl.text.trim(),
        evidencePhotoPaths: row.photos.map((f) => f.path).toList(),
      );
      if (!mounted) return;
      setState(() {
        _registeredByDish[dishId] = created;
        _rows.remove(dishId);
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Đã lưu mẫu "${dish.name}".'), backgroundColor: AppTheme.successEmerald),
      );
      if (_rows.isEmpty && mounted) Navigator.pop(context);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Không đăng ký được mẫu "${dish.name}".'), backgroundColor: AppTheme.dangerRose),
        );
      }
    } finally {
      if (mounted) setState(() => row.submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final s = widget.session;
    return Scaffold(
      appBar: AppBar(title: Text('Lưu mẫu · ${mealTypeLabel[s.meal] ?? s.meal ?? ''} ${s.date ?? ''}')),
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
                : ListView(
                    padding: const EdgeInsets.all(16),
                    children: [
                      Card(
                        child: Padding(
                          padding: const EdgeInsets.all(14),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Text('Thông tin chung (áp dụng cho mọi mẫu của phiên)',
                                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                              const SizedBox(height: 10),
                              TextField(
                                controller: _collectedTimeCtrl,
                                decoration: const InputDecoration(labelText: 'Giờ ăn (HH:mm)', border: OutlineInputBorder()),
                              ),
                              const SizedBox(height: 10),
                              ListTile(
                                contentPadding: EdgeInsets.zero,
                                title: const Text('Thời gian lấy mẫu *', style: TextStyle(fontSize: 13)),
                                subtitle: Text(_collectedAt == null ? 'Chưa chọn' : _collectedAt.toString()),
                                trailing: const Icon(Icons.schedule),
                                onTap: _pickCollectedAt,
                              ),
                              TextField(
                                controller: _storageLocationCtrl,
                                decoration: const InputDecoration(labelText: 'Vị trí lưu mẫu *', border: OutlineInputBorder()),
                              ),
                              const SizedBox(height: 10),
                              TextField(
                                controller: _storageTempCtrl,
                                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                                decoration: const InputDecoration(labelText: 'Nhiệt độ bảo quản °C (2–8°C, BR-110)', border: OutlineInputBorder()),
                              ),
                            ],
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),
                      const Text('Món cần lấy mẫu', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                      const SizedBox(height: 8),
                      if (_dishOptions.isEmpty)
                        const Padding(
                          padding: EdgeInsets.symmetric(vertical: 16),
                          child: Text('Chưa có món nào trong phiên này.', style: TextStyle(color: Colors.white54)),
                        ),
                      for (final d in _dishOptions)
                        _registeredByDish.containsKey(d.dishId)
                            ? _RegisteredDishCard(dish: d, sample: _registeredByDish[d.dishId]!)
                            : _DishRowCard(
                                dish: d,
                                row: _rows[d.dishId]!,
                                onChanged: () => setState(() {}),
                                onPickPhotos: () => _pickPhotos(d.dishId),
                                onSubmit: () => _submitDish(d.dishId, d),
                              ),
                    ],
                  ),
      ),
    );
  }
}

class _RegisteredDishCard extends StatelessWidget {
  final DishChoice dish;
  final FoodSample sample;
  const _RegisteredDishCard({required this.dish, required this.sample});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        title: Text(dish.name, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text('Đã lấy mẫu — ${sample.id}', style: const TextStyle(color: AppTheme.successEmerald, fontSize: 12)),
        trailing: const Icon(Icons.check_circle, color: AppTheme.successEmerald),
      ),
    );
  }
}

class _DishRowCard extends StatelessWidget {
  final DishChoice dish;
  final _RowState row;
  final VoidCallback onChanged;
  final VoidCallback onPickPhotos;
  final VoidCallback onSubmit;

  const _DishRowCard({
    required this.dish,
    required this.row,
    required this.onChanged,
    required this.onPickPhotos,
    required this.onSubmit,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${dish.name} (${dish.servings.isEmpty ? '?' : dish.servings} suất)',
                style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
            const SizedBox(height: 10),
            DropdownButtonFormField<String>(
              initialValue: row.kind,
              decoration: const InputDecoration(labelText: 'Loại món', border: OutlineInputBorder(), isDense: true),
              dropdownColor: AppTheme.cardDark,
              items: const [
                DropdownMenuItem(value: 'solid', child: Text('Thức ăn đặc — tối thiểu 100g')),
                DropdownMenuItem(value: 'liquid', child: Text('Thức ăn lỏng — tối thiểu 150ml')),
              ],
              onChanged: (v) {
                if (v != null) {
                  row.kind = v;
                  onChanged();
                }
              },
            ),
            const SizedBox(height: 10),
            TextField(
              controller: row.amountCtrl,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: 'Khối lượng (${row.kind == 'solid' ? 'gam' : 'ml'}) *',
                border: const OutlineInputBorder(),
                isDense: true,
              ),
            ),
            const SizedBox(height: 10),
            DropdownButtonFormField<String>(
              initialValue: row.container,
              decoration: const InputDecoration(labelText: 'Dụng cụ chứa mẫu *', border: OutlineInputBorder(), isDense: true),
              dropdownColor: AppTheme.cardDark,
              items: _containers.map((c) => DropdownMenuItem(value: c, child: Text(c, overflow: TextOverflow.ellipsis))).toList(),
              onChanged: (v) {
                if (v != null) {
                  row.container = v;
                  onChanged();
                }
              },
            ),
            CheckboxListTile(
              dense: true,
              contentPadding: EdgeInsets.zero,
              title: const Text('Dụng cụ đã rửa sạch, tiệt trùng * (Điều 6.2)', style: TextStyle(fontSize: 12)),
              value: row.sanitized,
              onChanged: (v) {
                row.sanitized = v ?? true;
                onChanged();
              },
            ),
            CheckboxListTile(
              dense: true,
              contentPadding: EdgeInsets.zero,
              title: const Text('Dụng cụ riêng, đã niêm phong * (Điều 7.1)', style: TextStyle(fontSize: 12)),
              value: row.sealed,
              onChanged: (v) {
                row.sealed = v ?? true;
                onChanged();
              },
            ),
            const SizedBox(height: 6),
            OutlinedButton.icon(
              onPressed: onPickPhotos,
              icon: const Icon(Icons.camera_alt_outlined),
              label: Text('Ảnh riêng của mẫu này * (${row.photos.length})'),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: row.submitting ? null : onSubmit,
                child: row.submitting
                    ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                    : const Text('Lưu mẫu này'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
