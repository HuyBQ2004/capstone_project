/// `ClassMealDeliveryResponse` (backend, MF06 Phase 2) — bếp giao suất theo
/// (ngày, bữa, lớp) TRƯỚC khi giáo viên chia từng món cho từng học sinh ở
/// màn Phân phát bữa ăn. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/class-deliveries/page.tsx`.
class ClassMealDelivery {
  final String id;
  final String? classroomId;
  final String? date;
  final String? mealType;
  final String? status; // DELIVERED | RECEIVED
  final String? deliveredAt;
  final String? deliveredBy;
  final String? receivedAt;
  final String? receivedBy;
  final String? note;

  ClassMealDelivery({
    required this.id,
    required this.classroomId,
    required this.date,
    required this.mealType,
    required this.status,
    required this.deliveredAt,
    required this.deliveredBy,
    required this.receivedAt,
    required this.receivedBy,
    required this.note,
  });

  bool get isReceived => status == 'RECEIVED';

  factory ClassMealDelivery.fromJson(Map<String, dynamic> json) => ClassMealDelivery(
        id: json['id'] as String,
        classroomId: json['classroomId'] as String?,
        date: json['date'] as String?,
        mealType: json['mealType'] as String?,
        status: json['status'] as String?,
        deliveredAt: json['deliveredAt'] as String?,
        deliveredBy: json['deliveredBy'] as String?,
        receivedAt: json['receivedAt'] as String?,
        receivedBy: json['receivedBy'] as String?,
        note: json['note'] as String?,
      );
}

/// `PlannedDishSummary` — gộp theo món, số suất bếp cần nấu cho lớp (kể cả
/// suất thay thế do dị ứng — BR-065).
class PlannedDishSummary {
  final String? dishId;
  final String? dishName;
  final int count;
  final int substituteCount;
  final int blockedCount;

  PlannedDishSummary({
    required this.dishId,
    required this.dishName,
    required this.count,
    required this.substituteCount,
    required this.blockedCount,
  });

  factory PlannedDishSummary.fromJson(Map<String, dynamic> json) => PlannedDishSummary(
        dishId: json['dishId'] as String?,
        dishName: json['dishName'] as String?,
        count: json['count'] as int? ?? 0,
        substituteCount: json['substituteCount'] as int? ?? 0,
        blockedCount: json['blockedCount'] as int? ?? 0,
      );
}

/// `PlannedDishRow` — suất DỰ KIẾN của từng học sinh (chưa ghi gì, chỉ xem
/// trước đúng luật thay món của `serve()` — MF09 Phase 1).
class PlannedDishRow {
  final String? studentId;
  final String? studentName;
  final String? dishId;
  final String? dishName;
  final bool isSubstitute;
  final bool blocked;
  final String? reason;
  final String? substitutedForDishName;
  final String? distributionId;

  PlannedDishRow({
    required this.studentId,
    required this.studentName,
    required this.dishId,
    required this.dishName,
    required this.isSubstitute,
    required this.blocked,
    required this.reason,
    required this.substitutedForDishName,
    required this.distributionId,
  });

  factory PlannedDishRow.fromJson(Map<String, dynamic> json) => PlannedDishRow(
        studentId: json['studentId'] as String?,
        studentName: json['studentName'] as String?,
        dishId: json['dishId'] as String?,
        dishName: json['dishName'] as String?,
        isSubstitute: json['isSubstitute'] as bool? ?? false,
        blocked: json['blocked'] as bool? ?? false,
        reason: json['reason'] as String?,
        substitutedForDishName: json['substitutedForDishName'] as String?,
        distributionId: json['distributionId'] as String?,
      );
}

/// `PlannedDishesResponse` — `GET /student-distributions/planned-dishes`.
class PlannedDishes {
  final List<PlannedDishSummary> byDish;
  final List<PlannedDishRow> perStudent;

  PlannedDishes({required this.byDish, required this.perStudent});

  factory PlannedDishes.fromJson(Map<String, dynamic> json) => PlannedDishes(
        byDish: ((json['byDish'] as List?) ?? const [])
            .map((e) => PlannedDishSummary.fromJson(e as Map<String, dynamic>))
            .toList(),
        perStudent: ((json['perStudent'] as List?) ?? const [])
            .map((e) => PlannedDishRow.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}
