import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/health_review_models.dart';
import '../data/health_review_repository.dart';
import '../data/student_models.dart';
import '../data/student_repository.dart';

/// 02_ReviewHealthInformationScreen.md (UC-077) + 08_SpecialMealRequestList.md
/// (UC-078) — "Duyệt nhanh khai báo sức khỏe / suất ăn đặc biệt". Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/health-reviews/page.tsx`
/// và `.../special-meals/page.tsx`. Segmented control chọn module (Dị ứng /
/// Bệnh lý nền / Suất ăn đặc biệt); mỗi module có tab Chờ xử lý / Đã duyệt.
class HealthReviewsScreen extends StatefulWidget {
  const HealthReviewsScreen({super.key});

  @override
  State<HealthReviewsScreen> createState() => _HealthReviewsScreenState();
}

enum _Module { allergy, chronic, meals }

class _HealthReviewsScreenState extends State<HealthReviewsScreen> {
  _Module _module = _Module.allergy;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Duyệt khai báo sức khỏe')),
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(12),
              child: SegmentedButton<_Module>(
                segments: const [
                  ButtonSegment(value: _Module.allergy, label: Text('Dị ứng')),
                  ButtonSegment(value: _Module.chronic, label: Text('Bệnh lý nền')),
                  ButtonSegment(value: _Module.meals, label: Text('Suất ăn đặc biệt')),
                ],
                selected: {_module},
                onSelectionChanged: (s) => setState(() => _module = s.first),
              ),
            ),
            Expanded(
              child: switch (_module) {
                _Module.allergy => const _AllergyModule(),
                _Module.chronic => const _ChronicModule(),
                _Module.meals => const _SpecialMealsModule(),
              },
            ),
          ],
        ),
      ),
    );
  }
}

/// ===== Module: Dị ứng =====
class _AllergyModule extends StatefulWidget {
  const _AllergyModule();

  @override
  State<_AllergyModule> createState() => _AllergyModuleState();
}

class _AllergyModuleState extends State<_AllergyModule> {
  bool _loading = true;
  String? _error;
  List<AllergyDeclaration> _rows = [];
  List<MedicalStudent> _students = [];
  bool _showPending = true;

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
      final rows = await HealthReviewRepository.instance.fetchAllergies();
      final students = await MedicalStudentRepository.instance.fetchAll();
      setState(() {
        _rows = rows;
        _students = students;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  MedicalStudent? _studentOf(String? id) {
    if (id == null) return null;
    for (final s in _students) {
      if (s.id == id) return s;
    }
    return null;
  }

  Future<void> _openDetail(AllergyDeclaration d) async {
    final student = _studentOf(d.studentId);
    final acted = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (context) => _AllergyDetailSheet(declaration: d, student: student),
    );
    if (acted == true) await _load();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());
    if (_error != null) {
      return Center(
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
      );
    }
    final pending = _rows.where((d) => d.status != 'VERIFIED').toList();
    final verified = _rows.where((d) => d.status == 'VERIFIED').toList();
    final list = _showPending ? pending : verified;

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              ChoiceChip(label: Text('Chờ xử lý (${pending.length})'), selected: _showPending, onSelected: (_) => setState(() => _showPending = true)),
              const SizedBox(width: 8),
              ChoiceChip(label: Text('Đã duyệt (${verified.length})'), selected: !_showPending, onSelected: (_) => setState(() => _showPending = false)),
            ],
          ),
        ),
        Expanded(
          child: list.isEmpty
              ? RefreshIndicator(
                  onRefresh: _load,
                  child: ListView(children: const [SizedBox(height: 80), Center(child: Text('Không có khai báo nào.', style: TextStyle(color: Colors.white54)))]),
                )
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: list.length,
                    itemBuilder: (context, index) {
                      final d = list[index];
                      final student = _studentOf(d.studentId);
                      return Card(
                        margin: const EdgeInsets.only(bottom: 10),
                        child: ListTile(
                          title: Text('${student?.name ?? d.studentId ?? '—'} — ${d.allergen ?? '—'}'),
                          subtitle: Text(
                            '${student != null ? 'Lớp ${student.classroomId ?? '—'} · ' : ''}${severityLabelVi(d.severity)} · ${reviewStatusLabel(d.status)}',
                            style: const TextStyle(fontSize: 12),
                          ),
                          trailing: const Icon(Icons.chevron_right),
                          onTap: () => _openDetail(d),
                        ),
                      );
                    },
                  ),
                ),
        ),
      ],
    );
  }
}

