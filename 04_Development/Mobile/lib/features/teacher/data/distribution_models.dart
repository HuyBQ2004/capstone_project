/// `ServingDishView` — món ĐÃ PHÁT (đóng băng, KHÔNG dẫn xuất — ADR-M09-1).
class ServingDishView {
  final String? dishId;
  final String? dishName;
  final bool isSubstitute;
  final String? reason;
  final String? substitutedForDishName;

  ServingDishView({
    required this.dishId,
    required this.dishName,
    required this.isSubstitute,
    required this.reason,
    required this.substitutedForDishName,
  });

  factory ServingDishView.fromJson(Map<String, dynamic> json) => ServingDishView(
        dishId: json['dishId'] as String?,
        dishName: json['dishName'] as String?,
        isSubstitute: json['isSubstitute'] as bool? ?? false,
        reason: json['reason'] as String?,
        substitutedForDishName: json['substitutedForDishName'] as String?,
      );
}

/// `DistributionResponse` (backend, MF09) — 1 phiên phân phát bữa ăn của 1 học
/// sinh. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/distribution/page.tsx`.
class Distribution {
  final String id;
  final String? studentId;
  final String? name;
  final String? classroomId;
  final String? date;
  final String? mealType;
  final String? status; // PENDING | SERVED
  final String? isSpecial; // yes | no
  final String? specialDiet;
  final List<String> allergyWarning;
  final String? servedAt;
  final List<ServingDishView> servedDishes;
  final String? catererName;

  Distribution({
    required this.id,
    required this.studentId,
    required this.name,
    required this.classroomId,
    required this.date,
    required this.mealType,
    required this.status,
    required this.isSpecial,
    required this.specialDiet,
    required this.allergyWarning,
    required this.servedAt,
    required this.servedDishes,
    required this.catererName,
  });

  bool get isPending => status == 'PENDING';
  bool get isServed => status == 'SERVED';
  bool get hasAllergyWarning => allergyWarning.isNotEmpty;

  factory Distribution.fromJson(Map<String, dynamic> json) => Distribution(
        id: json['id'] as String,
        studentId: json['studentId'] as String?,
        name: json['name'] as String?,
        classroomId: json['class'] as String?,
        date: json['date'] as String?,
        mealType: json['mealType'] as String?,
        status: json['status'] as String?,
        isSpecial: json['isSpecial'] as String?,
        specialDiet: json['specialDiet'] as String?,
        allergyWarning: (json['allergyWarning'] as List?)?.cast<String>() ?? const [],
        servedAt: json['servedAt'] as String?,
        servedDishes: ((json['servedDishes'] as List?) ?? const [])
            .map((e) => ServingDishView.fromJson(e as Map<String, dynamic>))
            .toList(),
        catererName: json['catererName'] as String?,
      );
}
