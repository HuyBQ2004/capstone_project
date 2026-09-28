/// `ChecklistItemResponse` (backend, MF07 — UC-186/BR-210).
class ChecklistItem {
  final String id;
  final String? name;
  final String? legalNote;

  ChecklistItem({required this.id, required this.name, required this.legalNote});

  factory ChecklistItem.fromJson(Map<String, dynamic> json) => ChecklistItem(
        id: json['id'] as String? ?? '',
        name: json['name'] as String?,
        legalNote: json['legalNote'] as String?,
      );
}

/// `ChecklistGroupResponse` — nhóm hạng mục checklist kiểm tra bếp, nạp động
/// từ `GET /checklist-groups` (BR-210), KHÔNG hard-code. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/conduct-inspection/page.tsx`.
class ChecklistGroup {
  final String id;
  final String? name;
  final int order;
  final List<ChecklistItem> items;

  ChecklistGroup({required this.id, required this.name, required this.order, required this.items});

  factory ChecklistGroup.fromJson(Map<String, dynamic> json) => ChecklistGroup(
        id: json['id'] as String? ?? '',
        name: json['name'] as String?,
        order: (json['order'] as num?)?.toInt() ?? 0,
        items: ((json['items'] as List?) ?? const [])
            .map((e) => ChecklistItem.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

/// `ItemResultDto`/`ItemFindingDto` gộp lại cho hiển thị chi tiết phiên đã nộp.
class InspectionItemResult {
  final String item;
  final bool passed;
  final int? photos;
  final String? reason;

  InspectionItemResult({required this.item, required this.passed, this.photos, this.reason});

  factory InspectionItemResult.fromJson(Map<String, dynamic> json) => InspectionItemResult(
        item: json['item'] as String? ?? '',
        passed: json['passed'] as bool? ?? false,
      );
}

/// `KitchenInspectionResponse` (backend, MF07 — UC-014). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/conduct-inspection/history/page.tsx`
/// và `[id]/page.tsx`.
class KitchenInspection {
  final String id;
  final String? date;
  final String? inspector;
  final int? score;
  final String? result;
  final String? confirmStatus;
  final String? confirmedBy;
  final String? confirmedAt;
  final bool? staffHealthValid;
  final List<InspectionItemResult> itemResults;
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
            .map((e) => InspectionItemResult.fromJson(e as Map<String, dynamic>))
            .toList(),
        failedItems: (json['failedItems'] as List?)?.cast<String>() ?? const [],
        recordContent: json['recordContent'] as String?,
      );
}

String kitchenConfirmLabel(KitchenInspection k) {
  switch (k.confirmStatus) {
    case 'CONFIRMED':
      return 'Bếp đã xác nhận';
    case 'AWAITING_CATERERS_CONFIRM':
      return 'Chờ bếp xác nhận';
    default:
      return 'Chưa có xác nhận';
  }
}
