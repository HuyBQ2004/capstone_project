import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/inspection_models.dart';
import '../data/inspection_repository.dart';
import 'samples_register_screen.dart';

const _quantityUnits = ['kg', 'lít', 'gam', 'ml', 'quả', 'hộp', 'bó', 'thùng'];
const _storageOptions = ['Thường', 'Lạnh', 'Đông'];

/// Phiên kiểm thực 3 bước (Mẫu 1/2/3 — QĐ 1246/QĐ-BYT). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inspections/[id]/page.tsx`.
/// Rút gọn cho mobile: nhập tay từng dòng (không tự điền theo catalog kho/thực
/// đơn — cần N lượt gọi API tra cứu, để lại cho bản Web), vẫn giữ ĐÚNG cấu
/// trúc 3 bước pháp lý + tiêu chí Đạt/Không đạt + ảnh/lý do bắt buộc khi
/// Không đạt (BR-083/090/094/095/098).
class InspectionDetailScreen extends StatefulWidget {
  final String code;
  const InspectionDetailScreen({super.key, required this.code});

  @override
  State<InspectionDetailScreen> createState() => _InspectionDetailScreenState();
}

class _InspectionDetailScreenState extends State<InspectionDetailScreen> {
  bool _loading = true;
  String? _error;
  InspectionSession? _session;
  int _activeStep = 0;

