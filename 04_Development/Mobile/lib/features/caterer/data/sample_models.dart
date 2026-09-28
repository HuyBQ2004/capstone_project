/// `FoodSampleResponse` (backend MF08 — UC-069/118). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/samples/page.tsx`.
class FoodSample {
  final String id;
  final String? dish;
  final String? dishId;
  final String? mealType;
  final String? meal;
  final String? mealShift;
  final int? servings;
  final String? weight;
  final String? container;
  final double? fridgeTemp;
  final bool tempOutOfRange;
  final String? collectedAt;
  final String? collectedBy;
  final String? storageLocation;
  final int? hoursRemaining;
  final String? status;
  final String? inspectionId;

  FoodSample({
    required this.id,
    required this.dish,
    required this.dishId,
    required this.mealType,
    required this.meal,
    required this.mealShift,
    required this.servings,
    required this.weight,
    required this.container,
    required this.fridgeTemp,
    required this.tempOutOfRange,
    required this.collectedAt,
    required this.collectedBy,
    required this.storageLocation,
    required this.hoursRemaining,
    required this.status,
    required this.inspectionId,
  });

  factory FoodSample.fromJson(Map<String, dynamic> json) => FoodSample(
        id: json['id'] as String? ?? '',
        dish: json['dish'] as String?,
        dishId: json['dishId'] as String?,
        mealType: json['mealType'] as String?,
        meal: json['meal'] as String?,
        mealShift: json['mealShift'] as String?,
        servings: (json['servings'] as num?)?.toInt(),
        weight: json['weight'] as String?,
        container: json['container'] as String?,
        fridgeTemp: (json['fridgeTemp'] as num?)?.toDouble(),
        tempOutOfRange: json['tempOutOfRange'] as bool? ?? false,
        collectedAt: json['collectedAt'] as String?,
        collectedBy: json['collectedBy'] as String?,
        storageLocation: json['storageLocation'] as String?,
        hoursRemaining: (json['hoursRemaining'] as num?)?.toInt(),
        status: json['status'] as String?,
        inspectionId: json['inspectionId'] as String?,
      );
}

const Map<String, String> sampleMealTypeLabel = {
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

/// Món đủ điều kiện lấy mẫu của 1 phiên kiểm thực đã PASSED — lấy từ
/// `step3Rows` (ưu tiên) hoặc `step2Rows` của phiên đó (mã món + số suất
/// thật, không đoán).
class DishChoice {
  final String name;
  final String dishId;
  final String servings;

  DishChoice({required this.name, required this.dishId, required this.servings});
}
