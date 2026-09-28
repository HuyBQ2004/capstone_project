import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/inspection_models.dart';
import '../data/inspection_repository.dart';
import 'inspection_history_screen.dart';

class _Evidence {
  /// 13/08/2026 — trước đây chỉ có `int photos` (bộ đếm tăng bằng tay, không có ảnh nào
  /// tồn tại thật), khiến nhân viên bấm tăng số đếm vô tội vạ mà không hề chụp gì — làm
  /// rỗng ý nghĩa bắt buộc ảnh khi "Không đạt" (BR-207). Nay lưu path ảnh THẬT chụp qua
  /// camera (`ImageSource.camera`, cùng cách các màn khác trong app), tải lên đúng hạng
  /// mục sau khi tạo phiên thành công (`POST /kitchen-inspections/{code}/evidences/{itemCode}`,
  /// V68 — đối chiếu bản Web `conduct-inspection/page.tsx`).
  List<String> photoPaths;
  String reason;
  _Evidence({List<String>? photoPaths, this.reason = ''}) : photoPaths = photoPaths ?? [];
  int get photos => photoPaths.length;
}

/// 12_ConductFoodSafetyInspectionScreen.md — UC-014. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/conduct-inspection/page.tsx`.
/// Wizard 1-cột: mỗi nhóm checklist một trang, đánh giá Đạt/Không đạt theo
/// từng hạng mục kèm ảnh minh chứng + lý do bắt buộc khi Không đạt (BR-207).
/// Checklist ĐỘNG nạp từ `checklist-groups` (BR-210) — quản lý CRUD checklist
/// (UC-186) là việc cấu hình ít dùng hàng ngày, không port sang mobile (xem
/// TASKS_MOBILE_SCOPE.md); Medical Staff vẫn chỉnh checklist qua bản Web.
class InspectionScreen extends StatefulWidget {
  const InspectionScreen({super.key});

  @override
  State<InspectionScreen> createState() => _InspectionScreenState();
}