  List<Step1FreshRow> _fresh = [];
  List<Step1DryRow> _dry = [];
  List<Step2Row> _rows2 = [];
  List<Step3Row> _rows3 = [];

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
      final s = await InspectionRepository.instance.fetchDetail(widget.code);
      setState(() {
        _session = s;
        _fresh = s.step1Fresh.isNotEmpty ? s.step1Fresh : [Step1FreshRow()];
        _dry = s.step1Dry;
        _rows2 = s.step2Rows.isNotEmpty ? s.step2Rows : [Step2Row(shift: mealTypeLabel[s.meal] ?? '')];
        _rows3 = s.step3Rows.isNotEmpty ? s.step3Rows : [Step3Row(shift: mealTypeLabel[s.meal] ?? '')];
        final statuses = [s.step1, s.step2, s.step3];
        final doneStatuses = {'PASSED', 'FAILED'};
        if (doneStatuses.contains(statuses[0]) && doneStatuses.contains(statuses[1])) {
          _activeStep = 2;
        } else if (doneStatuses.contains(statuses[0])) {
          _activeStep = 1;
        } else {
          _activeStep = 0;
        }
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được phiên kiểm thực. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  bool get _step1LawValid =>
      _fresh.every((r) => r.name.trim().isNotEmpty && r.receivedAt.trim().isNotEmpty && r.quantity.trim().isNotEmpty && r.supplierName.trim().isNotEmpty) &&
      _dry.every((r) => r.name.trim().isNotEmpty && r.receivedAt.trim().isNotEmpty && r.expiry.trim().isNotEmpty);
  bool get _step2LawValid => _rows2.every((r) =>
      r.dishName.trim().isNotEmpty && r.mainIngredients.trim().isNotEmpty && r.servings.trim().isNotEmpty && r.prepDoneAt.trim().isNotEmpty && r.cookDoneAt.trim().isNotEmpty);
  // `utensils` KHÔNG còn bắt buộc: cột 7 nay là đánh giá đạt/không đạt (`utensilsPass`), ô chữ
  // chỉ là ghi chú. Bắt buộc nó thì người dùng phải gõ một câu vô nghĩa mới nộp được biểu mẫu.
  bool get _step3LawValid => _rows3.every((r) =>
      r.dishName.trim().isNotEmpty && r.servings.trim().isNotEmpty && r.dividedDoneAt.trim().isNotEmpty && r.eatStartAt.trim().isNotEmpty);

  bool get _step1RowsFailing => _fresh.any((r) => r.fails) || _dry.any((r) => r.fails);
  bool get _step2RowsFailing => _rows2.any((r) => r.fails);
  bool get _step3RowsFailing => _rows3.any((r) => r.fails);

  bool get _step1ActionMissing => _fresh.any((r) => r.fails && r.action.trim().isEmpty) || _dry.any((r) => r.fails && r.action.trim().isEmpty);
  bool get _step2ActionMissing => _rows2.any((r) => r.fails && r.action.trim().isEmpty);
  bool get _step3ActionMissing => _rows3.any((r) => r.fails && r.action.trim().isEmpty);

  bool get _step1PhotoMissing => _fresh.any((r) => r.fails && r.photoCount == 0) || _dry.any((r) => r.fails && r.photoCount == 0);
  bool get _step2PhotoMissing => _rows2.any((r) => r.fails && r.photoCount == 0);
  bool get _step3PhotoMissing => _rows3.any((r) => r.fails && r.photoCount == 0);

  /*
   * 13/08/2026 — trước đây `pickMultiImage()` cho chọn ảnh có sẵn trong thư viện, lách được
   * yêu cầu chụp trực tiếp bắt buộc cho ảnh bằng chứng bước kiểm thực (BR-083). Đổi sang
   * camera, mỗi lần bấm chụp 1 ảnh; bấm nút nhiều lần để thêm ảnh cho cùng một dòng.
   */
  Future<int> _pickAndUploadRowEvidence(String ownerType, int currentCount) async {
    final photo = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 85);
    if (photo == null) return currentCount;
    try {
      await InspectionRepository.instance.uploadEvidence(widget.code, ownerType, photo.path);
      return currentCount + 1;
    } catch (_) {
      // Ảnh lỗi không tính vào count — giữ nguyên đối chiếu web (BR-083).
      return currentCount;
    }
  }

  /// BR-083 — GPS geofence khi nộp bước kiểm thực. Đối chiếu
  /// `04_Development/Frontend/src/app/(app)/service-caterers/inspections/[id]/page.tsx`
  /// (nút "Lấy vị trí GPS"). Trên mobile lấy TỰ ĐỘNG, âm thầm bỏ qua nếu
  /// người dùng từ chối quyền hoặc thiết bị không có GPS — geofence phía
  /// server hiện chỉ enforce khi trường đã cấu hình toạ độ, còn lại là no-op,
  /// nên KHÔNG được chặn nộp bước chỉ vì thiếu GPS.
  Future<Position?> _tryGetPosition() async {
    try {
      if (!await Geolocator.isLocationServiceEnabled()) return null;
      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.denied || permission == LocationPermission.deniedForever) {
        return null;
      }
      return await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(accuracy: LocationAccuracy.high, timeLimit: Duration(seconds: 8)),
      );
    } catch (_) {
      return null;
    }
  }

  Future<void> _submitStep(int stepNo, {required bool pass, double? coreTemp, double? holdingTemp, String? failReason, String? severity, String? witness}) async {
    final s = _session;
    if (s == null) return;
    try {
      final position = await _tryGetPosition();
      final updated = await InspectionRepository.instance.submitStep(
        widget.code,
        stepNo,
        step1Fresh: _fresh,
        step1Dry: _dry,
        step2Rows: _rows2,
        step3Rows: _rows3,
        result: pass ? 'PASSED' : 'FAILED',
        coreTemp: coreTemp,
        holdingTemp: holdingTemp,
        failReason: failReason,
        failSeverity: severity,
        failWitness: witness,
        gpsLat: position?.latitude,
        gpsLng: position?.longitude,
      );
      if (!mounted) return;
      setState(() {
        _session = updated;
        if (pass && stepNo < 3) _activeStep = stepNo;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(pass ? 'Bước $stepNo: Đạt — đã lưu bằng chứng.' : 'Bước $stepNo: Không đạt — chặn phục vụ (BR-094), đã tạo yêu cầu khắc phục (BR-095).'),
          backgroundColor: pass ? AppTheme.successEmerald : AppTheme.dangerRose,
        ),
      );
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Không ghi được kết quả bước kiểm thực.')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final s = _session;
    return Scaffold(
      appBar: AppBar(title: Text(s == null ? 'Phiên kiểm thực' : '${mealTypeLabel[s.meal] ?? s.meal} · ${s.date ?? ''}')),
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
                : s == null
                    ? const SizedBox.shrink()
                    : _buildBody(s),
      ),
    );
  }

  Widget _buildBody(InspectionSession s) {
    final statuses = [s.step1 ?? 'PENDING', s.step2 ?? 'PENDING', s.step3 ?? 'PENDING'];
    final anyFailed = statuses.contains('FAILED');
    final allPassed = statuses.every((st) => st == 'PASSED');

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        if (anyFailed)
          _Banner(
            color: AppTheme.dangerRose,
            icon: Icons.report_gmailerrorred_outlined,
            text: 'Có bước Không đạt — bữa ăn bị chặn phục vụ (BR-094). Yêu cầu khắc phục đã tạo tự động (BR-095).',
          ),
        if (allPassed) ...[
          _Banner(
            color: AppTheme.successEmerald,
            icon: Icons.verified_outlined,
            text: 'Cả 3 bước Đạt — đủ điều kiện phục vụ, nhớ đăng ký lưu mẫu (BR-105).',
          ),
          const SizedBox(height: 8),
          OutlinedButton.icon(
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => RegisterSamplesForSessionScreen(session: s)),
            ),
            icon: const Icon(Icons.science_outlined),
            label: const Text('Đăng ký lưu mẫu cho phiên này'),
          ),
        ],
        const SizedBox(height: 8),
        _StepProgress(statuses: statuses, active: _activeStep, onTap: (i) => setState(() => _activeStep = i)),
        const SizedBox(height: 16),
        if (_activeStep == 0) _buildStep1(statuses[0], anyFailed),
        if (_activeStep == 1) _buildStep2(statuses, anyFailed),
        if (_activeStep == 2) _buildStep3(statuses, anyFailed),
      ],
    );
  }

  Widget _buildStep1(String status, bool anyFailed) {
    final done = status == 'PASSED' || status == 'FAILED';
    final locked = anyFailed && !done;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const _SectionHeader(icon: Icons.inventory_2_outlined, title: 'Kiểm tra trước khi chế biến', lawForm: 'Điều 3 · Mẫu số 1 — QĐ 1246'),
        const SizedBox(height: 16),
        const Text('I. Thực phẩm tươi sống / đông lạnh', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: Colors.white60)),
        const SizedBox(height: 8),
        for (var i = 0; i < _fresh.length; i++)
          _FreshRowCard(
            row: _fresh[i],
            enabled: !done,
            onRemove: _fresh.length > 1 ? () => setState(() => _fresh.removeAt(i)) : null,
            onChanged: () => setState(() {}),
            onPickPhotos: () async {
              final n = await _pickAndUploadRowEvidence('STEP1_FRESH', _fresh[i].photoCount);
              setState(() => _fresh[i].photoCount = n);
            },
          ),
        if (!done)
          OutlinedButton.icon(
            onPressed: () => setState(() => _fresh.add(Step1FreshRow())),
            icon: const Icon(Icons.add),
            label: const Text('Thêm thực phẩm tươi sống'),
          ),
        const SizedBox(height: 16),
        const Text('II. Thực phẩm khô / bao gói sẵn', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: Colors.white60)),
        const SizedBox(height: 8),
        for (var i = 0; i < _dry.length; i++)
          _DryRowCard(
            row: _dry[i],
            enabled: !done,
            onRemove: () => setState(() => _dry.removeAt(i)),
            onChanged: () => setState(() {}),
            onPickPhotos: () async {
              final n = await _pickAndUploadRowEvidence('STEP1_DRY', _dry[i].photoCount);
              setState(() => _dry[i].photoCount = n);
            },
          ),
        if (!done)
          OutlinedButton.icon(
            onPressed: () => setState(() => _dry.add(Step1DryRow())),
            icon: const Icon(Icons.add),
            label: const Text('Thêm TP khô / bao gói sẵn'),
          ),
        const SizedBox(height: 8),
        if (locked)
          const Text('Bước này chờ tái kiểm bước Không đạt trước đó (BR-096).', style: TextStyle(color: Colors.white54, fontSize: 12)),
        if (!locked)
          _StepResultBlock(
            stepNo: 1,
            done: done,
            withTemp: null,
            lawValid: _step1LawValid && _fresh.isNotEmpty,
            rowsFailing: _step1RowsFailing,
            actionMissing: _step1ActionMissing,
            photoMissing: _step1PhotoMissing,
            code: widget.code,
            onPass: (_, __) => _submitStep(1, pass: true),
            onFail: (reason, severity, witness) => _submitStep(1, pass: false, failReason: reason, severity: severity, witness: witness),
          ),
      ],
    );
  }

  Widget _buildStep2(List<String> statuses, bool anyFailed) {
    final done = statuses[1] == 'PASSED' || statuses[1] == 'FAILED';
    final locked = statuses[0] != 'PASSED' || (anyFailed && !done);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const _SectionHeader(icon: Icons.soup_kitchen_outlined, title: 'Kiểm tra khi chế biến thức ăn', lawForm: 'Điều 4 · Mẫu số 2 — QĐ 1246'),
        const SizedBox(height: 16),
        if (locked)
          Text(
            statuses[0] != 'PASSED' ? 'Bước này mở sau khi Bước 1 Đạt (BR-079).' : 'Chờ tái kiểm bước Không đạt trước đó (BR-096).',
            style: const TextStyle(color: Colors.white54, fontSize: 12),
          )
        else ...[
          for (var i = 0; i < _rows2.length; i++)
            _Step2RowCard(
              row: _rows2[i],
              enabled: !done,
              onRemove: _rows2.length > 1 ? () => setState(() => _rows2.removeAt(i)) : null,
              onChanged: () => setState(() {}),
              onPickPhotos: () async {
                final n = await _pickAndUploadRowEvidence('STEP2', _rows2[i].photoCount);
                setState(() => _rows2[i].photoCount = n);
              },
            ),
          if (!done)
            OutlinedButton.icon(
              onPressed: () => setState(() => _rows2.add(Step2Row())),
              icon: const Icon(Icons.add),
              label: const Text('Thêm món ăn'),
            ),
          const SizedBox(height: 8),
          _StepResultBlock(
            stepNo: 2,
            done: done,
            withTemp: const (label: 'Nhiệt độ lõi (°C) — tối thiểu 75°C (BR-090)', min: 75.0),
            lawValid: _step2LawValid && _rows2.isNotEmpty,
            rowsFailing: _step2RowsFailing,
            actionMissing: _step2ActionMissing,
            photoMissing: _step2PhotoMissing,
            code: widget.code,
            onPass: (temp, _) => _submitStep(2, pass: true, coreTemp: temp),
            onFail: (reason, severity, witness) => _submitStep(2, pass: false, failReason: reason, severity: severity, witness: witness),
          ),
        ],
      ],
    );
  }

  Widget _buildStep3(List<String> statuses, bool anyFailed) {
    final done = statuses[2] == 'PASSED' || statuses[2] == 'FAILED';
    final locked = statuses[1] != 'PASSED' || (anyFailed && !done);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const _SectionHeader(icon: Icons.restaurant_outlined, title: 'Kiểm tra trước khi ăn', lawForm: 'Điều 5 · Mẫu số 3 — QĐ 1246'),
        const SizedBox(height: 16),
        if (locked)
          Text(
            statuses[1] != 'PASSED' ? 'Bước này mở sau khi Bước 2 Đạt (BR-079).' : 'Chờ tái kiểm bước Không đạt trước đó (BR-096).',
            style: const TextStyle(color: Colors.white54, fontSize: 12),
          )
        else ...[
          for (var i = 0; i < _rows3.length; i++)
            _Step3RowCard(
              row: _rows3[i],
              enabled: !done,
              onRemove: _rows3.length > 1 ? () => setState(() => _rows3.removeAt(i)) : null,
              onChanged: () => setState(() {}),
              onPickPhotos: () async {
                final n = await _pickAndUploadRowEvidence('STEP3', _rows3[i].photoCount);
                setState(() => _rows3[i].photoCount = n);
              },
            ),
          if (!done)
            OutlinedButton.icon(
              onPressed: () => setState(() => _rows3.add(Step3Row())),
              icon: const Icon(Icons.add),
              label: const Text('Thêm món ăn'),
            ),
          const SizedBox(height: 8),
          _StepResultBlock(
            stepNo: 3,
            done: done,
            withTemp: const (label: 'Nhiệt độ giữ nóng (°C) — tối thiểu 60°C (BR-098)', min: 60.0),
            lawValid: _step3LawValid && _rows3.isNotEmpty,
            rowsFailing: _step3RowsFailing,
            actionMissing: _step3ActionMissing,
            photoMissing: _step3PhotoMissing,
            code: widget.code,
            onPass: (_, holding) => _submitStep(3, pass: true, holdingTemp: holding),
            onFail: (reason, severity, witness) => _submitStep(3, pass: false, failReason: reason, severity: severity, witness: witness),
          ),
        ],
      ],
    );
  }
}

