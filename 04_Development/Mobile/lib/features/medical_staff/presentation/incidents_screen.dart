import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/incident_models.dart';
import '../data/incident_repository.dart';
import 'incident_detail_screen.dart';
import 'open_investigation_screen.dart';

/// 25_IncidentList.md — UC-114 mở rộng. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/incidents/page.tsx`.
/// 4 tab: Chờ tiếp nhận / Biên bản chờ xử lý / Đang xử lý / Đã đóng.
class IncidentsScreen extends StatefulWidget {
  const IncidentsScreen({super.key});

  @override
  State<IncidentsScreen> createState() => _IncidentsScreenState();
}

class _IncidentsScreenState extends State<IncidentsScreen> {
  bool _loading = true;
  String? _error;
  List<SourceReport> _reports = [];
  List<IncidentRecord> _records = [];
  List<Incident> _incidents = [];

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
      final results = await Future.wait([
        IncidentRepository.instance.fetchSourceReports(),
        IncidentRepository.instance.fetchRecords(),
        IncidentRepository.instance.fetchIncidents(),
      ]);
      setState(() {
        _reports = results[0] as List<SourceReport>;
        _records = results[1] as List<IncidentRecord>;
        _incidents = results[2] as List<Incident>;
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _closeReport(SourceReport r) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Đóng báo cáo — Không liên quan ATTP?'),
        content: Text('${r.id}: ${r.description ?? ''}'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Hủy')),
          ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Đóng')),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await IncidentRepository.instance.closeReport(r.id);
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    }
  }

  Future<void> _createRecord(SourceReport r) async {
    try {
      final content = 'Theo báo cáo ${r.id} của ${r.reportedByLabel ?? '—'} lúc '
          '${r.reportedAt ?? '—'}: ${r.description ?? ''}';
      final rec = await IncidentRepository.instance.createRecord(sourceReportCodes: [r.id], content: content);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Đã tạo biên bản ${rec.id}.')));
      }
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    }
  }

  Future<void> _closeRecord(IncidentRecord rec) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Đóng biên bản ${rec.id}?'),
        content: const Text('Toàn bộ báo cáo nguồn sẽ chuyển "Đã đóng — Không liên quan ATTP".'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('Hủy')),
          ElevatedButton(onPressed: () => Navigator.pop(context, true), child: const Text('Đóng')),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await IncidentRepository.instance.closeRecord(rec.id);
      await _load();
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message)));
    }
  }

  Future<void> _openInvestigation(IncidentRecord rec) async {
    final incidentCode = await Navigator.of(context)
        .push<String>(MaterialPageRoute(builder: (_) => OpenInvestigationScreen(record: rec)));
    if (incidentCode != null && mounted) {
      await _load();
      if (mounted) {
        Navigator.of(context)
            .push(MaterialPageRoute(builder: (_) => IncidentDetailScreen(code: incidentCode)));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final pendingReports =
        _reports.where((r) => r.status == 'PENDING_TRIAGE' || r.status == 'DRAFTING').toList();
    final pendingRecords = _records.where((r) => r.status == 'COMPLETED').toList();
    final processing = _incidents.where((i) => processingIncidentStatuses.contains(i.status)).toList();
    final closedReports = _reports.where((r) => r.status == 'CLOSED_NOT_FOOD_SAFETY').toList();
    final closedIncidents = _incidents.where((i) => i.status == 'CLOSED').toList();

    return DefaultTabController(
      length: 4,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Sự cố & Điều tra'),
          bottom: TabBar(
            isScrollable: true,
            tabs: [
              Tab(text: 'Chờ tiếp nhận (${pendingReports.length})'),
              Tab(text: 'Biên bản (${pendingRecords.length})'),
              Tab(text: 'Đang xử lý (${processing.length})'),
              Tab(text: 'Đã đóng (${closedReports.length + closedIncidents.length})'),
            ],
          ),
        ),
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
                  : TabBarView(
                      children: [
                        _ReportsTab(reports: pendingReports, onClose: _closeReport, onCreateRecord: _createRecord, onRefresh: _load),
                        _RecordsTab(records: pendingRecords, onClose: _closeRecord, onOpenInvestigation: _openInvestigation, onRefresh: _load),
                        _ProcessingTab(incidents: processing, onRefresh: _load),
                        _ClosedTab(reports: closedReports, incidents: closedIncidents, onRefresh: _load),
                      ],
                    ),
        ),
      ),
    );
  }
}

class _ReportsTab extends StatelessWidget {
  final List<SourceReport> reports;
  final ValueChanged<SourceReport> onClose;
  final ValueChanged<SourceReport> onCreateRecord;
  final Future<void> Function() onRefresh;
  const _ReportsTab({required this.reports, required this.onClose, required this.onCreateRecord, required this.onRefresh});