class _AllergyDetailSheet extends StatefulWidget {
  final AllergyDeclaration declaration;
  final MedicalStudent? student;
  const _AllergyDetailSheet({required this.declaration, required this.student});

  @override
  State<_AllergyDetailSheet> createState() => _AllergyDetailSheetState();
}

class _AllergyDetailSheetState extends State<_AllergyDetailSheet> {
  bool _rejecting = false;
  final _noteCtrl = TextEditingController();
  bool _busy = false;

  @override
  void dispose() {
    _noteCtrl.dispose();
    super.dispose();
  }

  bool get _proofValid => widget.declaration.severity != 'SEVERE' || widget.declaration.proofFileId != null;

  Future<void> _verify() async {
    setState(() => _busy = true);
    try {
      await HealthReviewRepository.instance.verifyAllergy(widget.declaration.id);
      if (mounted) Navigator.pop(context, true);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _requestInfo() async {
    if (_noteCtrl.text.trim().isEmpty) return;
    setState(() => _busy = true);
    try {
      await HealthReviewRepository.instance.requestAllergyInfo(widget.declaration.id, _noteCtrl.text.trim());
      if (mounted) Navigator.pop(context, true);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final d = widget.declaration;
    final verified = d.status == 'VERIFIED';
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('${widget.student?.name ?? d.studentId ?? '—'} — ${d.allergen ?? '—'}',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 4),
            Text('Nhóm: ${d.category ?? '—'} · Mức độ: ${severityLabelVi(d.severity)} · Khai báo ${d.declaredAt ?? '—'}',
                style: const TextStyle(fontSize: 12, color: Colors.white70)),
            const SizedBox(height: 10),
            Text(
              d.proofFileId != null ? 'Đã đính kèm tài liệu y tế.' : 'Chưa có tài liệu đính kèm.',
              style: const TextStyle(fontSize: 12),
            ),
            if (!_proofValid)
              const Padding(
                padding: EdgeInsets.only(top: 8),
                child: Text(
                  'Mức Nặng bắt buộc có tài liệu y tế — không thể xác minh cho tới khi phụ huynh bổ sung (BR-031).',
                  style: TextStyle(color: AppTheme.warningAmber, fontSize: 12),
                ),
              ),
            const SizedBox(height: 14),
            if (verified)
              const Text('Đã xác minh — chế độ chỉ xem.', style: TextStyle(color: Colors.white54))
            else if (_rejecting) ...[
              TextField(
                controller: _noteCtrl,
                minLines: 2,
                maxLines: 4,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Cần bổ sung gì * (BR-031)',
                ),
                onChanged: (_) => setState(() {}),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(onPressed: () => setState(() => _rejecting = false), child: const Text('Quay lại')),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _noteCtrl.text.trim().isNotEmpty && !_busy ? _requestInfo : null,
                      child: const Text('Gửi yêu cầu bổ sung'),
                    ),
                  ),
                ],
              ),
            ] else
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(onPressed: _busy ? null : () => setState(() => _rejecting = true), child: const Text('Yêu cầu bổ sung')),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _proofValid && !_busy ? _verify : null,
                      child: const Text('Xác minh'),
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}

/// ===== Module: Bệnh lý nền =====
class _ChronicModule extends StatefulWidget {
  const _ChronicModule();

  @override
  State<_ChronicModule> createState() => _ChronicModuleState();
}