class _Banner extends StatelessWidget {
  final Color color;
  final String text;
  final IconData icon;
  const _Banner({required this.color, required this.text, this.icon = Icons.info_outline});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 20, color: color),
          const SizedBox(width: 10),
          Expanded(child: Text(text, style: TextStyle(color: color, fontSize: 13, height: 1.4))),
        ],
      ),
    );
  }
}

const _stepShortNames = ['Trước chế biến', 'Khi chế biến', 'Trước khi ăn'];

class _StepProgress extends StatelessWidget {
  final List<String> statuses;
  final int active;
  final ValueChanged<int> onTap;
  const _StepProgress({required this.statuses, required this.active, required this.onTap});

  Color _colorFor(String status, bool isActive) {
    if (status == 'PASSED') return AppTheme.successEmerald;
    if (status == 'FAILED') return AppTheme.dangerRose;
    if (isActive) return AppTheme.primarySky;
    return Colors.white30;
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 10),
      decoration: BoxDecoration(
        color: AppTheme.cardDark,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppTheme.borderDark),
      ),
      child: Row(
        children: [
          for (var i = 0; i < 3; i++) ...[
            Expanded(
              child: GestureDetector(
                behavior: HitTestBehavior.opaque,
                onTap: () => onTap(i),
                child: Column(
                  children: [
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        if (i > 0)
                          Expanded(
                            child: Container(
                              height: 2,
                              margin: const EdgeInsets.only(right: 2),
                              color: statuses[i - 1] == 'PASSED' ? AppTheme.successEmerald : Colors.white24,
                            ),
                          ),
                        CircleAvatar(
                          radius: active == i ? 20 : 18,
                          backgroundColor: _colorFor(statuses[i], active == i).withValues(alpha: active == i ? 0.22 : 0.14),
                          child: statuses[i] == 'PASSED'
                              ? Icon(Icons.check, color: _colorFor(statuses[i], active == i), size: 20)
                              : statuses[i] == 'FAILED'
                                  ? Icon(Icons.priority_high, color: _colorFor(statuses[i], active == i), size: 20)
                                  : Text('${i + 1}', style: TextStyle(color: _colorFor(statuses[i], active == i), fontWeight: FontWeight.bold, fontSize: 16)),
                        ),
                        if (i < 2)
                          Expanded(
                            child: Container(
                              height: 2,
                              margin: const EdgeInsets.only(left: 2),
                              color: statuses[i] == 'PASSED' ? AppTheme.successEmerald : Colors.white24,
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 6),
                    Text(
                      'Bước ${i + 1}',
                      style: TextStyle(fontSize: 12, fontWeight: active == i ? FontWeight.bold : FontWeight.w500, color: active == i ? Colors.white : Colors.white60),
                    ),
                    Text(
                      _stepShortNames[i],
                      textAlign: TextAlign.center,
                      style: const TextStyle(fontSize: 10, color: Colors.white38),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

/// 13/08/2026 (Nam) — tiêu đề mỗi bước, gộp icon + tên + huy hiệu điều luật vào
/// một khối nhất quán thay vì chỉ 1 dòng chữ đậm — dễ phân biệt đang ở bước
/// nào khi cuộn dài qua nhiều dòng mẫu biểu.
class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String title;
  final String lawForm;
  const _SectionHeader({required this.icon, required this.title, required this.lawForm});

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(color: AppTheme.primarySky.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(10)),
          child: Icon(icon, color: AppTheme.primarySky, size: 20),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
              const SizedBox(height: 3),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(color: AppTheme.primarySky.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(999)),
                child: Text(lawForm, style: const TextStyle(fontSize: 10, color: AppTheme.primarySky, fontWeight: FontWeight.w600)),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

/// Đường kẻ phân nhóm nhỏ trong 1 card dòng — tách phần "thông tin" khỏi phần
/// "tiêu chí đạt/không đạt" cho đỡ rối mắt khi card có nhiều trường.
class _RowDivider extends StatelessWidget {
  final String label;
  const _RowDivider({required this.label});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 10, bottom: 4),
      child: Row(
        children: [
          Text(label.toUpperCase(), style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w700, color: Colors.white38, letterSpacing: 0.4)),
          const SizedBox(width: 8),
          Expanded(child: Container(height: 1, color: AppTheme.borderDark)),
        ],
      ),
    );
  }
}

typedef _TempMeta = ({String label, double min});

class _StepResultBlock extends StatefulWidget {
  final int stepNo;
  final bool done;
  final _TempMeta? withTemp;
  final bool lawValid;
  final bool rowsFailing;
  final bool actionMissing;
  final bool photoMissing;
  final String code;
  final void Function(double? coreTemp, double? holdingTemp) onPass;
  final void Function(String reason, String severity, String witness) onFail;

  const _StepResultBlock({
    required this.stepNo,
    required this.done,
    required this.withTemp,
    required this.lawValid,
    required this.rowsFailing,
    required this.actionMissing,
    required this.photoMissing,
    required this.code,
    required this.onPass,
    required this.onFail,
  });

  @override
  State<_StepResultBlock> createState() => _StepResultBlockState();
}

class _StepResultBlockState extends State<_StepResultBlock> {
  final _tempCtrl = TextEditingController();
  final _failReasonCtrl = TextEditingController();
  final _witnessCtrl = TextEditingController();
  String _severity = 'MEDIUM';
  int _stepPhotoCount = 0;
  bool _failing = false;
  String? _error;

  @override
  void dispose() {
    _tempCtrl.dispose();
    _failReasonCtrl.dispose();
    _witnessCtrl.dispose();
    super.dispose();
  }

  /*
   * 13/08/2026 — cùng lý do với `_pickAndUploadRowEvidence` ở trên: đổi từ chọn thư viện
   * sang chụp trực tiếp (BR-083), mỗi lần bấm chụp 1 ảnh.
   */
  Future<void> _pickStepPhotos() async {
    final photo = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 85);
    if (photo == null) return;
    try {
      await InspectionRepository.instance.uploadEvidence(widget.code, 'SESSION', photo.path);
      if (mounted) setState(() => _stepPhotoCount += 1);
    } catch (_) {}
  }

  bool _validateCommon() {
    if (!widget.lawValid) {
      setState(() => _error = 'Chưa điền đủ trường bắt buộc của mẫu biểu (tên, thời gian, khối lượng từng dòng).');
      return false;
    }
    if (widget.actionMissing) {
      setState(() => _error = 'Dòng Không đạt phải ghi rõ biện pháp xử lý.');
      return false;
    }
    if (widget.photoMissing) {
      setState(() => _error = 'Món/thực phẩm Không đạt phải có ảnh bằng chứng ngay tại dòng đó (BR-083).');
      return false;
    }
    if (_stepPhotoCount == 0) {
      setState(() => _error = 'Ảnh bằng chứng là bắt buộc cho mỗi bước (BR-083).');
      return false;
    }
    return true;
  }

  void _handlePass() {
    if (widget.rowsFailing) {
      setState(() => _error = 'Mẫu biểu còn dòng Không đạt — bước này chỉ có thể ghi Không đạt.');
      return;
    }
    if (!_validateCommon()) return;
    double? temp;
    if (widget.withTemp != null) {
      final v = double.tryParse(_tempCtrl.text.trim());
      if (v == null) {
        setState(() => _error = '${widget.withTemp!.label} là bắt buộc.');
        return;
      }
      if (v < widget.withTemp!.min) {
        setState(() => _error = '${v}°C dưới ngưỡng ${widget.withTemp!.min}°C — không thể Đạt.');
        return;
      }
      temp = v;
    }
    setState(() => _error = null);
    widget.onPass(widget.stepNo == 2 ? temp : null, widget.stepNo == 3 ? temp : null);
  }

  void _handleFail() {
    if (_failReasonCtrl.text.trim().isEmpty) {
      setState(() => _error = 'Lý do không đạt là bắt buộc.');
      return;
    }
    if (!_validateCommon()) return;
    if (_severity != 'LOW' && _witnessCtrl.text.trim().isEmpty) {
      setState(() => _error = 'Nhân chứng là bắt buộc với vi phạm mức Trung bình/Cao (BR-183).');
      return;
    }
    setState(() => _error = null);
    widget.onFail(_failReasonCtrl.text.trim(), _severity, _witnessCtrl.text.trim());
  }

  @override
  Widget build(BuildContext context) {
    if (widget.done) return const SizedBox.shrink();
    return Container(
      margin: const EdgeInsets.only(top: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppTheme.cardDark,
        border: Border.all(color: AppTheme.borderDark),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.fact_check_outlined, size: 18, color: AppTheme.primarySky),
              const SizedBox(width: 8),
              const Text('Chốt kết quả bước', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
            ],
          ),
          if (_error != null) ...[
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(color: AppTheme.dangerRose.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(8)),
              child: Text(_error!, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 12)),
            ),
          ],
          if (widget.rowsFailing) ...[
            const SizedBox(height: 8),
            const Text('Có dòng Không đạt trong mẫu biểu — chỉ có thể ghi Không đạt.', style: TextStyle(color: AppTheme.warningAmber, fontSize: 12)),
          ],
          const SizedBox(height: 10),
          if (widget.withTemp != null)
            TextField(
              controller: _tempCtrl,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: InputDecoration(labelText: widget.withTemp!.label, border: const OutlineInputBorder()),
            ),
          const SizedBox(height: 10),
          OutlinedButton.icon(
            onPressed: _pickStepPhotos,
            icon: const Icon(Icons.camera_alt_outlined, size: 16),
            label: Text('Ảnh bằng chứng bước ($_stepPhotoCount)'),
          ),
          const SizedBox(height: 12),
          if (!_failing)
            Row(
              children: [
                ElevatedButton(onPressed: widget.rowsFailing ? null : _handlePass, child: const Text('Xác nhận Đạt')),
                const SizedBox(width: 8),
                OutlinedButton(
                  onPressed: () => setState(() => _failing = true),
                  style: OutlinedButton.styleFrom(foregroundColor: AppTheme.dangerRose),
                  child: const Text('Ghi Không đạt'),
                ),
              ],
            )
          else
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                TextField(
                  controller: _failReasonCtrl,
                  minLines: 2,
                  maxLines: 3,
                  decoration: const InputDecoration(labelText: 'Lý do không đạt *', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  initialValue: _severity,
                  decoration: const InputDecoration(labelText: 'Mức độ', border: OutlineInputBorder()),
                  dropdownColor: AppTheme.cardDark,
                  items: const [
                    DropdownMenuItem(value: 'LOW', child: Text('Thấp')),
                    DropdownMenuItem(value: 'MEDIUM', child: Text('Trung bình')),
                    DropdownMenuItem(value: 'HIGH', child: Text('Cao')),
                  ],
                  onChanged: (v) {
                    if (v != null) setState(() => _severity = v);
                  },
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: _witnessCtrl,
                  decoration: InputDecoration(
                    labelText: _severity == 'LOW' ? 'Nhân chứng' : 'Nhân chứng * (BR-183)',
                    border: const OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    ElevatedButton(
                      onPressed: _handleFail,
                      style: ElevatedButton.styleFrom(backgroundColor: AppTheme.dangerRose),
                      child: const Text('Xác nhận Không đạt'),
                    ),
                    const SizedBox(width: 8),
                    TextButton(onPressed: () => setState(() => _failing = false), child: const Text('Hủy')),
                  ],
                ),
              ],
            ),
        ],
      ),
    );
  }
}

