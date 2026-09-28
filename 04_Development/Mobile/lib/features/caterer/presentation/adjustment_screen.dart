import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../data/adjustment_models.dart';
import '../data/adjustment_repository.dart';
import '../data/inspection_models.dart' show mealTypeLabel;

String _fmtQty(double v) {
  if (v == v.roundToDouble()) return v.toInt().toString();
  return v.toStringAsFixed(2);
}

/// Điều chỉnh tồn kho thủ công (BR-204) — MF06. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inventory/page.tsx`
/// (`AdjustStockDialog`).
///
/// 14/08/2026 (Nam) — mobile trước đây KHÔNG có màn này (chỉ có "Xuất kho theo bữa" FEFO,
/// không có đường điều chỉnh thủ công). Làm mới hoàn toàn, mang sẵn `serviceDate`/`mealType`
/// tuỳ chọn (không phải thêm sau) — gắn điều chỉnh (thường EXTRA_USAGE, VD nấu thiếu cần bù
/// nguyên liệu) vào đúng bữa để kiểm thực Bước 1 auto-fill nhận diện được, giống bản web.
class AdjustmentScreen extends StatefulWidget {
  const AdjustmentScreen({super.key});

  @override
  State<AdjustmentScreen> createState() => _AdjustmentScreenState();
}

class _AdjustmentScreenState extends State<AdjustmentScreen> {
  bool _loading = true;
  String? _loadError;
  List<AdjustableBatch> _batches = [];

  String? _batchId;
  final _qtyCtrl = TextEditingController();
  String? _reasonCode;
  final _signatureCtrl = TextEditingController();
  DateTime? _serviceDate;
  String? _mealType;
  bool _submitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _qtyCtrl.dispose();
    _signatureCtrl.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _loadError = null;
    });
    try {
      final batches = await AdjustmentRepository.instance.fetchAdjustableBatches();
      setState(() => _batches = batches);
    } on ApiException catch (e) {
      setState(() => _loadError = e.message);
    } catch (_) {
      setState(() => _loadError = 'Không tải được dữ liệu. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  AdjustableBatch? get _batch {
    for (final b in _batches) {
      if (b.id == _batchId) return b;
    }
    return null;
  }

  Future<void> _pickServiceDate() async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: _serviceDate ?? now,
      firstDate: now.subtract(const Duration(days: 30)),
      lastDate: now.add(const Duration(days: 30)),
    );
    if (picked != null) setState(() => _serviceDate = picked);
  }

  Future<void> _submit() async {
    final batchId = _batchId;
    final qty = double.tryParse(_qtyCtrl.text.trim().replaceAll(',', '.'));
    if (batchId == null || qty == null || qty == 0) {
      setState(() => _error = 'Chọn lô và nhập lượng điều chỉnh (âm để giảm, dương để tăng).');
      return;
    }
    final reasonCode = _reasonCode;
    if (reasonCode == null) {
      setState(() => _error = 'Mã lý do là bắt buộc cho điều chỉnh thủ công (BR-204).');
      return;
    }
    final signature = _signatureCtrl.text.trim();
    if (signature.isEmpty) {
      setState(() => _error = 'Điều chỉnh thủ công phải có xác nhận (ký) của quản lý (BR-204).');
      return;
    }
    setState(() {
      _error = null;
      _submitting = true;
    });
    try {
      await AdjustmentRepository.instance.createAdjustment(
        batchId: batchId,
        delta: qty,
        unit: _batch?.unit ?? 'kg',
        reasonCode: reasonCode,
        managerSignature: signature,
        serviceDate: _serviceDate == null
            ? null
            : '${_serviceDate!.year.toString().padLeft(4, '0')}-${_serviceDate!.month.toString().padLeft(2, '0')}-${_serviceDate!.day.toString().padLeft(2, '0')}',
        mealType: _mealType,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Đã ghi điều chỉnh $batchId ($reasonCode) — ký: $signature (BR-204).')),
        );
        Navigator.pop(context);
      }
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không thể ghi điều chỉnh — thử lại.');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Điều chỉnh tồn kho (BR-204)')),
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
                      if (_error != null) ...[
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.red.withValues(alpha: 0.1),
                            borderRadius: BorderRadius.circular(8),
                          ),
                          child: Text(_error!, style: const TextStyle(color: Colors.redAccent)),
                        ),
                        const SizedBox(height: 12),
                      ],
                      DropdownButtonFormField<String>(
                        initialValue: _batchId,
                        isExpanded: true,
                        decoration: const InputDecoration(labelText: 'Lô nguyên liệu *', border: OutlineInputBorder()),
                        items: [
                          for (final b in _batches)
                            DropdownMenuItem(
                              value: b.id,
                              child: Text('${b.id} — ${b.ingredient} (${_fmtQty(b.displayQty)} ${b.unit})', overflow: TextOverflow.ellipsis),
                            ),
                        ],
                        onChanged: (v) => setState(() => _batchId = v),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _qtyCtrl,
                        keyboardType: const TextInputType.numberWithOptions(decimal: true, signed: true),
                        decoration: const InputDecoration(labelText: 'Lượng điều chỉnh * (VD: -2)', border: OutlineInputBorder()),
                      ),
                      const SizedBox(height: 12),
                      DropdownButtonFormField<String>(
                        initialValue: _reasonCode,
                        isExpanded: true,
                        decoration: const InputDecoration(labelText: 'Mã lý do * (BR-204)', border: OutlineInputBorder()),
                        items: [
                          for (final (code, label) in kAdjustmentReasonCodes)
                            DropdownMenuItem(value: code, child: Text(label, overflow: TextOverflow.ellipsis)),
                        ],
                        onChanged: (v) => setState(() => _reasonCode = v),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _signatureCtrl,
                        decoration: const InputDecoration(labelText: 'Quản lý xác nhận (ký) * (BR-204)', border: OutlineInputBorder(), hintText: 'VD: Lê Thị Mai'),
                      ),
                      const SizedBox(height: 16),
                      const Text('Gắn với bữa (tuỳ chọn — dùng cho EXTRA_USAGE)', style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Expanded(
                            child: OutlinedButton(
                              onPressed: _pickServiceDate,
                              child: Text(_serviceDate == null
                                  ? 'Chọn ngày phục vụ'
                                  : '${_serviceDate!.day.toString().padLeft(2, '0')}/${_serviceDate!.month.toString().padLeft(2, '0')}/${_serviceDate!.year}'),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: DropdownButtonFormField<String>(
                              initialValue: _mealType,
                              isExpanded: true,
                              decoration: const InputDecoration(border: OutlineInputBorder(), contentPadding: EdgeInsets.symmetric(horizontal: 12)),
                              hint: const Text('Buổi ăn'),
                              items: [
                                for (final entry in mealTypeLabel.entries)
                                  DropdownMenuItem(value: entry.key, child: Text(entry.value, overflow: TextOverflow.ellipsis)),
                              ],
                              onChanged: (v) => setState(() => _mealType = v),
                            ),
                          ),
                        ],
                      ),
                      if (_serviceDate != null || _mealType != null) ...[
                        const SizedBox(height: 6),
                        const Text(
                          'Điều chỉnh sẽ được nhận diện ở Bước 1 kiểm thực và Phạm vi bữa ăn, giống xuất kho FEFO.',
                          style: TextStyle(fontSize: 11, color: Colors.white54),
                        ),
                      ],
                      const SizedBox(height: 20),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed: _submitting ? null : _submit,
                          child: _submitting
                              ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                              : const Text('Ghi điều chỉnh'),
                        ),
                      ),
                    ],
                  ),
      ),
    );
  }
}
