import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/child_models.dart';
import '../data/child_repository.dart';
import '../data/safety_report_models.dart';
import '../data/safety_report_repository.dart';
import 'widgets/child_selector.dart';

/// 18_ReportFoodSafetyConcern.md — UC-073. BR-138: gửi ngay không qua duyệt ·
/// BR-146..BR-155: quy trình sự cố · MSG-023/MSG-048. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/safety-concern/page.tsx`.
class SafetyConcernScreen extends StatefulWidget {
  const SafetyConcernScreen({super.key});

  @override
  State<SafetyConcernScreen> createState() => _SafetyConcernScreenState();
}

class _SafetyConcernScreenState extends State<SafetyConcernScreen> {
  List<ParentChild> _children = [];
  ParentChild? _active;
  bool _loading = true;
  String? _error;

  String? _category;
  final _descriptionCtrl = TextEditingController();
  XFile? _photo;
  String? _formError;
  bool _submitting = false;
  bool _submitted = false;

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
      final children = await ParentChildRepository.instance.fetchChildren();
      final active = ParentChildRepository.instance.activeChildOf(children);
      setState(() {
        _children = children;
        _active = active;
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

  Future<void> _pickPhoto() async {
    final file = await ImagePicker().pickImage(source: ImageSource.gallery, imageQuality: 85);
    if (file != null) setState(() => _photo = file);
  }

  Future<void> _submit() async {
    final active = _active;
    if (active == null) return;
    if (_category == null || _descriptionCtrl.text.trim().isEmpty || _photo == null) {
      setState(() => _formError = 'Vui lòng điền đầy đủ thông tin bắt buộc, bao gồm ảnh minh chứng (MSG-023).');
      return;
    }
    setState(() {
      _formError = null;
      _submitting = true;
    });
    try {
      final report = await SafetyReportRepository.instance.create(
        className: active.classroomId ?? '',
        studentCode: active.id,
        suspectedMealText: _category!,
        description: _descriptionCtrl.text.trim(),
        evidencePhotos: 1,
      );
      try {
        await SafetyReportRepository.instance.uploadEvidencePhoto(report.id, _photo!.path);
      } catch (_) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Đã gửi báo cáo nhưng tải ảnh minh chứng thất bại — thử tải lại sau.')),
          );
        }
      }
      if (mounted) setState(() => _submitted = true);
    } on ApiException catch (e) {
      setState(() => _formError = e.message);
    } catch (_) {
      setState(() => _formError = 'Không gửi được báo cáo, thử lại sau.');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  void _reset() {
    setState(() {
      _submitted = false;
      _category = null;
      _descriptionCtrl.clear();
      _photo = null;
      _formError = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Báo sự cố ATTP')),
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
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppTheme.dangerRose.withValues(alpha: 0.12),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: const Text(
                          'Dùng màn hình này khi nghi ngờ ngộ độc, dị vật hoặc phản ứng bất thường sau bữa ăn. '
                          'Nếu con có triệu chứng nặng, hãy đưa con tới cơ sở y tế trước, báo cáo sau.',
                          style: TextStyle(color: AppTheme.dangerRose, fontSize: 13),
                        ),
                      ),
                      const SizedBox(height: 16),
                      if (_children.isEmpty)
                        const Text('Tài khoản chưa liên kết học sinh nào.', style: TextStyle(color: Colors.white54))
                      else ...[
                        ChildSelector(children: _children, active: _active, onSelect: _selectChild),
                        const SizedBox(height: 16),
                        Card(
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(16),
                            side: const BorderSide(color: AppTheme.dangerRose, width: 1),
                          ),
                          child: Padding(
                            padding: const EdgeInsets.all(16),
                            child: _submitted
                                ? Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      const Row(
                                        children: [
                                          Icon(Icons.check_circle, color: AppTheme.successEmerald),
                                          SizedBox(width: 8),
                                          Expanded(
                                            child: Text(
                                              'Đã gửi báo cáo khẩn cấp (MSG-048)',
                                              style: TextStyle(fontWeight: FontWeight.bold),
                                            ),
                                          ),
                                        ],
                                      ),
                                      const SizedBox(height: 8),
                                      const Text(
                                        'Y tế trường và Ban giám hiệu đã nhận được báo cáo. Quy trình điều tra sự cố '
                                        'được kích hoạt ngay (BR-149/BR-151) — nhà trường sẽ liên hệ với bạn sớm nhất.',
                                        style: TextStyle(color: Colors.white70, fontSize: 13),
                                      ),
                                      const SizedBox(height: 16),
                                      SizedBox(
                                        width: double.infinity,
                                        child: OutlinedButton(onPressed: _reset, child: const Text('Gửi báo cáo khác')),
                                      ),
                                    ],
                                  )
                                : Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      const Text(
                                        'Báo cáo sự cố an toàn thực phẩm',
                                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: AppTheme.dangerRose),
                                      ),
                                      const SizedBox(height: 14),
                                      if (_formError != null) ...[
                                        Container(
                                          width: double.infinity,
                                          padding: const EdgeInsets.all(10),
                                          decoration: BoxDecoration(
                                            color: AppTheme.dangerRose.withValues(alpha: 0.15),
                                            borderRadius: BorderRadius.circular(10),
                                          ),
                                          child: Text(_formError!, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 13)),
                                        ),
                                        const SizedBox(height: 12),
                                      ],
                                      const Text('Loại sự cố * (BR-146)', style: TextStyle(fontSize: 13, color: Colors.white70)),
                                      const SizedBox(height: 8),
                                      DropdownButtonFormField<String>(
                                        initialValue: _category,
                                        decoration: const InputDecoration(border: OutlineInputBorder()),
                                        dropdownColor: AppTheme.cardDark,
                                        hint: const Text('— Chọn loại sự cố —'),
                                        items: [
                                          for (final c in concernCategories) DropdownMenuItem(value: c, child: Text(c)),
                                        ],
                                        onChanged: (v) => setState(() => _category = v),
                                      ),
                                      const SizedBox(height: 14),
                                      const Text('Mô tả chi tiết sự việc *', style: TextStyle(fontSize: 13, color: Colors.white70)),
                                      const SizedBox(height: 8),
                                      TextField(
                                        controller: _descriptionCtrl,
                                        minLines: 4,
                                        maxLines: 8,
                                        decoration: const InputDecoration(
                                          hintText: 'Ngày/bữa ăn nghi vấn, món ăn nghi vấn (nếu biết), triệu chứng của con, '
                                              'thời điểm xuất hiện, tình trạng hiện tại...',
                                          border: OutlineInputBorder(),
                                        ),
                                      ),
                                      const SizedBox(height: 14),
                                      const Text(
                                        'Ảnh minh chứng * (PDF/JPG/PNG ≤10MB — MSG-008/009)',
                                        style: TextStyle(fontSize: 13, color: Colors.white70),
                                      ),
                                      const SizedBox(height: 8),
                                      OutlinedButton.icon(
                                        onPressed: _pickPhoto,
                                        icon: const Icon(Icons.attach_file),
                                        label: Text(_photo == null ? 'Chọn ảnh triệu chứng / thức ăn nghi vấn' : 'Đã chọn: ${_photo!.name}'),
                                      ),
                                      const SizedBox(height: 20),
                                      SizedBox(
                                        width: double.infinity,
                                        child: ElevatedButton.icon(
                                          onPressed: _submitting ? null : _submit,
                                          style: ElevatedButton.styleFrom(backgroundColor: AppTheme.dangerRose),
                                          icon: _submitting
                                              ? const SizedBox(
                                                  width: 16,
                                                  height: 16,
                                                  child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                                                )
                                              : const Icon(Icons.warning_amber_rounded),
                                          label: Text(_submitting ? 'Đang gửi…' : 'Gửi báo cáo khẩn cấp'),
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
    );
  }
}