// ───────────────────────── Row editors ─────────────────────────

class _SmallField extends StatelessWidget {
  final String label;
  final String value;
  final ValueChanged<String> onChanged;
  final bool enabled;
  const _SmallField({required this.label, required this.value, required this.onChanged, this.enabled = true});

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      initialValue: value,
      enabled: enabled,
      onChanged: onChanged,
      style: const TextStyle(fontSize: 13),
      decoration: InputDecoration(labelText: label, isDense: true, border: const OutlineInputBorder()),
    );
  }
}

class _CriterionToggle extends StatelessWidget {
  final String label;
  final String failHint;
  final bool value;
  final ValueChanged<bool> onChanged;
  final bool enabled;
  const _CriterionToggle({required this.label, required this.failHint, required this.value, required this.onChanged, this.enabled = true});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(top: 8),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        border: Border.all(color: value ? Colors.white24 : AppTheme.dangerRose.withValues(alpha: 0.6), width: value ? 1 : 1.5),
        borderRadius: BorderRadius.circular(10),
        color: value ? null : AppTheme.dangerRose.withValues(alpha: 0.07),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
          const SizedBox(height: 6),
          Row(
            children: [
              Expanded(
                child: ChoiceChip(
                  avatar: value ? const Icon(Icons.check, size: 14, color: AppTheme.successEmerald) : null,
                  label: const Center(child: Text('Đạt', style: TextStyle(fontSize: 12))),
                  selectedColor: AppTheme.successEmerald.withValues(alpha: 0.2),
                  selected: value,
                  onSelected: enabled ? (_) => onChanged(true) : null,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: ChoiceChip(
                  avatar: !value ? const Icon(Icons.close, size: 14, color: AppTheme.dangerRose) : null,
                  label: const Center(child: Text('Không đạt', style: TextStyle(fontSize: 12))),
                  selectedColor: AppTheme.dangerRose.withValues(alpha: 0.2),
                  selected: !value,
                  onSelected: enabled ? (_) => onChanged(false) : null,
                ),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text('Không đạt khi: $failHint', style: const TextStyle(fontSize: 11, color: Colors.white54, height: 1.3)),
        ],
      ),
    );
  }
}

