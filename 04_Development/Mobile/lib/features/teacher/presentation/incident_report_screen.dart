import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/classroom_models.dart';
import '../data/classroom_repository.dart';
import '../data/incident_models.dart';
import '../data/incident_repository.dart';
import '../data/student_models.dart';
import '../data/student_repository.dart';

const _historyTabs = [
  ('all', 'Tất cả'),
  ('PENDING_TRIAGE', 'Chờ tiếp nhận'),
  ('MERGED', 'Đã gộp'),
  ('CLOSED_NOT_FOOD_SAFETY', 'Đã đóng'),
];

/// Báo cáo sự cố khẩn cấp tại lớp (UC-074) — Teacher chỉ tạo BÁO CÁO THÔ
/// (`source-reports`, RPT-xxx, PENDING_TRIAGE), Y tế trường triage/gộp thành
/// sự cố thật sau đó. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/incidents/page.tsx`.
class IncidentReportScreen extends StatefulWidget {
  const IncidentReportScreen({super.key});

  @override
  State<IncidentReportScreen> createState() => _IncidentReportScreenState();
}

class _IncidentReportScreenState extends State<IncidentReportScreen> {
  Classroom? _classroom;
  List<Student> _students = [];
  List<SourceReport> _history = [];
  final Set<String> _selectedStudents = {};
  final _descriptionCtrl = TextEditingController();
  XFile? _photo;
  bool _loading = true;
  bool _submitting = false;
  String? _error;
  String? _formError;
  String _historyTab = 'all';

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _descriptionCtrl.dispose();
    super.dispose();
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
      final students = await StudentRepository.instance.fetchClassStudents(classroom.id);
      final history = await IncidentRepository.instance.fetchHistory(
        className: classroom.id,
        status: _historyTab == 'all' ? null : _historyTab,
      );
      setState(() {
        _students = students;
        _history = history;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _reloadHistory() async {
    if (_classroom == null) return;
    final history = await IncidentRepository.instance.fetchHistory(
      className: _classroom!.id,
      status: _historyTab == 'all' ? null : _historyTab,
    );
    if (mounted) setState(() => _history = history);
  }

  Future<void> _pickPhoto() async {
    final file = await ImagePicker().pickImage(source: ImageSource.gallery, imageQuality: 85);
    if (file != null) setState(() => _photo = file);
  }

  void _toggleStudent(String id) {
    setState(() {
      if (_selectedStudents.contains(id)) {
        _selectedStudents.remove(id);
      } else {
        _selectedStudents.add(id);
      }
    });
  }

  Future<void> _submit() async {
    if (_selectedStudents.isEmpty || _descriptionCtrl.text.trim().isEmpty) {
      setState(() => _formError = 'Vui lòng điền đầy đủ: học sinh liên quan và mô tả chi tiết.');
      return;
    }
    setState(() {
      _formError = null;
      _submitting = true;
    });

    final names = _selectedStudents
        .map((id) => _students.firstWhere((s) => s.id == id, orElse: () => Student(
              id: id,
              name: id,
              classroomId: null,
              allergies: const [],
              allergySeverity: null,
              specialDiet: null,
              hasAllergy: false,
              hasSpecialDiet: false,
            )).name ?? id)
        .join(', ');

    try {
      final report = await IncidentRepository.instance.create(
        className: _classroom?.id ?? '',
        description: 'Học sinh liên quan: $names.\n${_descriptionCtrl.text.trim()}',
        evidencePhotos: _photo == null ? 0 : 1,
      );
      if (_photo != null) {
        try {
          await IncidentRepository.instance.uploadEvidencePhoto(report.id, _photo!.path);
        } catch (_) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Đã gửi báo cáo nhưng tải ảnh minh chứng thất bại — thử tải lại sau.')),
            );
          }
        }
      }
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Báo cáo sự cố sức khỏe đã được ghi nhận. (MSG-048)')),
        );
      }
      setState(() {
        _selectedStudents.clear();
        _descriptionCtrl.clear();
        _photo = null;
      });
      _reloadHistory();
    } on ApiException catch (e) {
      setState(() => _formError = e.message);
    } catch (_) {
      setState(() => _formError = 'Không thể gửi báo cáo, thử lại sau.');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Báo cáo sự cố')),
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
                        Text(
                          'Lớp ${_classroom?.id ?? '—'} — báo cáo kích hoạt thông báo tức thời tới Y tế trường (BR-146)',
                          style: const TextStyle(color: Colors.white60, fontSize: 13),
                        ),
                        const SizedBox(height: 16),
                        Card(
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text('Tạo báo cáo mới (UC-074)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                                const SizedBox(height: 14),
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
                                const Text('Học sinh liên quan * (chọn nhiều)', style: TextStyle(fontSize: 13, color: Colors.white70)),
                                const SizedBox(height: 8),
                                Wrap(
                                  spacing: 8,
                                  runSpacing: 8,
                                  children: [
                                    for (final s in _students)
                                      FilterChip(
                                        label: Text(s.name ?? s.id),
                                        selected: _selectedStudents.contains(s.id),
                                        onSelected: (_) => _toggleStudent(s.id),
                                      ),
                                  ],
                                ),
                                if (_students.isEmpty)
                                  const Padding(
                                    padding: EdgeInsets.only(top: 4),
                                    child: Text('Chưa có học sinh nào trong lớp.', style: TextStyle(color: Colors.white38, fontSize: 12)),
                                  ),
                                const SizedBox(height: 16),
                                const Text('Mô tả chi tiết *', style: TextStyle(fontSize: 13, color: Colors.white70)),
                                const SizedBox(height: 8),
                                TextField(
                                  controller: _descriptionCtrl,
                                  minLines: 3,
                                  maxLines: 6,
                                  decoration: const InputDecoration(
                                    hintText: 'Ngày ăn, triệu chứng quan sát được, thời điểm xuất hiện, diễn biến, đã xử lý gì…',
                                    border: OutlineInputBorder(),
                                  ),
                                ),
                                const SizedBox(height: 16),
                                const Text('Ảnh minh chứng (tuỳ chọn — PDF/JPG/PNG ≤10MB, BR-013)',
                                    style: TextStyle(fontSize: 13, color: Colors.white70)),
                                const SizedBox(height: 8),
                                OutlinedButton.icon(
                                  onPressed: _pickPhoto,
                                  icon: const Icon(Icons.attach_file),
                                  label: Text(_photo == null ? 'Chọn ảnh' : 'Đã chọn: ${_photo!.name}'),
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
                                    label: Text(_submitting ? 'Đang gửi…' : 'Gửi báo cáo sự cố'),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(height: 24),
                        const Text('Lịch sử báo cáo', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                        const SizedBox(height: 10),
                        Wrap(
                          spacing: 8,
                          children: [
                            for (final t in _historyTabs)
                              ChoiceChip(
                                label: Text(t.$2),
                                selected: _historyTab == t.$1,
                                onSelected: (_) {
                                  setState(() => _historyTab = t.$1);
                                  _reloadHistory();
                                },
                              ),
                          ],
                        ),
                        const SizedBox(height: 12),
                        if (_history.isEmpty)
                          const Padding(
                            padding: EdgeInsets.symmetric(vertical: 16),
                            child: Text('Lớp chưa có báo cáo sự cố nào.', style: TextStyle(color: Colors.white54)),
                          )
                        else
                          ..._history.map((r) => _HistoryCard(report: r)),
                      ],
                    ),
                  ),
      ),
    );
  }
}

class _HistoryCard extends StatelessWidget {
  final SourceReport report;
  const _HistoryCard({required this.report});

  @override
  Widget build(BuildContext context) {
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
                  child: Text(
                    report.suspectedMealText?.isNotEmpty == true
                        ? report.suspectedMealText!
                        : (report.description ?? '—'),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
                  ),
                ),
                _Badge(label: sourceReportStatusLabel(report.status), color: _statusColor(report.status)),
              ],
            ),
            const SizedBox(height: 6),
            Text('Mã: ${report.id}', style: const TextStyle(color: Colors.white38, fontSize: 11)),
            if (report.reportedAt != null)
              Text('Báo cáo lúc: ${report.reportedAt}', style: const TextStyle(color: Colors.white38, fontSize: 11)),
          ],
        ),
      ),
    );
  }

  Color _statusColor(String? status) {
    switch (status) {
      case 'PENDING_TRIAGE':
        return AppTheme.warningAmber;
      case 'MERGED':
        return AppTheme.primarySky;
      case 'CLOSED_NOT_FOOD_SAFETY':
        return Colors.white38;
      default:
        return Colors.white38;
    }
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
