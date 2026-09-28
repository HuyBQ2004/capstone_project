import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/receiving_models.dart';
import '../data/receiving_repository.dart';

const _checklist = [
  'Nhiệt độ xe lạnh đạt yêu cầu vận chuyển',
  'Bao bì nguyên vẹn, không rách/móp/phồng',
  'Cảm quan đạt: màu, mùi, độ tươi',
  'Chứng từ nguồn gốc khớp với lô hàng thực nhận',
];

const _storageZones = ['Dry', 'Chilled', 'Frozen'];

/// Mở phiên nhận hàng từ MỘT chuyến giao ĐÃ GIAO (BR-070/071). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/receiving/new/page.tsx`.
/// GHI CHÚ TRIỂN KHAI: mobile chưa có package định vị (`geolocator`) trong
/// `pubspec.yaml` hiện tại — vĩ độ/kinh độ (bắt buộc theo
/// `ReceivingSessionCreateRequest`) nhập TAY thay vì tự động đọc GPS thiết bị;
/// nội dung gửi lên vẫn là toạ độ thật do người dùng nhập, không phải giá trị
/// giả lập. Xác thực khuôn mặt tài xế dùng camera thật + endpoint thật
/// (`POST /deliveries/{code}/verify-driver`, BR-050/051).
class ReceivingNewScreen extends StatefulWidget {
  final Delivery delivery;
  const ReceivingNewScreen({super.key, required this.delivery});

  @override
  State<ReceivingNewScreen> createState() => _ReceivingNewScreenState();
}

class _ReceivingNewScreenState extends State<ReceivingNewScreen> {
  bool _loading = true;
  PurchaseRequestDetail? _pr;
  List<SupplierBatch> _batches = [];
  List<BatchReview> _reviews = [];

