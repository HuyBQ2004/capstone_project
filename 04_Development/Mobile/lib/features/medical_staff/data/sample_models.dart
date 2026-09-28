/// `FoodSampleResponse` (backend, MF08 — UC-118/UC-076). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/samples/page.tsx`.
class FoodSample {
  final String id;
  final String? dish;
  final String? dishId;
  final String? meal;
  final String? mealShift;
  final String? mealType;
  final String? weight;
  final int? hoursRemaining;
  final double? fridgeTemp;
  final String? collectedBy;
  final String? status;
  final String? incidentId;

  FoodSample({
    required this.id,
    required this.dish,
    required this.dishId,
    required this.meal,
    required this.mealShift,
    required this.mealType,
    required this.weight,
    required this.hoursRemaining,
    required this.fridgeTemp,
    required this.collectedBy,
    required this.status,
    required this.incidentId,
  });

  factory FoodSample.fromJson(Map<String, dynamic> json) => FoodSample(
        id: json['id'] as String? ?? '',
        dish: json['dish'] as String?,
        dishId: json['dishId'] as String?,
        meal: json['meal'] as String?,
        mealShift: json['mealShift'] as String?,
        mealType: json['mealType'] as String?,
        weight: json['weight'] as String?,
        hoursRemaining: (json['hoursRemaining'] as num?)?.toInt(),
        fridgeTemp: (json['fridgeTemp'] as num?)?.toDouble(),
        collectedBy: json['collectedBy'] as String?,
        status: json['status'] as String?,
        incidentId: json['incidentId'] as String?,
      );
}

const Map<String, String> mealTypeLabel = {
  'BREAKFAST': 'Bữa sáng',
  'LUNCH': 'Bữa trưa',
  'SNACK': 'Bữa chiều',
};

String sampleStatusLabel(String? status) {
  switch (status) {
    case 'RETAINED':
      return 'Đang lưu';
    case 'SENT_TO_LAB':
      return 'Đã gửi kiểm nghiệm';
    case 'DISPOSED':
      return 'Đã hủy';
    default:
      return status ?? '—';
  }
}

/// `ReceivingAuthorityResponse` — danh mục nơi tiếp nhận mẫu (MF08).
class ReceivingAuthority {
  final String id;
  final String? name;
  final String? type;
  final bool isActive;

  ReceivingAuthority({required this.id, required this.name, required this.type, required this.isActive});

  factory ReceivingAuthority.fromJson(Map<String, dynamic> json) => ReceivingAuthority(
        id: json['id'] as String? ?? '',
        name: json['name'] as String?,
        type: json['type'] as String?,
        isActive: json['isActive'] as bool? ?? false,
      );
}

/// `IncidentDto` — rút gọn, chỉ phần cần cho màn Sổ lưu mẫu (chọn sự cố khi
/// thu hồi & gửi xét nghiệm).
class IncidentBrief {
  final String id;
  final String? title;
  final String? status;

  IncidentBrief({required this.id, required this.title, required this.status});

  factory IncidentBrief.fromJson(Map<String, dynamic> json) => IncidentBrief(
        id: json['id'] as String? ?? '',
        title: json['title'] as String?,
        status: json['status'] as String?,
      );
}

/// `SampleLabRequestResponse` — phiếu thu hồi & bàn giao mẫu (UC-076).
class SampleLabRequest {
  final String id;
  final String? status;
  final List<String> sampleIds;

  SampleLabRequest({required this.id, required this.status, required this.sampleIds});

  factory SampleLabRequest.fromJson(Map<String, dynamic> json) => SampleLabRequest(
        id: json['id'] as String? ?? '',
        status: json['status'] as String?,
        sampleIds: (json['sampleIds'] as List?)?.cast<String>() ?? const [],
      );
}