class _RowVerdict extends StatelessWidget {
  final bool fail;
  final int photoCount;
  final VoidCallback onPickPhotos;
  final String action;
  final ValueChanged<String> onAction;
  final bool enabled;
  const _RowVerdict({required this.fail, required this.photoCount, required this.onPickPhotos, required this.action, required this.onAction, this.enabled = true});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(top: 8),
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: (fail ? AppTheme.dangerRose : AppTheme.primarySky).withValues(alpha: 0.06),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(fail ? Icons.cancel : Icons.check_circle, size: 14, color: fail ? AppTheme.dangerRose : AppTheme.successEmerald),
              const SizedBox(width: 6),
              Text(fail ? 'KHÔNG ĐẠT' : 'Đạt', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: fail ? AppTheme.dangerRose : AppTheme.successEmerald)),
            ],
          ),
          if (fail) ...[
            const SizedBox(height: 6),
            OutlinedButton.icon(
              onPressed: enabled ? onPickPhotos : null,
              icon: const Icon(Icons.camera_alt_outlined, size: 14),
              label: Text('Ảnh minh chứng * ($photoCount)', style: const TextStyle(fontSize: 11)),
            ),
            const SizedBox(height: 6),
            _SmallField(label: 'Biện pháp xử lý *', value: action, onChanged: onAction, enabled: enabled),
          ] else ...[
            const SizedBox(height: 6),
            _SmallField(label: 'Ghi chú', value: action, onChanged: onAction, enabled: enabled),
          ],
        ],
      ),
    );
  }
}