class _ChronicModuleState extends State<_ChronicModule> {
  bool _loading = true;
  String? _error;
  List<ChronicCondition> _rows = [];
  List<MedicalStudent> _students = [];
  bool _showPending = true;

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
      final rows = await HealthReviewRepository.instance.fetchChronicConditions();
      final students = await MedicalStudentRepository.instance.fetchAll();
      setState(() {
        _rows = rows;
        _students = students;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  MedicalStudent? _studentOf(String? id) {
    if (id == null) return null;
    for (final s in _students) {
      if (s.id == id) return s;
    }
    return null;
  }

  Future<void> _openDetail(ChronicCondition d) async {
    final student = _studentOf(d.studentId);
    final acted = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (context) => _ChronicDetailSheet(condition: d, student: student),
    );
    if (acted == true) await _load();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());
    if (_error != null) {
      return Center(
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
      );
    }
    final pending = _rows.where((d) => d.status != 'VERIFIED').toList();
    final verified = _rows.where((d) => d.status == 'VERIFIED').toList();
    final list = _showPending ? pending : verified;

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              ChoiceChip(label: Text('Chờ xử lý (${pending.length})'), selected: _showPending, onSelected: (_) => setState(() => _showPending = true)),
              const SizedBox(width: 8),
              ChoiceChip(label: Text('Đã duyệt (${verified.length})'), selected: !_showPending, onSelected: (_) => setState(() => _showPending = false)),
            ],
          ),
        ),
        Expanded(
          child: list.isEmpty
              ? RefreshIndicator(
                  onRefresh: _load,
                  child: ListView(children: const [SizedBox(height: 80), Center(child: Text('Không có khai báo nào.', style: TextStyle(color: Colors.white54)))]),
                )
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: list.length,
                    itemBuilder: (context, index) {
                      final d = list[index];
                      final student = _studentOf(d.studentId);
                      return Card(
                        margin: const EdgeInsets.only(bottom: 10),
                        child: ListTile(
                          title: Text('${student?.name ?? d.studentId ?? '—'} — ${d.conditionName ?? '—'}'),
                          subtitle: Text(
                            '${student != null ? 'Lớp ${student.classroomId ?? '—'} · ' : ''}${severityLabelVi(d.severity)} · ${reviewStatusLabel(d.status)}',
                            style: const TextStyle(fontSize: 12),
                          ),
                          trailing: const Icon(Icons.chevron_right),
                          onTap: () => _openDetail(d),
                        ),
                      );
                    },
                  ),
                ),
        ),
      ],
    );
  }
}

class _ChronicDetailSheet extends StatefulWidget {
  final ChronicCondition condition;
  final MedicalStudent? student;
  const _ChronicDetailSheet({required this.condition, required this.student});

  @override
  State<_ChronicDetailSheet> createState() => _ChronicDetailSheetState();
}

class _ChronicDetailSheetState extends State<_ChronicDetailSheet> {
  bool _rejecting = false;
  final _noteCtrl = TextEditingController();
  bool _busy = false;

  @override
  void dispose() {
    _noteCtrl.dispose();
    super.dispose();
  }

  bool get _proofValid => widget.condition.severity != 'SEVERE' || widget.condition.proofFileId != null;