class _InspectionScreenState extends State<InspectionScreen> {
  bool _loading = true;
  String? _loadError;
  List<ChecklistGroup> _groups = [];
  int _groupIndex = 0;
  final Map<String, bool?> _checks = {};
  final Map<String, _Evidence> _evidence = {};
  bool _submitting = false;
  KitchenInspection? _lastResult;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _loadError = null;
    });
    try {
      final groups = await InspectionRepository.instance.fetchChecklistGroups();
      setState(() => _groups = groups);
    } on ApiException catch (e) {
      setState(() => _loadError = e.message);
    } catch (_) {
      setState(() => _loadError = 'Không tải được checklist. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  List<String> get _allItemIds => _groups.expand((g) => g.items.map((i) => i.id)).toList();

  int get _answeredCount => _allItemIds.where((i) => _checks[i] != null).length;

  int get _passedCount => _allItemIds.where((i) => _checks[i] == true).length;

  int get _score => _allItemIds.isEmpty ? 0 : ((_passedCount / _allItemIds.length) * 100).round();

  List<String> get _failedItems => _allItemIds.where((i) => _checks[i] == false).toList();

  List<String> get _itemsMissingEvidence => _failedItems
      .where((i) => (_evidence[i]?.photos ?? 0) == 0 || (_evidence[i]?.reason ?? '').trim().isEmpty)
      .toList();

  bool get _submitDisabled =>
      _allItemIds.isEmpty ||
      _answeredCount < _allItemIds.length ||
      _itemsMissingEvidence.isNotEmpty ||
      _submitting;

  void _setCheck(String item, bool value) {
    setState(() => _checks[item] = value);
  }

  Future<void> _addPhoto(String item) async {
    final photo = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 85);
    if (photo == null || !mounted) return;
    setState(() {
      final ev = _evidence[item] ?? _Evidence();
      ev.photoPaths.add(photo.path);
      _evidence[item] = ev;
    });
  }

  void _setReason(String item, String reason) {
    final ev = _evidence[item] ?? _Evidence();
    ev.reason = reason;
    _evidence[item] = ev;
  }

  Future<void> _submit() async {
    if (_submitDisabled) return;
    setState(() => _submitting = true);
    try {
      final itemResults = _allItemIds
          .map((item) => {
                'item': item,
                'passed': _checks[item] == true,
                if ((_evidence[item]?.photos ?? 0) > 0) 'photos': _evidence[item]!.photos,
                if ((_evidence[item]?.reason ?? '').trim().isNotEmpty)
                  'reason': _evidence[item]!.reason.trim(),
              })
          .toList();
      final result = await InspectionRepository.instance.submit(itemResults: itemResults);
      // V68 — ảnh gắn ĐÚNG hạng mục, tải lên SAU khi có mã phiên server sinh; lỗi tải
      // từng ảnh không chặn việc đã nộp checklist thành công.
      for (final item in _allItemIds) {
        for (final path in _evidence[item]?.photoPaths ?? const <String>[]) {
          try {
            await InspectionRepository.instance.uploadEvidence(result.id, item, path);
          } catch (_) {
            // Bỏ qua — ảnh lỗi không chặn kết quả checklist đã lưu.
          }
        }
      }
      setState(() {
        _lastResult = result;
        _checks.clear();
        _evidence.clear();
        _groupIndex = 0;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              (result.score ?? 0) >= 60
                  ? 'Đã hoàn thành phiên kiểm tra (điểm ${result.score}%) — chờ bếp xác nhận kết quả.'
                  : 'Điểm ${result.score}% dưới 60% — đã lưu kết quả Không đạt, đã cảnh báo Ban giám hiệu.',
            ),
            backgroundColor: (result.score ?? 0) >= 60 ? AppTheme.successEmerald : AppTheme.warningAmber,
          ),
        );
      }
    } on ApiException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Không nộp được phiên kiểm tra, thử lại sau.')),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Kiểm tra an toàn bếp'),
        actions: [
          IconButton(
            icon: const Icon(Icons.history),
            tooltip: 'Lịch sử kiểm tra',
            onPressed: () =>
                Navigator.of(context).push(MaterialPageRoute(builder: (_) => const InspectionHistoryScreen())),
          ),
        ],
      ),
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : _loadError != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(_loadError!, textAlign: TextAlign.center),
                          const SizedBox(height: 12),
                          ElevatedButton(onPressed: _load, child: const Text('Thử lại')),
                        ],
                      ),
                    ),
                  )
                : _groups.isEmpty
                    ? const Center(
                        child: Padding(
                          padding: EdgeInsets.all(24),
                          child: Text(
                            'Chưa có nhóm checklist nào — thiết lập tại bản Web (UC-186).',
                            textAlign: TextAlign.center,
                            style: TextStyle(color: Colors.white54),
                          ),
                        ),
                      )
                    : ListView(
                        padding: const EdgeInsets.all(16),
                        children: [
                          if (_lastResult != null) _ResultBanner(result: _lastResult!),
                          _ProgressCard(
                            answered: _answeredCount,
                            total: _allItemIds.length,
                            score: _score,
                            failedMissingEvidence: _itemsMissingEvidence.length,
                          ),
                          const SizedBox(height: 16),
                          _GroupSelector(
                            groups: _groups,
                            index: _groupIndex,
                            checks: _checks,
                            onChanged: (i) => setState(() => _groupIndex = i),
                          ),
                          const SizedBox(height: 12),
                          for (final item in _groups[_groupIndex].items)
                            _ChecklistItemCard(
                              item: item,
                              value: _checks[item.id],
                              evidence: _evidence[item.id],
                              onSetCheck: (v) => _setCheck(item.id, v),
                              onAddPhoto: () => _addPhoto(item.id),
                              onReasonChanged: (v) => _setReason(item.id, v),
                            ),
                          Row(
                            children: [
                              if (_groupIndex > 0)
                                OutlinedButton.icon(
                                  onPressed: () => setState(() => _groupIndex -= 1),
                                  icon: const Icon(Icons.chevron_left),
                                  label: const Text('Quay lại'),
                                ),
                              const Spacer(),
                              if (_groupIndex < _groups.length - 1)
                                OutlinedButton.icon(
                                  onPressed: () => setState(() => _groupIndex += 1),
                                  icon: const Icon(Icons.chevron_right),
                                  label: Text('Nhóm ${_groupIndex + 2}/${_groups.length}'),
                                ),
                            ],
                          ),
                          const SizedBox(height: 20),
                          Card(
                            child: Padding(
                              padding: const EdgeInsets.all(14),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  // 13/08/2026 (Nam) — bỏ ô tick "Giấy khám sức khỏe nhân viên bếp":
                                  // đã có hạng mục checklist riêng đánh giá đúng nội dung này (tự
                                  // tạo qua checklist-groups, BR-210) — ô tick ở đây trùng lặp, dễ
                                  // gây hiểu nhầm 2 nguồn sự thật khác nhau cho cùng một việc. Đối
                                  // chiếu bản Web (sửa 11/08/2026) — payload vẫn gửi cố định `true`
                                  // (xem `InspectionRepository.submit()`), chỉ bỏ UI.
                                  if (_answeredCount < _allItemIds.length)
                                    Padding(
                                      padding: const EdgeInsets.only(top: 6),
                                      child: Text(
                                        'Còn ${_allItemIds.length - _answeredCount} hạng mục chưa đánh giá (BR-101).',
                                        style: const TextStyle(color: AppTheme.warningAmber, fontSize: 12),
                                      ),
                                    ),
                                  if (_itemsMissingEvidence.isNotEmpty)
                                    Padding(
                                      padding: const EdgeInsets.only(top: 6),
                                      child: Text(
                                        '${_itemsMissingEvidence.length} hạng mục Không đạt còn thiếu ảnh/lý do (BR-207).',
                                        style: const TextStyle(color: AppTheme.warningAmber, fontSize: 12),
                                      ),
                                    ),
                                  const SizedBox(height: 12),
                                  SizedBox(
                                    width: double.infinity,
                                    child: ElevatedButton.icon(
                                      onPressed: _submitDisabled ? null : _submit,
                                      icon: _submitting
                                          ? const SizedBox(
                                              width: 16,
                                              height: 16,
                                              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                                            )
                                          : const Icon(Icons.check_circle_outline),
                                      label: Text(_submitting ? 'Đang nộp…' : 'Hoàn thành phiên kiểm tra'),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ),
      ),
    );
  }
}

class _ResultBanner extends StatelessWidget {
  final KitchenInspection result;
  const _ResultBanner({required this.result});

  @override
  Widget build(BuildContext context) {
    final ok = (result.score ?? 0) >= 60;
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: (ok ? AppTheme.successEmerald : AppTheme.warningAmber).withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Text(
        'Đã lưu phiên ${result.id} — điểm ${result.score}% (${ok ? 'Đạt' : 'Không đạt'}).',
        style: TextStyle(color: ok ? AppTheme.successEmerald : AppTheme.warningAmber, fontSize: 13),
      ),
    );
  }
}