class _QtyField extends StatelessWidget {
  final String value;
  final ValueChanged<String> onChanged;
  final bool enabled;
  const _QtyField({required this.value, required this.onChanged, this.enabled = true});

  @override
  Widget build(BuildContext context) {
    final m = RegExp(r'^([\d.,]*)\s*(.*)$').firstMatch(value);
    final num = m?.group(1) ?? '';
    final unitRaw = m?.group(2) ?? '';
    final unit = _quantityUnits.contains(unitRaw) ? unitRaw : 'kg';
    return Row(
      children: [
        Expanded(
          child: TextFormField(
            initialValue: num,
            enabled: enabled,
            keyboardType: TextInputType.number,
            style: const TextStyle(fontSize: 13),
            decoration: const InputDecoration(labelText: 'Khối lượng *', isDense: true, border: OutlineInputBorder()),
            onChanged: (v) => onChanged('$v $unit'.trim()),
          ),
        ),
        const SizedBox(width: 6),
        DropdownButton<String>(
          value: unit,
          items: _quantityUnits.map((u) => DropdownMenuItem(value: u, child: Text(u))).toList(),
          onChanged: enabled ? (v) => onChanged('$num $v'.trim()) : null,
        ),
      ],
    );
  }
}

/// 13/08/2026 (Nam) — tiêu đề chung cho mọi thẻ dòng (nguyên liệu/món), thay
/// cho dòng chữ xám nhỏ lặp lại 4 nơi. Icon loại dòng + số thứ tự + trạng thái
/// Đạt/Không đạt ngay trên đầu — không phải kéo xuống cuối card mới biết.
class _RowCardHeader extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool fails;
  final VoidCallback? onRemove;
  const _RowCardHeader({required this.icon, required this.label, required this.fails, this.onRemove});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 16, color: Colors.white38),
        const SizedBox(width: 6),
        Expanded(child: Text(label, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: Colors.white54))),
        if (fails)
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
            decoration: BoxDecoration(color: AppTheme.dangerRose.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(999)),
            child: const Text('Không đạt', style: TextStyle(fontSize: 10, color: AppTheme.dangerRose, fontWeight: FontWeight.w600)),
          ),
        if (onRemove != null) IconButton(icon: const Icon(Icons.delete_outline, size: 18), onPressed: onRemove, visualDensity: VisualDensity.compact),
      ],
    );
  }
}

