/// `InventoryBatchResponse` (GET /inventory) — chỉ giữ field cần cho màn "Điều chỉnh tồn kho
/// thủ công" (BR-204). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inventory/page.tsx`.
class AdjustableBatch {
  final String id;
  final String ingredient;
  final String unit;
  final double? remaining;
  final double? quantity;
  final bool quarantined;

  AdjustableBatch({
    required this.id,
    required this.ingredient,
    required this.unit,
    this.remaining,
    this.quantity,
    required this.quarantined,
  });

  factory AdjustableBatch.fromJson(Map<String, dynamic> json) {
    return AdjustableBatch(
      id: json['id'] as String? ?? '',
      ingredient: json['ingredient'] as String? ?? '',
      unit: json['unit'] as String? ?? '',
      remaining: (json['remaining'] as num?)?.toDouble(),
      quantity: (json['quantity'] as num?)?.toDouble(),
      quarantined: json['quarantine'] != null,
    );
  }

  /// BR-205: hiển thị đúng số còn dùng được (không phải tổng đã nhập).
  double get displayQty => remaining ?? quantity ?? 0;
}

/// Giá trị PHẢI khớp enum Java `ReasonCode` (SCREAMING_SNAKE, D6) — cùng danh sách
/// `REASON_CODES` ở bản web.
const kAdjustmentReasonCodes = <(String, String)>[
  ('SPOILAGE', 'SPOILAGE — hư hỏng/ôi thiu'),
  ('COUNT_CORRECTION', 'COUNT_CORRECTION — chênh lệch kiểm kê'),
  ('DAMAGE', 'DAMAGE — hỏng vỡ khi lưu kho'),
  ('RETURN_TO_SUPPLIER', 'RETURN_TO_SUPPLIER — trả NCC'),
  ('EXTRA_USAGE', 'EXTRA_USAGE — dùng nhiều hơn định mức khi nấu'),
];
