import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/sample_models.dart';
import '../data/sample_repository.dart';

/// Mục 11.6 (bản web) — "Thu hồi & gửi xét nghiệm": chọn 1 sự cố đang mở, tick
/// các mẫu RETAINED trong cửa sổ điều tra 24h (server tự tính, BR-149), gộp
/// 1 lượt bàn giao (ADR-M08-1). BR-113: chỉ Medical Staff tự thực hiện —
/// không giao Service Caterers.
class SampleHandoverScreen extends StatefulWidget {
  const SampleHandoverScreen({super.key});

  @override
  State<SampleHandoverScreen> createState() => _SampleHandoverScreenState();
}

class _SampleHandoverScreenState extends State<SampleHandoverScreen> {
  List<IncidentBrief> _incidents = [];
  List<ReceivingAuthority> _authorities = [];
  String? _incidentId;
  List<FoodSample> _candidates = [];
  final Set<String> _selected = {};
  bool _addMore = false;
  String? _handoverType;
  String? _authorityId;
  final _criteriaCtrl = TextEditingController();
  final _labRepCtrl = TextEditingController();
  final List<XFile> _sealPhotos = [];
  final List<XFile> _handoverRecords = [];
  bool _loadingLists = true;
  bool _loadingCandidates = false;
  bool _submitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadLists();
  }

  @override
  void dispose() {
    _criteriaCtrl.dispose();
    _labRepCtrl.dispose();
    super.dispose();
  }

  Future<void> _loadLists() async {
    setState(() => _loadingLists = true);
    try {
      final incidents = await SampleRepository.instance.fetchIncidents();
      final authorities = await SampleRepository.instance.fetchAuthorities();
      setState(() {
        _incidents = incidents.where((i) => i.status != 'CLOSED').toList();
        _authorities = authorities;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _loadingLists = false);
    }
  }

  Future<void> _loadCandidates() async {
    if (_incidentId == null) return;
    setState(() {
      _loadingCandidates = true;
      _selected.clear();
    });
    try {
      final list = await SampleRepository.instance.fetchRetrievable(_incidentId!, includeAllDishes: _addMore);
      setState(() {
        _candidates = list;
        _selected.addAll(list.map((s) => s.id));
      });
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _loadingCandidates = false);
    }
  }

  bool get _formValid =>
      _incidentId != null &&
      _selected.isNotEmpty &&
      _handoverType != null &&
      (_handoverType != 'LAB' || _criteriaCtrl.text.trim().isNotEmpty) &&
      (_handoverType != 'AUTHORITY' || _authorityId != null) &&
      _handoverRecords.isNotEmpty;

  Future<void> _pickHandoverRecords() async {
    final files = await ImagePicker().pickMultiImage(imageQuality: 85);
    if (files.isNotEmpty) setState(() => _handoverRecords.addAll(files));
  }

  Future<void> _pickSealPhotos() async {
    final files = await ImagePicker().pickMultiImage(imageQuality: 85);
    if (files.isNotEmpty) setState(() => _sealPhotos.addAll(files));
  }

  Future<void> _submit() async {
    if (!_formValid) return;
    setState(() => _submitting = true);
    try {
      final req = await SampleRepository.instance.handOver(
        incidentId: _incidentId!,
        sampleIds: _selected.toList(),
        handoverType: _handoverType!,
        authorityId: _authorityId,
        testCriteria: _criteriaCtrl.text.trim().isEmpty ? null : _criteriaCtrl.text.trim(),
        labRepresentative: _labRepCtrl.text.trim().isEmpty ? null : _labRepCtrl.text.trim(),
        sealPhotoPaths: _sealPhotos.map((f) => f.path).toList(),
        handoverRecordPaths: _handoverRecords.map((f) => f.path).toList(),
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Đã bàn giao ${_selected.length} mẫu (phiếu ${req.id}).'),
            backgroundColor: AppTheme.successEmerald,
          ),
        );
        Navigator.of(context).pop();
      }
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Thu hồi & gửi xét nghiệm')),
      body: SafeArea(
        child: _loadingLists
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? Center(child: Text(_error!, textAlign: TextAlign.center))
                : ListView(
                    padding: const EdgeInsets.all(16),
                    children: [
                      DropdownButtonFormField<String>(
                        initialValue: _incidentId,
                        decoration: const InputDecoration(
                          labelText: 'Sự cố đang điều tra * (BR-114)',
                          border: OutlineInputBorder(),
                        ),
                        dropdownColor: AppTheme.cardDark,
                        items: [
                          for (final i in _incidents)
                            DropdownMenuItem(value: i.id, child: Text('${i.id} — ${i.title ?? ''}')),
                        ],
                        onChanged: (v) {
                          setState(() => _incidentId = v);
                          _loadCandidates();
                        },
                      ),
                      const SizedBox(height: 16),
                      if (_incidentId != null) ...[
                        Row(
                          children: [
                            const Expanded(
                              child: Text(
                                'Mặc định: mẫu RETAINED trong cửa sổ điều tra 24h.',
                                style: TextStyle(fontSize: 12, color: Colors.white54),
                              ),
                            ),
                            TextButton(
                              onPressed: () {
                                setState(() => _addMore = !_addMore);
                                _loadCandidates();
                              },
                              child: Text(_addMore ? 'Chỉ món nghi vấn' : '+ Bổ sung mẫu'),
                            ),
                          ],
                        ),
                        if (_loadingCandidates)
                          const Center(child: Padding(padding: EdgeInsets.all(20), child: CircularProgressIndicator()))
                        else if (_candidates.isEmpty)
                          const Padding(
                            padding: EdgeInsets.symmetric(vertical: 12),
                            child: Text('Không còn mẫu đang lưu trong cửa sổ điều tra.',
                                style: TextStyle(color: Colors.white54)),
                          )
                        else
                          for (final s in _candidates)
                            CheckboxListTile(
                              value: _selected.contains(s.id),
                              onChanged: (v) => setState(() {
                                if (v == true) {
                                  _selected.add(s.id);
                                } else {
                                  _selected.remove(s.id);
                                }
                              }),
                              title: Text('${s.id} — ${s.dish ?? '—'}', style: const TextStyle(fontSize: 13)),
                              subtitle: Text(
                                (s.hoursRemaining ?? 0) > 0
                                    ? 'Còn ${s.hoursRemaining}h trong hạn lưu'
                                    : 'Đã quá hạn lưu tối thiểu',
                                style: const TextStyle(fontSize: 11),
                              ),
                            ),
                        const SizedBox(height: 12),
                        DropdownButtonFormField<String>(
                          initialValue: _handoverType,
                          decoration:
                              const InputDecoration(labelText: 'Hình thức bàn giao * (BR-212)', border: OutlineInputBorder()),
                          dropdownColor: AppTheme.cardDark,
                          items: const [
                            DropdownMenuItem(value: 'LAB', child: Text('Gửi phòng kiểm nghiệm')),
                            DropdownMenuItem(value: 'AUTHORITY', child: Text('Giao cơ quan chức năng')),
                          ],
                          onChanged: (v) => setState(() => _handoverType = v),
                        ),
                        const SizedBox(height: 12),
                        if (_handoverType == 'AUTHORITY')
                          DropdownButtonFormField<String>(
                            initialValue: _authorityId,
                            decoration:
                                const InputDecoration(labelText: 'Nơi tiếp nhận *', border: OutlineInputBorder()),
                            dropdownColor: AppTheme.cardDark,
                            items: [
                              for (final a in _authorities)
                                DropdownMenuItem(value: a.id, child: Text(a.name ?? a.id)),
                            ],
                            onChanged: (v) => setState(() => _authorityId = v),
                          ),
                        if (_handoverType == 'AUTHORITY') const SizedBox(height: 12),
                        TextField(
                          controller: _criteriaCtrl,
                          onChanged: (_) => setState(() {}),
                          decoration: InputDecoration(
                            labelText: _handoverType == 'LAB' ? 'Chỉ tiêu kiểm nghiệm *' : 'Chỉ tiêu kiểm nghiệm',
                            border: const OutlineInputBorder(),
                            hintText: 'VD: Salmonella, E. coli, S. aureus…',
                          ),
                        ),
                        const SizedBox(height: 12),
                        TextField(
                          controller: _labRepCtrl,
                          decoration: const InputDecoration(
                            labelText: 'Đại diện bên nhận (nếu có)',
                            border: OutlineInputBorder(),
                          ),
                        ),
                        const SizedBox(height: 16),
                        OutlinedButton.icon(
                          onPressed: _pickSealPhotos,
                          icon: const Icon(Icons.photo_camera_outlined),
                          label: Text('Ảnh niêm phong (${_sealPhotos.length}) — tuỳ chọn'),
                        ),
                        const SizedBox(height: 8),
                        OutlinedButton.icon(
                          onPressed: _pickHandoverRecords,
                          icon: const Icon(Icons.description_outlined),
                          label: Text('Biên bản giao nhận mẫu * (${_handoverRecords.length})'),
                        ),
                        if (_handoverRecords.isEmpty)
                          const Padding(
                            padding: EdgeInsets.only(top: 4),
                            child: Text(
                              'Bắt buộc — ảnh chụp phiếu nhận mẫu của nơi tiếp nhận (BR-212).',
                              style: TextStyle(color: AppTheme.warningAmber, fontSize: 11),
                            ),
                          ),
                        const SizedBox(height: 20),
                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton(
                            onPressed: _formValid && !_submitting ? _submit : null,
                            child: _submitting
                                ? const SizedBox(
                                    width: 16,
                                    height: 16,
                                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                                  )
                                : Text('Xác nhận đã bàn giao (${_selected.length} mẫu)'),
                          ),
                        ),
                      ],
                    ],
                  ),
      ),
    );
  }
}