class _FreshRowCard extends StatelessWidget {
  final Step1FreshRow row;
  final bool enabled;
  final VoidCallback? onRemove;
  final VoidCallback onChanged;
  final VoidCallback onPickPhotos;
  const _FreshRowCard({required this.row, required this.enabled, required this.onRemove, required this.onChanged, required this.onPickPhotos});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      color: row.fails ? AppTheme.dangerRose.withValues(alpha: 0.05) : null,
      child: Padding(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _RowCardHeader(icon: Icons.set_meal_outlined, label: 'Thực phẩm tươi sống', fails: row.fails, onRemove: onRemove != null && enabled ? onRemove : null),
            const SizedBox(height: 8),
            _SmallField(label: 'Tên thực phẩm *', value: row.name, enabled: enabled, onChanged: (v) { row.name = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Thời gian nhập *', value: row.receivedAt, enabled: enabled, onChanged: (v) { row.receivedAt = v; onChanged(); }),
            const SizedBox(height: 8),
            _QtyField(value: row.quantity, enabled: enabled, onChanged: (v) { row.quantity = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Nơi cung cấp *', value: row.supplierName, enabled: enabled, onChanged: (v) { row.supplierName = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Địa chỉ / điện thoại NCC', value: row.supplierContact, enabled: enabled, onChanged: (v) { row.supplierContact = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Người giao hàng', value: row.deliverer, enabled: enabled, onChanged: (v) { row.deliverer = v; onChanged(); }),
            const _RowDivider(label: 'Giấy tờ'),
            Wrap(
              spacing: 12,
              children: [
                _CheckMini(label: 'Hóa đơn', value: row.hasInvoice, enabled: enabled, onChanged: (v) { row.hasInvoice = v; onChanged(); }),
                _CheckMini(label: 'GCN thú y', value: row.hasVetCert, enabled: enabled, onChanged: (v) { row.hasVetCert = v; onChanged(); }),
                _CheckMini(label: 'GCN kiểm dịch', value: row.hasQuarantineCert, enabled: enabled, onChanged: (v) { row.hasQuarantineCert = v; onChanged(); }),
              ],
            ),
            const _RowDivider(label: 'Đánh giá'),
            _CriterionToggle(
              label: 'Kiểm tra cảm quan',
              failHint: 'màu/mùi bất thường, dập nát, chảy nhớt, rã đông chảy nước.',
              value: row.sensoryPass,
              enabled: enabled,
              onChanged: (v) { row.sensoryPass = v; onChanged(); },
            ),
            const SizedBox(height: 6),
            DropdownButtonFormField<String>(
              initialValue: row.quickTest,
              decoration: const InputDecoration(labelText: 'Xét nghiệm nhanh (nếu có)', isDense: true, border: OutlineInputBorder()),
              dropdownColor: AppTheme.cardDark,
              items: const [
                DropdownMenuItem(value: 'NONE', child: Text('Không thực hiện')),
                DropdownMenuItem(value: 'PASS', child: Text('Đạt')),
                DropdownMenuItem(value: 'FAIL', child: Text('Không đạt')),
              ],
              onChanged: enabled ? (v) { if (v != null) { row.quickTest = v; onChanged(); } } : null,
            ),
            _RowVerdict(fail: row.fails, photoCount: row.photoCount, onPickPhotos: onPickPhotos, action: row.action, enabled: enabled, onAction: (v) { row.action = v; onChanged(); }),
          ],
        ),
      ),
    );
  }
}

class _DryRowCard extends StatelessWidget {
  final Step1DryRow row;
  final bool enabled;
  final VoidCallback onRemove;
  final VoidCallback onChanged;
  final VoidCallback onPickPhotos;
  const _DryRowCard({required this.row, required this.enabled, required this.onRemove, required this.onChanged, required this.onPickPhotos});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      color: row.fails ? AppTheme.dangerRose.withValues(alpha: 0.05) : null,
      child: Padding(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _RowCardHeader(icon: Icons.inventory_outlined, label: 'TP khô / bao gói sẵn', fails: row.fails, onRemove: enabled ? onRemove : null),
            const SizedBox(height: 8),
            _SmallField(label: 'Tên thực phẩm *', value: row.name, enabled: enabled, onChanged: (v) { row.name = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Cơ sở sản xuất', value: row.producer, enabled: enabled, onChanged: (v) { row.producer = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Thời gian nhập *', value: row.receivedAt, enabled: enabled, onChanged: (v) { row.receivedAt = v; onChanged(); }),
            const SizedBox(height: 8),
            _QtyField(value: row.quantity, enabled: enabled, onChanged: (v) { row.quantity = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Hạn sử dụng *', value: row.expiry, enabled: enabled, onChanged: (v) { row.expiry = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Nơi cung cấp', value: row.supplierName, enabled: enabled, onChanged: (v) { row.supplierName = v; onChanged(); }),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              initialValue: _storageOptions.contains(row.storage) ? row.storage : 'Thường',
              decoration: const InputDecoration(labelText: 'Bảo quản', isDense: true, border: OutlineInputBorder()),
              dropdownColor: AppTheme.cardDark,
              items: _storageOptions.map((o) => DropdownMenuItem(value: o, child: Text(o))).toList(),
              onChanged: enabled ? (v) { if (v != null) { row.storage = v; onChanged(); } } : null,
            ),
            const _RowDivider(label: 'Giấy tờ'),
            _CheckMini(label: 'Hóa đơn', value: row.hasInvoice, enabled: enabled, onChanged: (v) { row.hasInvoice = v; onChanged(); }),
            const _RowDivider(label: 'Đánh giá'),
            _CriterionToggle(
              label: 'Kiểm tra cảm quan',
              failHint: 'bao bì rách/phồng, mốc, hết hạn, nhãn thiếu thông tin.',
              value: row.sensoryPass,
              enabled: enabled,
              onChanged: (v) { row.sensoryPass = v; onChanged(); },
            ),
            _RowVerdict(fail: row.fails, photoCount: row.photoCount, onPickPhotos: onPickPhotos, action: row.action, enabled: enabled, onAction: (v) { row.action = v; onChanged(); }),
          ],
        ),
      ),
    );
  }
}