  Future<void> _verify() async {
    setState(() => _busy = true);
    try {
      await HealthReviewRepository.instance.verifyChronic(widget.condition.id);
      if (mounted) Navigator.pop(context, true);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _requestInfo() async {
    if (_noteCtrl.text.trim().isEmpty) return;
    setState(() => _busy = true);
    try {
      await HealthReviewRepository.instance.requestChronicInfo(widget.condition.id, _noteCtrl.text.trim());
      if (mounted) Navigator.pop(context, true);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final d = widget.condition;
    final verified = d.status == 'VERIFIED';
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('${widget.student?.name ?? d.studentId ?? '—'} — ${d.conditionName ?? '—'}',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 4),
            Text('Nhóm: ${d.category ?? '—'} · Mức độ: ${severityLabelVi(d.severity)} · Khai báo ${d.declaredAt ?? '—'}',
                style: const TextStyle(fontSize: 12, color: Colors.white70)),
            if ((d.notes ?? '').isNotEmpty) ...[
              const SizedBox(height: 8),
              Text('Ghi chú của phụ huynh: ${d.notes}', style: const TextStyle(fontSize: 12, color: Colors.white70)),
            ],
            const SizedBox(height: 10),
            Text(
              d.proofFileId != null ? 'Đã đính kèm tài liệu y tế.' : 'Chưa có tài liệu đính kèm.',
              style: const TextStyle(fontSize: 12),
            ),
            if (!_proofValid)
              const Padding(
                padding: EdgeInsets.only(top: 8),
                child: Text(
                  'Mức Nặng bắt buộc có tài liệu y tế — không thể xác minh cho tới khi phụ huynh bổ sung (BR-031).',
                  style: TextStyle(color: AppTheme.warningAmber, fontSize: 12),
                ),
              ),
            const SizedBox(height: 14),
            if (verified)
              const Text('Đã xác minh — chế độ chỉ xem.', style: TextStyle(color: Colors.white54))
            else if (_rejecting) ...[
              TextField(
                controller: _noteCtrl,
                minLines: 2,
                maxLines: 4,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: 'Cần bổ sung gì * (BR-031)',
                ),
                onChanged: (_) => setState(() {}),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(onPressed: () => setState(() => _rejecting = false), child: const Text('Quay lại')),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _noteCtrl.text.trim().isNotEmpty && !_busy ? _requestInfo : null,
                      child: const Text('Gửi yêu cầu bổ sung'),
                    ),
                  ),
                ],
              ),
            ] else
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(onPressed: _busy ? null : () => setState(() => _rejecting = true), child: const Text('Yêu cầu bổ sung')),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _proofValid && !_busy ? _verify : null,
                      child: const Text('Xác minh'),
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}

/// ===== Module: Suất ăn đặc biệt =====
class _SpecialMealsModule extends StatefulWidget {
  const _SpecialMealsModule();

  @override
  State<_SpecialMealsModule> createState() => _SpecialMealsModuleState();
}

class _SpecialMealsModuleState extends State<_SpecialMealsModule> {
  bool _loading = true;
  String? _error;
  List<SpecialMealRequest> _rows = [];
  bool _showPending = true;

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
      final rows = await HealthReviewRepository.instance.fetchSpecialMeals();
      setState(() => _rows = rows);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _openDetail(SpecialMealRequest r) async {
    final acted = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      builder: (context) => _SpecialMealDetailSheet(request: r),
    );
    if (acted == true) await _load();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());
    if (_error != null) {
      return Center(
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
      );
    }
    final pending = _rows.where((r) => r.status == 'PENDING').toList();
    final others = _rows.where((r) => r.status != 'PENDING').toList();
    final list = _showPending ? pending : others;

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              ChoiceChip(label: Text('Chờ duyệt (${pending.length})'), selected: _showPending, onSelected: (_) => setState(() => _showPending = true)),
              const SizedBox(width: 8),
              ChoiceChip(label: Text('Đã xử lý (${others.length})'), selected: !_showPending, onSelected: (_) => setState(() => _showPending = false)),
            ],
          ),
        ),
        Expanded(
          child: list.isEmpty
              ? RefreshIndicator(
                  onRefresh: _load,
                  child: ListView(children: const [SizedBox(height: 80), Center(child: Text('Không có yêu cầu nào.', style: TextStyle(color: Colors.white54)))]),
                )
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.builder(
                    padding: const EdgeInsets.all(16),
                    itemCount: list.length,
                    itemBuilder: (context, index) {
                      final r = list[index];
                      final standard = standardDiets.contains(r.dietaryType);
                      return Card(
                        margin: const EdgeInsets.only(bottom: 10),
                        child: ListTile(
                          title: Text('${r.student ?? '—'} (${r.classroomId ?? '—'})'),
                          subtitle: Text(
                            '${r.dietaryType ?? '—'}${standard ? '' : ' — ngoài danh mục chuẩn'} · ${reviewStatusLabel(r.status)}',
                            style: TextStyle(fontSize: 12, color: standard ? Colors.white70 : AppTheme.dangerRose),
                          ),
                          trailing: const Icon(Icons.chevron_right),
                          onTap: () => _openDetail(r),
                        ),
                      );
                    },
                  ),
                ),
        ),
      ],
    );
  }
}