  @override
  Widget build(BuildContext context) {
    if (reports.isEmpty) {
      return RefreshIndicator(
        onRefresh: onRefresh,
        child: ListView(
          children: const [
            SizedBox(height: 80),
            Center(child: Text('Không có báo cáo nào chờ tiếp nhận.', style: TextStyle(color: Colors.white54))),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: reports.length,
        itemBuilder: (context, index) {
          final r = reports[index];
          return Card(
            margin: const EdgeInsets.only(bottom: 10),
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(child: Text(r.id, style: const TextStyle(fontWeight: FontWeight.w600))),
                      Text(r.status == 'DRAFTING' ? 'Đang soạn nháp' : 'Chờ tiếp nhận',
                          style: const TextStyle(fontSize: 11, color: Colors.white54)),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text('Lớp ${r.className ?? '—'} · ${r.description ?? ''}', style: const TextStyle(fontSize: 13)),
                  const SizedBox(height: 2),
                  Text(
                    '${r.suspectedMealText ?? '—'} (${r.mealDate ?? '—'}) · ${r.reportedByLabel ?? '—'}',
                    style: const TextStyle(fontSize: 11, color: Colors.white54),
                  ),
                  if (r.symptoms.isNotEmpty)
                    Padding(
                      padding: const EdgeInsets.only(top: 6),
                      child: Wrap(
                        spacing: 6,
                        children: [for (final s in r.symptoms) Chip(label: Text(s, style: const TextStyle(fontSize: 11)))],
                      ),
                    ),
                  const SizedBox(height: 8),
                  if (r.status == 'PENDING_TRIAGE')
                    Row(
                      children: [
                        TextButton(onPressed: () => onClose(r), child: const Text('Đóng — Không liên quan ATTP')),
                        const Spacer(),
                        ElevatedButton(onPressed: () => onCreateRecord(r), child: const Text('Tạo biên bản')),
                      ],
                    ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class _RecordsTab extends StatelessWidget {
  final List<IncidentRecord> records;
  final ValueChanged<IncidentRecord> onClose;
  final ValueChanged<IncidentRecord> onOpenInvestigation;
  final Future<void> Function() onRefresh;
  const _RecordsTab({required this.records, required this.onClose, required this.onOpenInvestigation, required this.onRefresh});

  @override
  Widget build(BuildContext context) {
    if (records.isEmpty) {
      return RefreshIndicator(
        onRefresh: onRefresh,
        child: ListView(
          children: const [
            SizedBox(height: 80),
            Center(child: Text('Chưa có biên bản nào chờ xử lý.', style: TextStyle(color: Colors.white54))),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: records.length,
        itemBuilder: (context, index) {
          final rec = records[index];
          return Card(
            margin: const EdgeInsets.only(bottom: 10),
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(rec.id, style: const TextStyle(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 4),
                  Text(rec.content ?? '', maxLines: 3, overflow: TextOverflow.ellipsis, style: const TextStyle(fontSize: 12)),
                  const SizedBox(height: 4),
                  Wrap(spacing: 6, children: [for (final rid in rec.sourceReportCodes) Chip(label: Text(rid, style: const TextStyle(fontSize: 11)))]),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      TextButton(onPressed: () => onClose(rec), child: const Text('Đóng')),
                      const Spacer(),
                      ElevatedButton(onPressed: () => onOpenInvestigation(rec), child: const Text('Mở phiên điều tra')),
                    ],
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class _ProcessingTab extends StatelessWidget {
  final List<Incident> incidents;
  final Future<void> Function() onRefresh;
  const _ProcessingTab({required this.incidents, required this.onRefresh});

  @override
  Widget build(BuildContext context) {
    if (incidents.isEmpty) {
      return RefreshIndicator(
        onRefresh: onRefresh,
        child: ListView(
          children: const [
            SizedBox(height: 80),
            Center(child: Text('Không có phiên điều tra nào đang xử lý.', style: TextStyle(color: Colors.white54))),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: incidents.length,
        itemBuilder: (context, index) {
          final i = incidents[index];
          return Card(
            margin: const EdgeInsets.only(bottom: 10),
            child: ListTile(
              title: Text(i.title ?? i.id, style: const TextStyle(fontWeight: FontWeight.w600)),
              subtitle: Text(
                '${i.id} · ${i.affectedStudentsCount} HS ảnh hưởng · ${i.status ?? '—'}',
                style: const TextStyle(fontSize: 12),
              ),
              trailing: i.severity != null && i.severity != 'PENDING'
                  ? Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: AppTheme.dangerRose.withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(i.severity!, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 11)),
                    )
                  : null,
              onTap: () =>
                  Navigator.of(context).push(MaterialPageRoute(builder: (_) => IncidentDetailScreen(code: i.id))).then((_) => onRefresh()),
            ),
          );
        },
      ),
    );
  }
}

class _ClosedTab extends StatelessWidget {
  final List<SourceReport> reports;
  final List<Incident> incidents;
  final Future<void> Function() onRefresh;
  const _ClosedTab({required this.reports, required this.incidents, required this.onRefresh});

  @override
  Widget build(BuildContext context) {
    final rows = <Widget>[
      for (final r in reports)
        Card(
          margin: const EdgeInsets.only(bottom: 8),
          child: ListTile(
            title: Text(r.id),
            subtitle: Text('Báo cáo (RPT) · ${r.description ?? ''}', style: const TextStyle(fontSize: 12)),
            trailing: const Text('Không liên quan ATTP', style: TextStyle(fontSize: 11, color: Colors.white54)),
          ),
        ),
      for (final i in incidents)
        Card(
          margin: const EdgeInsets.only(bottom: 8),
          child: ListTile(
            title: Text(i.id),
            subtitle: Text('Sự cố (INC) · ${i.title ?? ''}', style: const TextStyle(fontSize: 12)),
            trailing: const Text('Đã hoàn tất', style: TextStyle(fontSize: 11, color: Colors.white54)),
            onTap: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => IncidentDetailScreen(code: i.id))),
          ),
        ),
    ];
    if (rows.isEmpty) {
      return RefreshIndicator(
        onRefresh: onRefresh,
        child: ListView(
          children: const [
            SizedBox(height: 80),
            Center(child: Text('Chưa có hồ sơ nào đã đóng.', style: TextStyle(color: Colors.white54))),
          ],
        ),
      );
    }
    return RefreshIndicator(
      onRefresh: onRefresh,
      child: ListView(padding: const EdgeInsets.all(16), children: rows),
    );
  }
}
