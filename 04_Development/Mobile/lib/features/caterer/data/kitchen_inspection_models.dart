/// `KitchenInspectionResponse` (backend, MF07 — UC-014/BR-221). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/kitchen-inspections/`.
/// Kiểm tra vệ sinh BẾP do Y tế chấm (khác `InspectionSession` — kiểm thực 3
/// bước theo TỪNG BỮA do chính Caterers tự làm).
class KitchenInspectionItemResult {
  final String item;
  final bool passed;

  KitchenInspectionItemResult({required this.item, required this.passed});

  factory KitchenInspectionItemResult.fromJson(Map<String, dynamic> json) => KitchenInspectionItemResult(
        item: json['item'] as String? ?? '',
        passed: json['passed'] as bool? ?? false,
      );
}

class KitchenInspection {
  final String id;
  final String? date;
  final String? inspector;
  final int? score;
  final String? result;
  final String? confirmStatus; // AWAITING_CATERERS_CONFIRM | CONFIRMED
  final String? confirmedBy;
  final String? confirmedAt;
  final bool? staffHealthValid;
  final List<KitchenInspectionItemResult> itemResults;
  final List<String> failedItems;
  final String? recordContent;

  KitchenInspection({
    required this.id,
    required this.date,
    required this.inspector,
    required this.score,
    required this.result,
    required this.confirmStatus,
    required this.confirmedBy,
    required this.confirmedAt,
    required this.staffHealthValid,
    required this.itemResults,
    required this.failedItems,
    required this.recordContent,
  });

  factory KitchenInspection.fromJson(Map<String, dynamic> json) => KitchenInspection(
        id: json['id'] as String? ?? '',
        date: json['date'] as String?,
        inspector: json['inspector'] as String?,
        score: (json['score'] as num?)?.toInt(),
        result: json['result'] as String?,
        confirmStatus: json['confirmStatus'] as String?,
        confirmedBy: json['confirmedBy'] as String?,
        confirmedAt: json['confirmedAt'] as String?,
        staffHealthValid: json['staffHealthValid'] as bool?,
        itemResults: ((json['itemResults'] as List?) ?? const [])
            .map((e) => KitchenInspectionItemResult.fromJson(e as Map<String, dynamic>))
            .toList(),
        failedItems: (json['failedItems'] as List?)?.cast<String>() ?? const [],
        recordContent: json['recordContent'] as String?,
      );
}

String kitchenConfirmLabel(KitchenInspection k) {
  switch (k.confirmStatus) {
    case 'CONFIRMED':
      return 'Đã xác nhận';
    case 'AWAITING_CATERERS_CONFIRM':
      return 'Chờ bếp xác nhận kết quả';
    default:
      return 'Chưa có xác nhận';
  }
}
