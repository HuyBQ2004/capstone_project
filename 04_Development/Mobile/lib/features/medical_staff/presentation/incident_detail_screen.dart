import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/incident_models.dart';
import '../data/incident_repository.dart';
import 'sample_handover_screen.dart';

/// 26_IncidentDetail.md — UC-116, rút gọn cho "hàng ngày" (mobile): tóm tắt
/// ca, đánh giá mức độ (UC-009/BR-147), kết luận nguyên nhân (BR-150/151),
/// xác nhận khắc phục (UC-168) khi CAPA đã nộp bằng chứng. KHÔNG port món/lô
/// nghi vấn, bằng chứng camera, báo cáo chính thức PDF — vẫn làm ở bản Web.
/// Medical Staff KHÔNG đóng sự cố (BR-155/UC-013 — chỉ School Administration).
class IncidentDetailScreen extends StatefulWidget {
  final String code;
  const IncidentDetailScreen({super.key, required this.code});

  @override
  State<IncidentDetailScreen> createState() => _IncidentDetailScreenState();
}

class _IncidentDetailScreenState extends State<IncidentDetailScreen> {
  bool _loading = true;
  String? _error;
  Incident? _incident;
  CorrectiveAction? _capa;

  String? _severity;
  final _assessmentCtrl = TextEditingController();
  final _rootCauseCtrl = TextEditingController();
  bool _busy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _assessmentCtrl.dispose();
    _rootCauseCtrl.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final incident = await IncidentRepository.instance.fetchIncidentDetail(widget.code);
      final capas = await IncidentRepository.instance.fetchCorrectiveActions();
      final capa = capas.where((c) => c.incidentId == incident.id).isEmpty
          ? null
          : capas.firstWhere((c) => c.incidentId == incident.id);
      setState(() {
        _incident = incident;
        _capa = capa;
        _assessmentCtrl.text = incident.clinicalRiskAssessment ?? '';
        _rootCauseCtrl.text = incident.rootCause ?? '';
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được chi tiết sự cố.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  bool get _assessValid =>
      _severity != null && (_severity == 'LOW' || _assessmentCtrl.text.trim().isNotEmpty);

  Future<void> _submitAssess() async {
    if (!_assessValid || _busy) return;
    setState(() => _busy = true);
    try {
      await IncidentRepository.instance.assess(
        code: widget.code,
        severity: _severity!,
        clinicalRiskAssessment: _assessmentCtrl.text.trim().isEmpty ? null : _assessmentCtrl.text.trim(),
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đã chốt đánh giá — bắt đầu điều tra.')),
        );
      }
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _submitRootCause(String status) async {
    if (_rootCauseCtrl.text.trim().isEmpty || _busy) return;
    setState(() => _busy = true);
    try {
      await IncidentRepository.instance.rootCause(
        code: widget.code,
        rootCause: _rootCauseCtrl.text.trim(),
        rootCauseStatus: status,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(status == 'FINAL' ? 'Đã xác nhận kết luận chính thức.' : 'Đã lưu kết luận sơ bộ.')),
        );
      }
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _validateCapa() async {
    if (_capa == null || _busy) return;
    setState(() => _busy = true);
    try {
      await IncidentRepository.instance.validateCapa(_capa!.id);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đã xác nhận khắc phục — sự cố chuyển sang Hoàn tất.')),
        );
      }
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.code)),
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : _error != null || _incident == null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(_error ?? 'Không tìm thấy sự cố.', textAlign: TextAlign.center),
                          const SizedBox(height: 12),
                          ElevatedButton(onPressed: _load, child: const Text('Thử lại')),
                        ],
                      ),
                    ),
                  )
                : _buildBody(),
      ),
    );
  }

  Widget _buildBody() {
    final incident = _incident!;
    final state = incident.status ?? '';
    final locked = state == 'CLOSED' || incident.reportExported == true;

    return RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(incident.title ?? incident.id, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const SizedBox(height: 6),
                  Wrap(
                    spacing: 8,
                    runSpacing: 6,
                    children: [
                      _Badge(text: state, color: AppTheme.primarySky),
                      if (incident.severity != null && incident.severity != 'PENDING')
                        _Badge(text: incident.severity!, color: AppTheme.dangerRose),
                      _Badge(text: '${incident.affectedStudentsCount} HS ảnh hưởng', color: AppTheme.warningAmber),
                      for (final s in incident.symptoms) _Badge(text: s, color: AppTheme.warningAmber),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text('Báo cáo bởi ${incident.reportedByLabel ?? '—'} lúc ${incident.reportedAt ?? '—'}',
                      style: const TextStyle(fontSize: 12, color: Colors.white54)),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          if (locked)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 8),
              child: Text(
                'Hồ sơ ở chế độ chỉ đọc (đã đóng hoặc đã nộp báo cáo chính thức).',
                style: TextStyle(color: Colors.white54),
              ),
            ),
          if (!locked && state == 'SEVERITY_PENDING') _buildAssessCard(),
          if (!locked && state != 'SEVERITY_PENDING') _buildRootCauseCard(),
          if (!locked)
            Card(
              margin: const EdgeInsets.only(top: 4, bottom: 12),
              child: ListTile(
                leading: const Icon(Icons.science_outlined, color: AppTheme.primarySky),
                title: const Text('Thu hồi & gửi mẫu xét nghiệm'),
                subtitle: const Text('UC-076 · BR-113', style: TextStyle(fontSize: 11)),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => Navigator.of(context)
                    .push(MaterialPageRoute(builder: (_) => const SampleHandoverScreen())),
              ),
            ),
          if (_capa != null) _buildCapaCard(),
        ],
      ),
    );
  }

  Widget _buildAssessCard() {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Đánh giá mức độ nghiêm trọng (UC-009, BR-147)',
                style: TextStyle(fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            for (final opt in const [
              ('LOW', 'Nhẹ — triệu chứng nhẹ, cục bộ'),
              ('MEDIUM', 'Trung bình — nhiều HS, cần theo dõi y tế'),
              ('HIGH', 'Nặng — nguy cơ nghiêm trọng, cảnh báo ngay'),
            ])
              RadioListTile<String>(
                dense: true,
                value: opt.$1,
                groupValue: _severity,
                onChanged: (v) => setState(() => _severity = v),
                title: Text(opt.$2, style: const TextStyle(fontSize: 13)),
              ),
            const SizedBox(height: 8),
            TextField(
              controller: _assessmentCtrl,
              minLines: 2,
              maxLines: 4,
              decoration: InputDecoration(
                border: const OutlineInputBorder(),
                labelText: _severity == 'LOW' ? 'Đánh giá rủi ro lâm sàng (tuỳ chọn)' : 'Đánh giá rủi ro lâm sàng *',
              ),
              onChanged: (_) => setState(() {}),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _assessValid && !_busy ? _submitAssess : null,
                child: const Text('Chốt đánh giá & bắt đầu điều tra'),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRootCauseCard() {
    final incident = _incident!;
    final finalStatus = incident.rootCauseStatus == 'FINAL';
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Expanded(
                  child: Text('Kết luận nguyên nhân gốc rễ (BR-150/151)', style: TextStyle(fontWeight: FontWeight.w600)),
                ),
                if (incident.rootCause != null)
                  _Badge(text: finalStatus ? 'Chính thức' : 'Sơ bộ', color: finalStatus ? AppTheme.successEmerald : AppTheme.warningAmber),
              ],
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _rootCauseCtrl,
              minLines: 3,
              maxLines: 6,
              enabled: !finalStatus,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                hintText: 'VD: Nhiệt độ lõi món gà không đạt 75°C ở Bước 2',
              ),
              onChanged: (_) => setState(() {}),
            ),
            if (!finalStatus) ...[
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: _rootCauseCtrl.text.trim().isNotEmpty && !_busy
                          ? () => _submitRootCause('PRELIMINARY')
                          : null,
                      child: const Text('Lưu kết luận sơ bộ'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: _rootCauseCtrl.text.trim().isNotEmpty && !_busy
                          ? () => _submitRootCause('FINAL')
                          : null,
                      child: const Text('Xác nhận chính thức'),
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildCapaCard() {
    final capa = _capa!;
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Yêu cầu khắc phục ${capa.id}', style: const TextStyle(fontWeight: FontWeight.w600)),
            const SizedBox(height: 4),
            Text('Trạng thái: ${capa.status ?? '—'}', style: const TextStyle(fontSize: 12, color: Colors.white70)),
            if ((capa.rejectReason ?? '').isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 6),
                child: Text('Ban giám hiệu từ chối: ${capa.rejectReason}',
                    style: const TextStyle(color: AppTheme.dangerRose, fontSize: 12)),
              ),
            if (capa.status == 'SUBMITTED_EVIDENCE') ...[
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: _busy ? null : _validateCapa,
                  child: const Text('Xác nhận khắc phục (UC-168)'),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _Badge extends StatelessWidget {
  final String text;
  final Color color;
  const _Badge({required this.text, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(999)),
      child: Text(text, style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w600)),
    );
  }
}
