import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../data/incident_models.dart';
import '../data/incident_repository.dart';
import '../data/student_models.dart';
import '../data/student_repository.dart';

/// Mở phiên điều tra từ biên bản COMPLETED — `title` + `affectedStudentCodes`
/// bắt buộc, không suy ra được từ báo cáo nguồn (backend `OpenInvestigationRequest`).
/// Trả về mã sự cố (INC-xxx) vừa tạo qua `Navigator.pop`.
class OpenInvestigationScreen extends StatefulWidget {
  final IncidentRecord record;
  const OpenInvestigationScreen({super.key, required this.record});

  @override
  State<OpenInvestigationScreen> createState() => _OpenInvestigationScreenState();
}

class _OpenInvestigationScreenState extends State<OpenInvestigationScreen> {
  final _titleCtrl = TextEditingController();
  final _searchCtrl = TextEditingController();
  List<MedicalStudent> _students = [];
  final Set<String> _selected = {};
  bool _loading = true;
  bool _submitting = false;
  String? _error;
  // 13/08/2026 — tách khỏi `_error` (lỗi submit, hiện trong form): trước đây lỗi TẢI DANH
  // SÁCH HỌC SINH cũng dùng chung field này, hiện chỉ một dòng text tĩnh không có cách nào
  // thử lại ngoài back ra vào lại màn — nay có nút "Thử lại" gọi lại đúng `_load()`.
  String? _loadError;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    _searchCtrl.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _loadError = null;
    });
    try {
      final students = await MedicalStudentRepository.instance.fetchAll();
      setState(() => _students = students);
    } on ApiException catch (e) {
      setState(() => _loadError = e.message);
    } catch (_) {
      setState(() => _loadError = 'Không tải được danh sách học sinh. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _submit() async {
    if (_titleCtrl.text.trim().isEmpty || _selected.isEmpty) {
      setState(() => _error = 'Cần tiêu đề sự cố và ít nhất 1 học sinh bị ảnh hưởng.');
      return;
    }
    setState(() {
      _error = null;
      _submitting = true;
    });
    try {
      final code = await IncidentRepository.instance.openInvestigation(
        recordCode: widget.record.id,
        title: _titleCtrl.text.trim(),
        affectedStudentCodes: _selected.toList(),
      );
      if (mounted) Navigator.of(context).pop(code);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final query = _searchCtrl.text.trim().toLowerCase();
    final candidates = query.isEmpty
        ? _students
        : _students
            .where((s) => (s.name ?? '').toLowerCase().contains(query) || s.id.toLowerCase().contains(query))
            .toList();

    return Scaffold(
      appBar: AppBar(title: Text('Mở phiên điều tra từ ${widget.record.id}')),
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
                : ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  if (_error != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: Text(_error!, style: const TextStyle(color: Colors.redAccent)),
                    ),
                  TextField(
                    controller: _titleCtrl,
                    decoration: const InputDecoration(
                      labelText: 'Tiêu đề sự cố *',
                      border: OutlineInputBorder(),
                      hintText: 'VD: Nghi ngộ độc thực phẩm sau bữa trưa lớp 3A',
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text('Học sinh bị ảnh hưởng * (${_selected.length} đã chọn)',
                      style: const TextStyle(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  TextField(
                    controller: _searchCtrl,
                    onChanged: (_) => setState(() {}),
                    decoration: const InputDecoration(
                      prefixIcon: Icon(Icons.search),
                      hintText: 'Tìm theo tên hoặc mã học sinh…',
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 8),
                  SizedBox(
                    height: 320,
                    child: candidates.isEmpty
                        ? const Center(child: Text('Không tìm thấy học sinh phù hợp.'))
                        : ListView.builder(
                            itemCount: candidates.length > 50 ? 50 : candidates.length,
                            itemBuilder: (context, index) {
                              final s = candidates[index];
                              return CheckboxListTile(
                                dense: true,
                                value: _selected.contains(s.id),
                                onChanged: (v) => setState(() {
                                  if (v == true) {
                                    _selected.add(s.id);
                                  } else {
                                    _selected.remove(s.id);
                                  }
                                }),
                                title: Text('${s.name ?? s.id} — ${s.id} (${s.classroomId ?? '—'})',
                                    style: const TextStyle(fontSize: 13)),
                              );
                            },
                          ),
                  ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _submitting ? null : _submit,
                      child: _submitting
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                            )
                          : const Text('Mở phiên điều tra'),
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
