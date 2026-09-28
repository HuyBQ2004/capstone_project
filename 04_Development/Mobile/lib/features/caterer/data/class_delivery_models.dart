/// `ClassMealDeliveryResponse` (backend MF06 Phase 2) — bếp giao suất theo
/// (ngày, bữa, lớp). Đối chiếu
/// `04_Development/Mobile/lib/features/teacher/data/class_delivery_models.dart`
/// (phía NHẬN) — đây là phía GIAO của cùng luồng `class-meal-deliveries`.
class ClassMealDelivery {
  final String id;
  final String? classroomId;
  final String? date;
  final String? mealType;
  final String? status; // DELIVERED | RECEIVED
  final String? deliveredBy;
  final String? deliveredAt;
  final String? receivedBy;
  final String? receivedAt;
  final String? note;

  ClassMealDelivery({
    required this.id,
    required this.classroomId,
    required this.date,
    required this.mealType,
    required this.status,
    required this.deliveredBy,
    required this.deliveredAt,
    required this.receivedBy,
    required this.receivedAt,
    required this.note,
  });

  bool get isReceived => status == 'RECEIVED';

  factory ClassMealDelivery.fromJson(Map<String, dynamic> json) => ClassMealDelivery(
        id: json['id'] as String? ?? '',
        classroomId: json['classroomId'] as String?,
        date: json['date'] as String?,
        mealType: json['mealType'] as String?,
        status: json['status'] as String?,
        deliveredBy: json['deliveredBy'] as String?,
        deliveredAt: json['deliveredAt'] as String?,
        receivedBy: json['receivedBy'] as String?,
        receivedAt: json['receivedAt'] as String?,
        note: json['note'] as String?,
      );
}

/// `PlannedDishSummary` — gộp theo món, số suất bếp cần nấu cho lớp.
class PlannedDishSummary {
  final String? dishName;
  final int count;
  final int substituteCount;
  final int blockedCount;

  PlannedDishSummary({
    required this.dishName,
    required this.count,
    required this.substituteCount,
    required this.blockedCount,
  });

  factory PlannedDishSummary.fromJson(Map<String, dynamic> json) => PlannedDishSummary(
        dishName: json['dishName'] as String?,
        count: json['count'] as int? ?? 0,
        substituteCount: json['substituteCount'] as int? ?? 0,
        blockedCount: json['blockedCount'] as int? ?? 0,
      );
}

/// `PlannedDishesResponse` — `GET /student-distributions/planned-dishes`.
class PlannedDishes {
  final List<PlannedDishSummary> byDish;

  PlannedDishes({required this.byDish});

  factory PlannedDishes.fromJson(Map<String, dynamic> json) => PlannedDishes(
        byDish: ((json['byDish'] as List?) ?? const [])
            .map((e) => PlannedDishSummary.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

const Map<String, String> classDeliveryMealTypeLabel = {
  'BREAKFAST': 'Bữa sáng',
  'LUNCH': 'Bữa trưa',
  'SNACK': 'Bữa phụ',
};