class _Step2RowCard extends StatelessWidget {
  final Step2Row row;
  final bool enabled;
  final VoidCallback? onRemove;
  final VoidCallback onChanged;
  final VoidCallback onPickPhotos;
  const _Step2RowCard({required this.row, required this.enabled, required this.onRemove, required this.onChanged, required this.onPickPhotos});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      color: row.fails ? AppTheme.dangerRose.withValues(alpha: 0.05) : null,
      child: Padding(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _RowCardHeader(icon: Icons.restaurant_menu_outlined, label: 'Món ăn', fails: row.fails, onRemove: onRemove != null && enabled ? onRemove : null),
            const SizedBox(height: 8),
            _SmallField(label: 'Tên món *', value: row.dishName, enabled: enabled, onChanged: (v) { row.dishName = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Nguyên liệu chính *', value: row.mainIngredients, enabled: enabled, onChanged: (v) { row.mainIngredients = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Số suất *', value: row.servings, enabled: enabled, onChanged: (v) { row.servings = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Sơ chế xong (giờ) *', value: row.prepDoneAt, enabled: enabled, onChanged: (v) { row.prepDoneAt = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Chế biến xong (giờ) *', value: row.cookDoneAt, enabled: enabled, onChanged: (v) { row.cookDoneAt = v; onChanged(); }),
            const _RowDivider(label: 'Đánh giá'),
            _CriterionToggle(label: 'Vệ sinh người chế biến', failHint: 'không đồ bảo hộ, móng dài, vết thương hở.', value: row.hygieneStaff, enabled: enabled, onChanged: (v) { row.hygieneStaff = v; onChanged(); }),
            _CriterionToggle(label: 'Vệ sinh dụng cụ, thiết bị', failHint: 'dùng chung dao/thớt sống-chín, chưa tiệt trùng.', value: row.hygieneEquipment, enabled: enabled, onChanged: (v) { row.hygieneEquipment = v; onChanged(); }),
            _CriterionToggle(label: 'Vệ sinh khu vực chế biến', failHint: 'sàn bẩn, côn trùng, rác không che đậy.', value: row.hygieneArea, enabled: enabled, onChanged: (v) { row.hygieneArea = v; onChanged(); }),
            _CriterionToggle(label: 'Cảm quan sau chế biến', failHint: 'màu/mùi/vị bất thường, chưa chín kỹ, lẫn dị vật.', value: row.sensoryPass, enabled: enabled, onChanged: (v) { row.sensoryPass = v; onChanged(); }),
            _RowVerdict(fail: row.fails, photoCount: row.photoCount, onPickPhotos: onPickPhotos, action: row.action, enabled: enabled, onAction: (v) { row.action = v; onChanged(); }),
          ],
        ),
      ),
    );
  }
}

class _Step3RowCard extends StatelessWidget {
  final Step3Row row;
  final bool enabled;
  final VoidCallback? onRemove;
  final VoidCallback onChanged;
  final VoidCallback onPickPhotos;
  const _Step3RowCard({required this.row, required this.enabled, required this.onRemove, required this.onChanged, required this.onPickPhotos});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      color: row.fails ? AppTheme.dangerRose.withValues(alpha: 0.05) : null,
      child: Padding(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _RowCardHeader(icon: Icons.restaurant_menu_outlined, label: 'Món ăn', fails: row.fails, onRemove: onRemove != null && enabled ? onRemove : null),
            const SizedBox(height: 8),
            _SmallField(label: 'Tên món *', value: row.dishName, enabled: enabled, onChanged: (v) { row.dishName = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Số suất *', value: row.servings, enabled: enabled, onChanged: (v) { row.servings = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Chia món xong (giờ) *', value: row.dividedDoneAt, enabled: enabled, onChanged: (v) { row.dividedDoneAt = v; onChanged(); }),
            const SizedBox(height: 8),
            _SmallField(label: 'Bắt đầu ăn (giờ) *', value: row.eatStartAt, enabled: enabled, onChanged: (v) { row.eatStartAt = v; onChanged(); }),
            const _RowDivider(label: 'Đánh giá'),
            // Cột 7 của Mẫu 3 là mục ĐÁNH GIÁ đạt/không đạt, không phải ô liệt kê tên dụng cụ
            // (Nam bắt lỗi này trên web 08/08/2026, mobile bị y hệt). Ô chữ bên dưới hạ xuống
            // thành ghi chú TUỲ CHỌN — hữu ích nhất khi ghi lý do lúc Không đạt.
            _CriterionToggle(
              label: 'Dụng cụ chia, chứa đựng, che đậy',
              failHint: 'dụng cụ bẩn, không che đậy, dùng chung đồ sống–chín.',
              value: row.utensilsPass,
              enabled: enabled,
              onChanged: (v) { row.utensilsPass = v; onChanged(); },
            ),
            _SmallField(label: 'Ghi chú dụng cụ (tuỳ chọn)', value: row.utensils, enabled: enabled, onChanged: (v) { row.utensils = v; onChanged(); }),
            const SizedBox(height: 6),
            _CriterionToggle(label: 'Cảm quan trước khi ăn', failHint: 'màu/mùi bất thường, nguội, dụng cụ chia không đảm bảo vệ sinh.', value: row.sensoryPass, enabled: enabled, onChanged: (v) { row.sensoryPass = v; onChanged(); }),
            _RowVerdict(fail: row.fails, photoCount: row.photoCount, onPickPhotos: onPickPhotos, action: row.action, enabled: enabled, onAction: (v) { row.action = v; onChanged(); }),
          ],
        ),
      ),
    );
  }
}

class _CheckMini extends StatelessWidget {
  final String label;
  final bool value;
  final bool enabled;
  final ValueChanged<bool> onChanged;
  const _CheckMini({required this.label, required this.value, required this.enabled, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: enabled ? () => onChanged(!value) : null,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(value ? Icons.check_box : Icons.check_box_outline_blank, size: 16, color: value ? AppTheme.primarySky : Colors.white38),
            const SizedBox(width: 4),
            Text(label, style: const TextStyle(fontSize: 11)),
          ],
        ),
      ),
    );
  }
}