class _ProgressCard extends StatelessWidget {
  final int answered;
  final int total;
  final int score;
  final int failedMissingEvidence;
  const _ProgressCard({
    required this.answered,
    required this.total,
    required this.score,
    required this.failedMissingEvidence,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Đã đánh giá $answered/$total hạng mục', style: const TextStyle(fontSize: 13)),
                  const SizedBox(height: 4),
                  Text(
                    'Điểm tạm tính: $score%',
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: score < 60 ? AppTheme.dangerRose : AppTheme.successEmerald,
                    ),
                  ),
                ],
              ),
            ),
            if (failedMissingEvidence > 0)
              const Icon(Icons.warning_amber_rounded, color: AppTheme.warningAmber),
          ],
        ),
      ),
    );
  }
}

class _GroupSelector extends StatelessWidget {
  final List<ChecklistGroup> groups;
  final int index;
  final Map<String, bool?> checks;
  final ValueChanged<int> onChanged;
  const _GroupSelector({
    required this.groups,
    required this.index,
    required this.checks,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return DropdownButtonFormField<int>(
      initialValue: index,
      // 13/08/2026 (Nam) — lỗi UI dropdown: thiếu `isExpanded` nên field co lại vừa khít nội
      // dung MỤC RỘNG NHẤT thay vì chiếm hết chiều ngang cha; tên nhóm dài + số đếm
      // "(x/y)" tràn khỏi khung, chữ bị cắt/đè lên viền — đúng lỗi kinh điển của
      // `DropdownButtonFormField` khi item có `Text` dài mà không set cờ này.
      isExpanded: true,
      decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Nhóm hạng mục'),
      dropdownColor: AppTheme.cardDark,
      items: [
        for (var i = 0; i < groups.length; i++)
          DropdownMenuItem(
            value: i,
            child: Text(
              '${i + 1} — ${groups[i].name ?? '—'} '
              '(${groups[i].items.where((it) => checks[it.id] != null).length}/${groups[i].items.length})',
              overflow: TextOverflow.ellipsis,
            ),
          ),
      ],
      onChanged: (v) {
        if (v != null) onChanged(v);
      },
    );
  }
}

class _ChecklistItemCard extends StatelessWidget {
  final ChecklistItem item;
  final bool? value;
  final _Evidence? evidence;
  final ValueChanged<bool> onSetCheck;
  final VoidCallback onAddPhoto;
  final ValueChanged<String> onReasonChanged;
  const _ChecklistItemCard({
    required this.item,
    required this.value,
    required this.evidence,
    required this.onSetCheck,
    required this.onAddPhoto,
    required this.onReasonChanged,
  });