class _SpecialMealDetailSheet extends StatefulWidget {
  final SpecialMealRequest request;
  const _SpecialMealDetailSheet({required this.request});

  @override
  State<_SpecialMealDetailSheet> createState() => _SpecialMealDetailSheetState();
}

class _SpecialMealDetailSheetState extends State<_SpecialMealDetailSheet> {
  bool _rejecting = false;
  final _noteCtrl = TextEditingController();
  bool _busy = false;

  @override
  void dispose() {
    _noteCtrl.dispose();
    super.dispose();
  }

  bool get _dietValid => standardDiets.contains(widget.request.dietaryType);

  Future<void> _approve() async {
    setState(() => _busy = true);
    try {
      await HealthReviewRepository.instance
          .approveSpecialMeal(widget.request.id, note: _noteCtrl.text.trim().isEmpty ? null : _noteCtrl.text.trim());
      if (mounted) Navigator.pop(context, true);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _reject() async {
    if (_noteCtrl.text.trim().isEmpty) return;
    setState(() => _busy = true);
    try {
      await HealthReviewRepository.instance.rejectSpecialMeal(widget.request.id, _noteCtrl.text.trim());
      if (mounted) Navigator.pop(context, true);
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final r = widget.request;
    final pending = r.status == 'PENDING';
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('${r.student ?? '—'} (${r.classroomId ?? '—'})', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 4),
            Text('${r.dietaryType ?? '—'} · ${r.duration ?? '—'} · Yêu cầu bởi ${r.requestedBy ?? '—'}',
                style: const TextStyle(fontSize: 12, color: Colors.white70)),
            const SizedBox(height: 10),
            Text('Lý do y khoa:', style: TextStyle(fontSize: 12, color: Colors.white54)),
            Text(r.justification ?? '—', style: const TextStyle(fontSize: 13)),
            const SizedBox(height: 8),
            Text(r.hasDocument ? 'Có tài liệu minh chứng đính kèm.' : 'Không có tài liệu đính kèm.',
                style: const TextStyle(fontSize: 12)),
            if (!_dietValid)
              const Padding(
                padding: EdgeInsets.only(top: 8),
                child: Text(
                  'Chế độ ăn không thuộc danh mục chuẩn (BR-066) — chỉ có thể Từ chối kèm lý do.',
                  style: TextStyle(color: AppTheme.dangerRose, fontSize: 12),
                ),
              ),
            const SizedBox(height: 14),
            if (!pending)
              Text('Yêu cầu đã xử lý (${reviewStatusLabel(r.status)}) — chế độ chỉ xem.',
                  style: const TextStyle(color: Colors.white54))
            else if (_rejecting) ...[
              TextField(
                controller: _noteCtrl,
                minLines: 2,
                maxLines: 4,
                decoration: const InputDecoration(border: OutlineInputBorder(), labelText: 'Lý do từ chối *'),
                onChanged: (_) => setState(() {}),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(child: OutlinedButton(onPressed: () => setState(() => _rejecting = false), child: const Text('Quay lại'))),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _noteCtrl.text.trim().isNotEmpty && !_busy ? _reject : null,
                      child: const Text('Từ chối'),
                    ),
                  ),
                ],
              ),
            ] else
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(onPressed: _busy ? null : () => setState(() => _rejecting = true), child: const Text('Từ chối')),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(onPressed: _dietValid && !_busy ? _approve : null, child: const Text('Duyệt')),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