  bool _faceVerifying = false;
  bool _faceVerified = false;
  /// 14/08/2026 — trước đây chỉ đếm số ảnh chụp (`_photosCount++`), KHÔNG giữ lại đường dẫn
  /// file nào, nên ảnh "bằng chứng phiên nhận (chung)" bắt buộc theo BR-072 chưa từng thật sự
  /// upload — giống lỗi tương tự đã sửa ở bản web (`receiving/new/page.tsx`). Giữ path để upload
  /// thật qua `uploadRejectDocument` (endpoint dùng chung) ngay trước khi tạo phiên.
  final List<String> _generalPhotoPaths = [];
  final _latCtrl = TextEditingController();
  final _lngCtrl = TextEditingController();
  final List<bool> _checks = List.filled(_checklist.length, false);
  bool _submitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _latCtrl.dispose();
    _lngCtrl.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    try {
      final results = await Future.wait([
        widget.delivery.purchaseRequestId == null
            ? Future.value(null)
            : ReceivingRepository.instance.fetchPurchaseRequest(widget.delivery.purchaseRequestId!),
        ReceivingRepository.instance.fetchSupplierBatches(),
      ]);
      final pr = results[0] as PurchaseRequestDetail?;
      final batches = results[1] as List<SupplierBatch>;
      final assigned = (pr?.items ?? const []).where((it) => it.supplierBatchId != null).toList();
      setState(() {
        _pr = pr;
        _batches = batches;
        _reviews = assigned
            .map((it) => BatchReview(ingredientId: it.ingredientId, ingredient: it.ingredient ?? it.ingredientId, batchId: it.supplierBatchId!))
            .toList();
      });
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được dữ liệu chuyến giao.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  /// ⚠️ NGOÀI PHẠM VI (đã grep xác nhận 06/08/2026): `GET /delivery-persons`
  /// chỉ `@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SUPPLIER')")` —
  /// SERVICE_CATERERS gọi vào sẽ 403 (khác bản Web vẫn gọi resource này để
  /// tra tên tài xế). Mobile chỉ hiển thị `driverId` thô.
  String get _driverName => widget.delivery.driverId ?? '—';

  SupplierBatch? _batchOf(String id) => _batches.where((b) => b.id == id).isEmpty ? null : _batches.firstWhere((b) => b.id == id);

  Future<void> _verifyFace() async {
    final photo = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 85);
    if (photo == null) return;
    setState(() => _faceVerifying = true);
    try {
      final ok = await ReceivingRepository.instance.verifyDriverFace(widget.delivery.id, photo.path);
      setState(() => _faceVerified = ok);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(ok ? 'Xác thực khuôn mặt $_driverName thành công (BR-051).' : 'Xác thực thất bại — không khớp khuôn mặt đã đăng ký.'),
            backgroundColor: ok ? AppTheme.successEmerald : AppTheme.dangerRose,
          ),
        );
      }
    } on ApiException catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.message), backgroundColor: AppTheme.dangerRose));
    } finally {
      if (mounted) setState(() => _faceVerifying = false);
    }
  }

  /*
   * 13/08/2026 — trước đây dùng `pickMultiImage()` (chọn ảnh có sẵn trong thư viện), lách
   * được yêu cầu chụp trực tiếp mà BR-072 bắt buộc cho mọi ảnh bằng chứng của Đơn vị suất
   * ăn. Đổi sang `pickImage(source: camera)`, mỗi lần bấm chụp 1 ảnh — cùng cách
   * `_pickRejectPhoto` bên dưới đã làm đúng; bấm nút nhiều lần để chụp nhiều ảnh.
   */
  Future<void> _pickGeneralPhotos() async {
    final photo = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 85);
    if (photo != null) setState(() => _generalPhotoPaths.add(photo.path));
  }

  Future<void> _pickRejectPhoto(BatchReview r) async {
    final photo = await ImagePicker().pickImage(source: ImageSource.camera, imageQuality: 85);
    if (photo != null) setState(() => r.rejectPhotoPaths.add(photo.path));
  }

  Future<void> _submit() async {
    if (_reviews.isEmpty) {
      setState(() => _error = 'Chuyến giao chưa có lô hàng nào được NCC gán (BR-071).');
      return;
    }
    if (!_faceVerified) {
      setState(() => _error = 'Tài xế $_driverName chưa được xác thực khuôn mặt tại cổng (BR-051).');
      return;
    }
    for (final r in _reviews) {
      final batch = _batchOf(r.batchId);
      if (!r.rejected && (batch?.isExpired ?? false)) {
        setState(() => _error = 'Lô ${r.batchId} (${r.ingredient}) đã hết hạn — phải từ chối lô này (BR-071).');
        return;
      }
      if (r.rejected && r.rejectReason.trim().isEmpty) {
        setState(() => _error = 'Lô ${r.batchId}: lô bị từ chối phải ghi rõ lý do.');
        return;
      }
      if (r.rejected && r.rejectPhotoPaths.isEmpty) {
        setState(() => _error = 'Lô ${r.batchId}: lô bị từ chối phải có ảnh chụp trực tiếp làm bằng chứng (BR-072).');
        return;
      }
    }
    if (_generalPhotoPaths.isEmpty) {
      setState(() => _error = 'Ảnh bằng chứng nhận hàng là bắt buộc (BR-072).');
      return;
    }
    final lat = double.tryParse(_latCtrl.text.trim());
    final lng = double.tryParse(_lngCtrl.text.trim());
    if (lat == null || lng == null) {
      setState(() => _error = 'Vĩ độ/kinh độ là bắt buộc (BR-073/074).');
      return;
    }
    if (_checks.any((c) => !c)) {
      setState(() => _error = 'Phải hoàn thành toàn bộ checklist nhận hàng.');
      return;
    }
    setState(() {
      _error = null;
      _submitting = true;
    });
    try {
      final lines = <Map<String, dynamic>>[];
      for (final r in _reviews) {
        List<String>? rejectDocumentIds;
        if (r.rejected) {
          rejectDocumentIds = [];
          for (final p in r.rejectPhotoPaths) {
            final id = await ReceivingRepository.instance.uploadRejectDocument(p);
            if (id.isNotEmpty) rejectDocumentIds.add(id);
          }
        }
        lines.add({
          'supplierBatchId': r.batchId,
          'ingredientId': r.ingredientId,
          'storageZone': r.storageZone.toUpperCase(),
          'rejected': r.rejected,
          if (r.rejected) 'rejectReason': r.rejectReason.trim(),
          if (rejectDocumentIds != null) 'rejectDocumentIds': rejectDocumentIds,
        });
      }
      final evidencePhotoIds = <String>[];
      for (final p in _generalPhotoPaths) {
        final id = await ReceivingRepository.instance.uploadRejectDocument(p);
        if (id.isNotEmpty) evidencePhotoIds.add(id);
      }
      await ReceivingRepository.instance.createSession(
        deliveryId: widget.delivery.id,
        lat: lat,
        lng: lng,
        photosCount: evidencePhotoIds.length,
        evidencePhotoIds: evidencePhotoIds,
        lines: lines,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Hoàn tất phiên nhận hàng.'), backgroundColor: AppTheme.successEmerald),
        );
        Navigator.pop(context);
      }
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không thể hoàn tất phiên nhận hàng — thử lại.');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Phiên nhận hàng ${widget.delivery.id}')),
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  if (_error != null)
                    Container(
                      margin: const EdgeInsets.only(bottom: 12),
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(color: AppTheme.dangerRose.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(8)),
                      child: Text(_error!, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 12)),
                    ),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Nhà cung cấp: ${_pr?.supplier ?? '—'}', style: const TextStyle(fontWeight: FontWeight.w600)),
                          const SizedBox(height: 4),
                          Text('Tài xế: $_driverName · Biển số ${widget.delivery.vehiclePlate ?? '—'}', style: const TextStyle(fontSize: 12, color: Colors.white70)),
                          const SizedBox(height: 10),
                          SizedBox(
                            width: double.infinity,
                            child: OutlinedButton.icon(
                              onPressed: _faceVerifying || _faceVerified ? null : _verifyFace,
                              icon: _faceVerifying
                                  ? const SizedBox(width: 14, height: 14, child: CircularProgressIndicator(strokeWidth: 2))
                                  : Icon(_faceVerified ? Icons.check_circle : Icons.face_retouching_natural, color: _faceVerified ? AppTheme.successEmerald : null),
                              label: Text(_faceVerified ? 'Đã xác thực khuôn mặt tài xế' : 'Xác thực khuôn mặt tài xế (BR-051)'),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text('Đối chiếu lô hàng (${_reviews.length} lô — BR-071)', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                  const SizedBox(height: 8),
                  if (_reviews.isEmpty)
                    const Text('Chuyến giao chưa có lô gán — NCC phải gán lô khi xác nhận đơn.', style: TextStyle(color: Colors.white54, fontSize: 12)),
                  for (final r in _reviews) _BatchReviewCard(review: r, batch: _batchOf(r.batchId), onChanged: () => setState(() {}), onCapturePhoto: () => _pickRejectPhoto(r)),
                  const SizedBox(height: 16),
                  const Text('Bằng chứng nhận hàng (BR-072/073/074)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                  const SizedBox(height: 8),
                  OutlinedButton.icon(onPressed: _pickGeneralPhotos, icon: const Icon(Icons.camera_alt_outlined), label: Text('Ảnh bằng chứng phiên nhận * (${_generalPhotoPaths.length})')),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(child: TextField(controller: _latCtrl, keyboardType: const TextInputType.numberWithOptions(decimal: true, signed: true), decoration: const InputDecoration(labelText: 'Vĩ độ (lat) *', border: OutlineInputBorder()))),
                      const SizedBox(width: 8),
                      Expanded(child: TextField(controller: _lngCtrl, keyboardType: const TextInputType.numberWithOptions(decimal: true, signed: true), decoration: const InputDecoration(labelText: 'Kinh độ (lng) *', border: OutlineInputBorder()))),
                    ],
                  ),
                  const SizedBox(height: 4),
                  const Text('Mobile chưa có định vị tự động — nhập toạ độ GPS thật đọc từ ứng dụng bản đồ.', style: TextStyle(fontSize: 10, color: Colors.white38)),
                  const SizedBox(height: 12),
                  const Text('Checklist nhận hàng *', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                  for (var i = 0; i < _checklist.length; i++)
                    CheckboxListTile(
                      dense: true,
                      contentPadding: EdgeInsets.zero,
                      title: Text(_checklist[i], style: const TextStyle(fontSize: 12)),
                      value: _checks[i],
                      onChanged: (v) => setState(() => _checks[i] = v ?? false),
                    ),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _submitting ? null : _submit,
                      child: _submitting
                          ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Text('Hoàn tất phiên nhận hàng'),
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}

class _BatchReviewCard extends StatelessWidget {
  final BatchReview review;
  final SupplierBatch? batch;
  final VoidCallback onChanged;
  final VoidCallback onCapturePhoto;
  const _BatchReviewCard({required this.review, required this.batch, required this.onChanged, required this.onCapturePhoto});

  @override
  Widget build(BuildContext context) {
    final expired = batch?.isExpired ?? false;
    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      color: review.rejected ? AppTheme.dangerRose.withValues(alpha: 0.05) : null,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(child: Text('${review.ingredient} · ${review.batchId}', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13))),
                if (expired) const Padding(padding: EdgeInsets.only(right: 6), child: Text('Hết hạn', style: TextStyle(color: AppTheme.dangerRose, fontSize: 11))),
                TextButton(
                  onPressed: () {
                    review.rejected = !review.rejected;
                    onChanged();
                  },
                  child: Text(review.rejected ? 'Bỏ từ chối' : 'Từ chối lô', style: TextStyle(color: review.rejected ? AppTheme.successEmerald : AppTheme.dangerRose, fontSize: 12)),
                ),
              ],
            ),
            if (batch != null)
              Text('Lô ${batch!.lotNumber ?? '—'} · HSD ${batch!.expirationDate ?? '—'} · ${batch!.quantity ?? '—'} ${batch!.unit ?? ''}',
                  style: const TextStyle(fontSize: 11, color: Colors.white54)),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              initialValue: review.storageZone,
              decoration: const InputDecoration(labelText: 'Khu vực lưu kho', isDense: true, border: OutlineInputBorder()),
              dropdownColor: AppTheme.cardDark,
              items: _storageZones.map((z) => DropdownMenuItem(value: z, child: Text(z))).toList(),
              onChanged: review.rejected
                  ? null
                  : (v) {
                      if (v != null) {
                        review.storageZone = v;
                        onChanged();
                      }
                    },
            ),
            if (review.rejected) ...[
              const SizedBox(height: 8),
              TextFormField(
                initialValue: review.rejectReason,
                minLines: 2,
                maxLines: 3,
                decoration: const InputDecoration(labelText: 'Lý do từ chối *', isDense: true, border: OutlineInputBorder()),
                onChanged: (v) {
                  review.rejectReason = v;
                  onChanged();
                },
              ),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: onCapturePhoto,
                icon: const Icon(Icons.camera_alt_outlined, size: 14),
                label: Text('Chụp ảnh bằng chứng * (${review.rejectPhotoPaths.length})', style: const TextStyle(fontSize: 11)),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