  @override
  Widget build(BuildContext context) {
    final failed = value == false;
    final missingPhoto = failed && (evidence?.photos ?? 0) == 0;
    final missingReason = failed && (evidence?.reason ?? '').trim().isEmpty;
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(item.name ?? item.id, style: const TextStyle(fontWeight: FontWeight.w600)),
                ),
                if ((item.legalNote ?? '').trim().isNotEmpty)
                  IconButton(
                    icon: const Icon(Icons.info_outline, size: 18),
                    tooltip: item.legalNote,
                    onPressed: () => showDialog<void>(
                      context: context,
                      builder: (_) => AlertDialog(
                        title: const Text('Căn cứ pháp lý'),
                        content: Text(item.legalNote ?? ''),
                        actions: [
                          TextButton(onPressed: () => Navigator.pop(context), child: const Text('Đóng')),
                        ],
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => onSetCheck(true),
                    style: OutlinedButton.styleFrom(
                      backgroundColor: value == true ? AppTheme.successEmerald.withValues(alpha: 0.2) : null,
                      foregroundColor: value == true ? AppTheme.successEmerald : null,
                    ),
                    child: const Text('Đạt'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => onSetCheck(false),
                    style: OutlinedButton.styleFrom(
                      backgroundColor: value == false ? AppTheme.dangerRose.withValues(alpha: 0.2) : null,
                      foregroundColor: value == false ? AppTheme.dangerRose : null,
                    ),
                    child: const Text('Không đạt'),
                  ),
                ),
              ],
            ),
            if (failed) ...[
              const SizedBox(height: 10),
              Row(
                children: [
                  OutlinedButton.icon(
                    onPressed: onAddPhoto,
                    icon: const Icon(Icons.camera_alt_outlined, size: 16),
                    label: Text('Ảnh minh chứng (${evidence?.photos ?? 0})'),
                  ),
                  const SizedBox(width: 8),
                  const Text('* bắt buộc', style: TextStyle(color: AppTheme.dangerRose, fontSize: 11)),
                ],
              ),
              if (missingPhoto)
                const Padding(
                  padding: EdgeInsets.only(top: 4),
                  child: Text(
                    'Hạng mục Không đạt bắt buộc ít nhất 1 ảnh (BR-207).',
                    style: TextStyle(color: AppTheme.dangerRose, fontSize: 11),
                  ),
                ),
              const SizedBox(height: 8),
              TextField(
                onChanged: onReasonChanged,
                minLines: 2,
                maxLines: 3,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: 'Lý do không đạt (bắt buộc)',
                ),
              ),
              if (missingReason)
                const Padding(
                  padding: EdgeInsets.only(top: 4),
                  child: Text(
                    'Bắt buộc nhập lý do (BR-207).',
                    style: TextStyle(color: AppTheme.dangerRose, fontSize: 11),
                  ),
                ),
            ],
          ],
        ),
      ),
    );
  }
}
