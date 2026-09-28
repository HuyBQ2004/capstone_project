import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/allergy_models.dart';
import '../data/allergy_repository.dart';
import '../data/child_models.dart';
import '../data/child_repository.dart';
import 'widgets/child_selector.dart';

/// 05_SubmitAllergyInformation.md — UC-084. BR-031: khai báo chỉ có hiệu lực
/// sau khi Y tế trường xác minh; mức Nặng bắt buộc bằng chứng y tế. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/allergies/page.tsx`.
class AllergyScreen extends StatefulWidget {
  const AllergyScreen({super.key});

  @override
  State<AllergyScreen> createState() => _AllergyScreenState();
}

class _AllergyScreenState extends State<AllergyScreen> {
  List<ParentChild> _children = [];
  ParentChild? _active;
  List<AllergyDeclaration> _declared = [];
  bool _loading = true;
  String? _error;

  String? _category;
  final _specificNameCtrl = TextEditingController();
  String? _severity;
  XFile? _proofFile;
  String? _formError;
  bool _submitting = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _specificNameCtrl.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final children = await ParentChildRepository.instance.fetchChildren();
      final active = ParentChildRepository.instance.activeChildOf(children);
      final declared =
          active == null ? <AllergyDeclaration>[] : await AllergyRepository.instance.fetchForStudent(active.id);
      setState(() {
        _children = children;
        _active = active;
        _declared = declared;
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

  Future<void> _pickProof() async {
    final file = await ImagePicker().pickImage(source: ImageSource.gallery, imageQuality: 85);
    if (file != null) setState(() => _proofFile = file);
  }

  Future<void> _submit() async {
    final active = _active;
    if (active == null) return;
    final isOther = _category == 'Khác';
    final allergen = _specificNameCtrl.text.trim().isNotEmpty ? _specificNameCtrl.text.trim() : _category;

    if (_category == null || _severity == null || (isOther && _specificNameCtrl.text.trim().isEmpty)) {
      setState(() => _formError = 'Vui lòng điền đầy đủ thông tin bắt buộc (*).');
      return;
    }
    if (_severity == 'SEVERE' && _proofFile == null) {
      setState(() => _formError = 'Mức độ Nặng bắt buộc đính kèm bằng chứng y tế từ bệnh viện (BR-031).');
      return;
    }

    setState(() {
      _formError = null;
      _submitting = true;
    });
    try {
      String? proofFileId;
      if (_proofFile != null) {
        proofFileId = await AllergyRepository.instance.uploadProof(active.id, _proofFile!.path);
      }
      await AllergyRepository.instance.create(
        studentId: active.id,
        category: _category!,
        allergen: allergen!,
        severity: _severity!,
        proofFileId: proofFileId,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Đã gửi khai báo dị ứng — chờ Y tế trường xác minh (MSG-036).'),
            backgroundColor: AppTheme.successEmerald,
          ),
        );
      }
      setState(() {
        _category = null;
        _severity = null;
        _proofFile = null;
        _specificNameCtrl.clear();
      });
      final declared = await AllergyRepository.instance.fetchForStudent(active.id);
      if (mounted) setState(() => _declared = declared);
    } on ApiException catch (e) {
      setState(() => _formError = e.message);
    } catch (_) {
      setState(() => _formError = 'Không gửi được khai báo, thử lại sau.');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isOther = _category == 'Khác';
    return Scaffold(
      appBar: AppBar(title: const Text('Khai báo dị ứng')),
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
                          const SizedBox(height: 16),
                          const Text('Hồ sơ dị ứng hiện tại', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                          const SizedBox(height: 10),
                          if (_declared.isEmpty)
                            const Padding(
                              padding: EdgeInsets.symmetric(vertical: 8),
                              child: Text('Chưa có khai báo dị ứng nào.', style: TextStyle(color: Colors.white54)),
                            )
                          else
                            ..._declared.map((d) => _DeclarationCard(declaration: d)),
                          const SizedBox(height: 10),
                          Container(
                            width: double.infinity,
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: AppTheme.primarySky.withValues(alpha: 0.1),
                              borderRadius: BorderRadius.circular(10),
                            ),
                            child: const Text(
                              'Hồ sơ đã khai báo không thể tự chỉnh sửa. Cần thay đổi thì liên hệ Y tế trường (BR-031).',
                              style: TextStyle(fontSize: 12, color: Colors.white70),
                            ),
                          ),
                          const SizedBox(height: 24),
                          const Text('Khai báo dị ứng mới', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                          const SizedBox(height: 10),
                          Card(
                            child: Padding(
                              padding: const EdgeInsets.all(16),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  if (_formError != null) ...[
                                    Container(
                                      width: double.infinity,
                                      padding: const EdgeInsets.all(10),
                                      decoration: BoxDecoration(
                                        color: AppTheme.dangerRose.withValues(alpha: 0.12),
                                        borderRadius: BorderRadius.circular(10),
                                      ),
                                      child: Text(_formError!, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 13)),
                                    ),
                                    const SizedBox(height: 12),
                                  ],
                                  const Text('Nhóm dị ứng *', style: TextStyle(fontSize: 13, color: Colors.white70)),
                                  const SizedBox(height: 8),
                                  DropdownButtonFormField<String>(
                                    initialValue: _category,
                                    decoration: const InputDecoration(border: OutlineInputBorder()),
                                    dropdownColor: AppTheme.cardDark,
                                    hint: const Text('— Chọn nhóm dị ứng —'),
                                    items: [
                                      for (final c in allergenCategories) DropdownMenuItem(value: c, child: Text(c)),
                                    ],
                                    onChanged: (v) => setState(() => _category = v),
                                  ),
                                  const SizedBox(height: 14),
                                  Text(
                                    isOther ? 'Tên chất gây dị ứng *' : 'Tên chất gây dị ứng (cụ thể hoá, tuỳ chọn)',
                                    style: const TextStyle(fontSize: 13, color: Colors.white70),
                                  ),
                                  const SizedBox(height: 8),
                                  TextField(
                                    controller: _specificNameCtrl,
                                    decoration: const InputDecoration(
                                      hintText: 'VD: tôm, cua, sò...',
                                      border: OutlineInputBorder(),
                                    ),
                                  ),
                                  const SizedBox(height: 14),
                                  const Text('Mức độ *', style: TextStyle(fontSize: 13, color: Colors.white70)),
                                  const SizedBox(height: 8),
                                  Wrap(
                                    spacing: 8,
                                    runSpacing: 8,
                                    children: [
                                      for (final s in severityOptions)
                                        ChoiceChip(
                                          label: Text(severityLabel(s)),
                                          selected: _severity == s,
                                          onSelected: (_) => setState(() => _severity = s),
                                        ),
                                    ],
                                  ),
                                  if (_severity == 'SEVERE') ...[
                                    const SizedBox(height: 12),
                                    Container(
                                      width: double.infinity,
                                      padding: const EdgeInsets.all(10),
                                      decoration: BoxDecoration(
                                        color: AppTheme.warningAmber.withValues(alpha: 0.12),
                                        borderRadius: BorderRadius.circular(10),
                                      ),
                                      child: const Text(
                                        'Mức Nặng bắt buộc nộp bằng chứng y tế từ bệnh viện — khai báo không có giấy tờ sẽ bị từ chối (BR-031).',
                                        style: TextStyle(color: AppTheme.warningAmber, fontSize: 12),
                                      ),
                                    ),
                                  ],
                                  const SizedBox(height: 14),
                                  Text(
                                    'Bằng chứng y tế từ bệnh viện ${_severity == 'SEVERE' ? '* (bắt buộc)' : '(tuỳ chọn)'}',
                                    style: const TextStyle(fontSize: 13, color: Colors.white70),
                                  ),
                                  const SizedBox(height: 8),
                                  OutlinedButton.icon(
                                    onPressed: _pickProof,
                                    icon: const Icon(Icons.attach_file),
                                    label: Text(_proofFile == null ? 'Chọn tệp PDF/JPG/PNG ≤10MB' : 'Đã chọn: ${_proofFile!.name}'),
                                  ),
                                  const SizedBox(height: 20),
                                  SizedBox(
                                    width: double.infinity,
                                    child: ElevatedButton.icon(
                                      onPressed: _submitting ? null : _submit,
                                      icon: _submitting
                                          ? const SizedBox(
                                              width: 16,
                                              height: 16,
                                              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                                            )
                                          : const Icon(Icons.send),
                                      label: Text(_submitting ? 'Đang gửi…' : 'Gửi khai báo dị ứng'),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
      ),
    );
  }
}

class _DeclarationCard extends StatelessWidget {
  final AllergyDeclaration declaration;
  const _DeclarationCard({required this.declaration});

  Color _statusColor(String? status) {
    switch (status) {
      case 'VERIFIED':
        return AppTheme.successEmerald;
      case 'REJECTED':
        return AppTheme.dangerRose;
      case 'NEEDS_INFO':
        return AppTheme.warningAmber;
      default:
        return Colors.white38;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Flexible(
                        child: Text(
                          declaration.allergen ?? '—',
                          style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
                        ),
                      ),
                      const SizedBox(width: 6),
                      if (declaration.severity != null)
                        Text('(${severityLabel(declaration.severity)})', style: const TextStyle(fontSize: 11, color: Colors.white60)),
                    ],
                  ),
                  const SizedBox(height: 3),
                  Text(
                    '${declaration.category ?? '—'} · Khai báo ${declaration.declaredAt ?? '—'}',
                    style: const TextStyle(fontSize: 11, color: Colors.white38),
                  ),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
              decoration: BoxDecoration(
                color: _statusColor(declaration.status).withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                allergyStatusLabel(declaration.status),
                style: TextStyle(color: _statusColor(declaration.status), fontSize: 11, fontWeight: FontWeight.w600),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
